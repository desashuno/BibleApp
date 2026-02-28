package org.biblestudio.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.tan

/**
 * Data class representing a geographic point to display on the map.
 */
data class MapPin(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val label: String,
    val color: Color = Color(0xFFE91E63),
    val isSelected: Boolean = false
)

/**
 * State holder for the OSM tile map.
 */
class OsmMapState(
    initialCenterLat: Double = 31.7683,
    initialCenterLng: Double = 35.2137,
    initialZoom: Int = 7
) {
    var centerLat by mutableStateOf(initialCenterLat)
    var centerLng by mutableStateOf(initialCenterLng)
    var zoom by mutableStateOf(initialZoom.coerceIn(MIN_ZOOM, MAX_ZOOM))

    @Suppress("UNUSED_PARAMETER")
    fun pan(dx: Float, dy: Float, canvasWidth: Float, canvasHeight: Float) {
        val n = 1 shl zoom
        val tileSize = TILE_SIZE.toFloat()
        val totalPixels = n * tileSize

        // Convert pixel delta to lat/lng delta
        val lngDelta = dx / totalPixels * 360.0
        centerLng = (centerLng - lngDelta).coerceIn(-180.0, 180.0)

        val latRad = centerLat * PI / 180.0
        val mercY = ln(tan(PI / 4.0 + latRad / 2.0))
        val pixelPerMerc = totalPixels / (2.0 * PI)
        val newMercY = mercY + dy / pixelPerMerc
        centerLat = (atan(exp(newMercY)) * 2.0 - PI / 2.0) * 180.0 / PI
        centerLat = centerLat.coerceIn(-85.0, 85.0)
    }

    fun zoomIn() { zoom = min(zoom + 1, MAX_ZOOM) }
    fun zoomOut() { zoom = max(zoom - 1, MIN_ZOOM) }

    companion object {
        const val MIN_ZOOM = 2
        const val MAX_ZOOM = 17
        const val TILE_SIZE = 256
    }
}

// ─────────────────── Tile math ───────────────────

internal fun latLngToTileXY(lat: Double, lng: Double, zoom: Int): Pair<Double, Double> {
    val n = (1 shl zoom).toDouble()
    val x = (lng + 180.0) / 360.0 * n
    val latRad = lat * PI / 180.0
    val y = (1.0 - ln(tan(latRad) + 1.0 / kotlin.math.cos(latRad)) / PI) / 2.0 * n
    return x to y
}

internal fun tileXYToLatLng(tileX: Double, tileY: Double, zoom: Int): Pair<Double, Double> {
    val n = (1 shl zoom).toDouble()
    val lng = tileX / n * 360.0 - 180.0
    val latRad = atan(kotlin.math.sinh(PI * (1.0 - 2.0 * tileY / n)))
    val lat = latRad * 180.0 / PI
    return lat to lng
}

internal fun latLngToPixel(
    lat: Double,
    lng: Double,
    centerLat: Double,
    centerLng: Double,
    zoom: Int,
    canvasWidth: Float,
    canvasHeight: Float
): Offset {
    val (cx, cy) = latLngToTileXY(centerLat, centerLng, zoom)
    val (px, py) = latLngToTileXY(lat, lng, zoom)
    val tileSize = OsmMapState.TILE_SIZE.toFloat()
    val x = canvasWidth / 2f + ((px - cx) * tileSize).toFloat()
    val y = canvasHeight / 2f + ((py - cy) * tileSize).toFloat()
    return Offset(x, y)
}

// ─────────────────── Tile cache ───────────────────

internal object TileCache {
    private val cache = LinkedHashMap<String, ImageBitmap?>(256, 0.75f, true)
    private val loading = mutableSetOf<String>()
    private const val MAX_SIZE = 512

    fun get(key: String): ImageBitmap? = cache[key]
    fun contains(key: String): Boolean = key in cache
    fun isLoading(key: String): Boolean = key in loading
    fun markLoading(key: String) { loading.add(key) }

    fun put(key: String, bitmap: ImageBitmap?) {
        loading.remove(key)
        cache[key] = bitmap
        // Evict oldest if needed
        while (cache.size > MAX_SIZE) {
            val oldest = cache.keys.firstOrNull() ?: break
            cache.remove(oldest)
        }
    }
}

// ─────────────────── Composable ───────────────────

/**
 * OpenStreetMap tile map rendered in a Compose Canvas.
 *
 * Supports drag-to-pan and tap-on-pin interactions.
 * Tiles are loaded from `tile.openstreetmap.org` and cached in memory.
 *
 * @param mapState Mutable state for center position and zoom.
 * @param pins List of pins to draw on the map.
 * @param onPinClicked Called when a pin is tapped.
 * @param onZoomChanged Called when zoom changes (for external sync).
 * @param modifier Standard Compose modifier.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod", "MagicNumber")
@Composable
fun OsmTileMap(
    mapState: OsmMapState,
    pins: List<MapPin>,
    onPinClicked: (MapPin) -> Unit = {},
    onZoomChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()

    // Track canvas size for hit testing
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Re-compose trigger for tile loading
    var tileLoadTrigger by remember { mutableStateOf(0) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    mapState.pan(
                        dx = -dragAmount.x,
                        dy = -dragAmount.y,
                        canvasWidth = size.width.toFloat(),
                        canvasHeight = size.height.toFloat()
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        mapState.zoomIn()
                        onZoomChanged(mapState.zoom)
                    },
                    onTap = { offset ->
                        // Hit-test pins
                        val hitRadius = 24f
                        for (pin in pins) {
                            val pinPos = latLngToPixel(
                                pin.latitude, pin.longitude,
                                mapState.centerLat, mapState.centerLng,
                                mapState.zoom,
                                canvasSize.width, canvasSize.height
                            )
                            val dist = (offset - pinPos).getDistance()
                            if (dist <= hitRadius) {
                                onPinClicked(pin)
                                return@detectTapGestures
                            }
                        }
                    }
                )
            }
    ) {
        canvasSize = size
        val zoom = mapState.zoom
        val tileSize = OsmMapState.TILE_SIZE.toFloat()

        // Calculate which tiles are visible
        val (centerTileX, centerTileY) = latLngToTileXY(mapState.centerLat, mapState.centerLng, zoom)
        val halfTilesX = (size.width / tileSize / 2f).toInt() + 2
        val halfTilesY = (size.height / tileSize / 2f).toInt() + 2

        val tileMinX = floor(centerTileX).toInt() - halfTilesX
        val tileMaxX = floor(centerTileX).toInt() + halfTilesX
        val tileMinY = floor(centerTileY).toInt() - halfTilesY
        val tileMaxY = floor(centerTileY).toInt() + halfTilesY
        val n = 1 shl zoom

        // Draw background
        drawRect(Color(0xFFADD8E6)) // Light blue ocean

        // Draw tiles
        for (ty in tileMinY..tileMaxY) {
            for (tx in tileMinX..tileMaxX) {
                val wrappedTx = ((tx % n) + n) % n
                if (ty < 0 || ty >= n) continue

                val offsetX = size.width / 2f + ((tx - centerTileX) * tileSize).toFloat()
                val offsetY = size.height / 2f + ((ty - centerTileY) * tileSize).toFloat()

                val tileKey = "$zoom/$wrappedTx/$ty"
                val bitmap = TileCache.get(tileKey)

                if (bitmap != null) {
                    drawImage(
                        image = bitmap,
                        dstOffset = IntOffset(offsetX.roundToInt(), offsetY.roundToInt()),
                        dstSize = IntSize(tileSize.roundToInt(), tileSize.roundToInt())
                    )
                } else {
                    // Draw placeholder tile
                    drawRect(
                        color = Color(0xFFE8E8E8),
                        topLeft = Offset(offsetX, offsetY),
                        size = Size(tileSize, tileSize)
                    )

                    // Start loading if not already loading
                    if (!TileCache.isLoading(tileKey) && !TileCache.contains(tileKey)) {
                        TileCache.markLoading(tileKey)
                        scope.launch {
                            val loaded = loadTileBitmap(zoom, wrappedTx, ty)
                            TileCache.put(tileKey, loaded)
                            tileLoadTrigger++ // Force recomposition
                        }
                    }
                }
            }
        }

        // Draw pins
        for (pin in pins) {
            val pinPos = latLngToPixel(
                pin.latitude, pin.longitude,
                mapState.centerLat, mapState.centerLng,
                zoom, size.width, size.height
            )

            // Skip off-screen pins
            if (pinPos.x < -30f || pinPos.x > size.width + 30f ||
                pinPos.y < -30f || pinPos.y > size.height + 30f) continue

            val radius = if (pin.isSelected) 12f else 8f

            // Pin shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = radius + 1f,
                center = pinPos + Offset(1f, 2f)
            )

            // Pin fill
            drawCircle(color = pin.color, radius = radius, center = pinPos)

            // Selection ring
            if (pin.isSelected) {
                drawCircle(
                    color = Color.White,
                    radius = radius + 3f,
                    center = pinPos,
                    style = Stroke(width = 2.5f)
                )
            }

            // Pin border
            drawCircle(
                color = Color.White,
                radius = radius,
                center = pinPos,
                style = Stroke(width = 1.5f)
            )
        }

        // Attribution (required by OSM)
        drawOsmAttribution(textMeasurer)

        // Zoom indicator
        drawZoomIndicator(textMeasurer, zoom)

        // Force read of tileLoadTrigger to trigger recomposition
        @Suppress("UNUSED_EXPRESSION")
        tileLoadTrigger
    }
}

@Suppress("MagicNumber")
private fun DrawScope.drawOsmAttribution(textMeasurer: TextMeasurer) {
    val attrText = "© OpenStreetMap contributors"
    val attrStyle = TextStyle(fontSize = 9.sp, color = Color(0xFF666666))
    val measured = textMeasurer.measure(attrText, attrStyle)
    val bgPadding = 4f
    drawRect(
        color = Color.White.copy(alpha = 0.8f),
        topLeft = Offset(
            size.width - measured.size.width - bgPadding * 2,
            size.height - measured.size.height - bgPadding * 2
        ),
        size = Size(
            measured.size.width + bgPadding * 2,
            measured.size.height + bgPadding * 2
        )
    )
    drawText(
        textLayoutResult = measured,
        topLeft = Offset(
            size.width - measured.size.width - bgPadding,
            size.height - measured.size.height - bgPadding
        )
    )
}

@Suppress("MagicNumber")
private fun DrawScope.drawZoomIndicator(textMeasurer: TextMeasurer, zoom: Int) {
    val zoomText = "Z$zoom"
    val zoomStyle = TextStyle(fontSize = 11.sp, color = Color(0xFF333333))
    val measured = textMeasurer.measure(zoomText, zoomStyle)
    val padding = 6f
    drawRect(
        color = Color.White.copy(alpha = 0.85f),
        topLeft = Offset(padding, size.height - measured.size.height - padding * 2),
        size = Size(measured.size.width + padding * 2, measured.size.height + padding * 2)
    )
    drawText(
        textLayoutResult = measured,
        topLeft = Offset(padding * 2, size.height - measured.size.height - padding)
    )
}
