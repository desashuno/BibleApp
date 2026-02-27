package org.biblestudio.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom icon set for Bible-study-specific concepts not covered by
 * the Material icon library. Each icon uses a 24×24 dp viewport.
 *
 * Icons: Interlinear, Strongs, CrossRef, Parallel, Hebrew, Greek.
 */
@Suppress("MagicNumber")
object BsIcons {
    /** Two overlapping lines representing interlinear text. */
    val Interlinear: ImageVector by lazy {
        ImageVector.Builder(
            name = "Interlinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                // Top line
                moveTo(3f, 7f)
                horizontalLineTo(21f)
                verticalLineTo(9f)
                horizontalLineTo(3f)
                close()
                // Bottom line (indented for interlinear offset)
                moveTo(5f, 12f)
                horizontalLineTo(19f)
                verticalLineTo(14f)
                horizontalLineTo(5f)
                close()
                // Third row
                moveTo(3f, 17f)
                horizontalLineTo(21f)
                verticalLineTo(19f)
                horizontalLineTo(3f)
                close()
            }
        }.build()
    }

    /** Strong's concordance number icon: stylised hash (#) symbol. */
    val Strongs: ImageVector by lazy {
        ImageVector.Builder(
            name = "Strongs",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                // Vertical bars
                moveTo(8f, 4f)
                lineTo(10f, 4f)
                lineTo(9f, 20f)
                lineTo(7f, 20f)
                close()
                moveTo(14f, 4f)
                lineTo(16f, 4f)
                lineTo(15f, 20f)
                lineTo(13f, 20f)
                close()
                // Horizontal bars
                moveTo(5f, 9f)
                horizontalLineTo(19f)
                verticalLineTo(11f)
                horizontalLineTo(5f)
                close()
                moveTo(5f, 14f)
                horizontalLineTo(19f)
                verticalLineTo(16f)
                horizontalLineTo(5f)
                close()
            }
        }.build()
    }

    /** Cross-reference icon: two crossing arrows. */
    val CrossRef: ImageVector by lazy {
        ImageVector.Builder(
            name = "CrossRef",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 11f)
                horizontalLineTo(18f)
                lineTo(15f, 8f)
                lineTo(16.5f, 6.5f)
                lineTo(21f, 11f)
                lineTo(21f, 13f)
                lineTo(16.5f, 17.5f)
                lineTo(15f, 16f)
                lineTo(18f, 13f)
                horizontalLineTo(3f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(11f, 3f)
                verticalLineTo(18f)
                lineTo(8f, 15f)
                lineTo(6.5f, 16.5f)
                lineTo(11f, 21f)
                lineTo(13f, 21f)
                lineTo(17.5f, 16.5f)
                lineTo(16f, 15f)
                lineTo(13f, 18f)
                verticalLineTo(3f)
                close()
            }
        }.build()
    }

    /** Parallel icon: two side-by-side columns. */
    val Parallel: ImageVector by lazy {
        ImageVector.Builder(
            name = "Parallel",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                // Left column frame
                moveTo(3f, 3f)
                horizontalLineTo(10f)
                verticalLineTo(5f)
                horizontalLineTo(5f)
                verticalLineTo(19f)
                horizontalLineTo(10f)
                verticalLineTo(21f)
                horizontalLineTo(3f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                // Right column frame
                moveTo(14f, 3f)
                horizontalLineTo(21f)
                verticalLineTo(21f)
                horizontalLineTo(14f)
                verticalLineTo(19f)
                horizontalLineTo(19f)
                verticalLineTo(5f)
                horizontalLineTo(14f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                // Left column text lines
                moveTo(6f, 8f)
                horizontalLineTo(9f)
                verticalLineTo(9f)
                horizontalLineTo(6f)
                close()
                moveTo(6f, 11f)
                horizontalLineTo(9f)
                verticalLineTo(12f)
                horizontalLineTo(6f)
                close()
                moveTo(6f, 14f)
                horizontalLineTo(9f)
                verticalLineTo(15f)
                horizontalLineTo(6f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                // Right column text lines
                moveTo(15f, 8f)
                horizontalLineTo(18f)
                verticalLineTo(9f)
                horizontalLineTo(15f)
                close()
                moveTo(15f, 11f)
                horizontalLineTo(18f)
                verticalLineTo(12f)
                horizontalLineTo(15f)
                close()
                moveTo(15f, 14f)
                horizontalLineTo(18f)
                verticalLineTo(15f)
                horizontalLineTo(15f)
                close()
            }
        }.build()
    }

    /** Hebrew script icon: stylised Aleph-like glyph. */
    val Hebrew: ImageVector by lazy {
        ImageVector.Builder(
            name = "Hebrew",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 4f)
                lineTo(8f, 4f)
                lineTo(18f, 20f)
                lineTo(16f, 20f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 4f)
                lineTo(16f, 4f)
                lineTo(6f, 20f)
                lineTo(8f, 20f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 11f)
                horizontalLineTo(20f)
                verticalLineTo(13f)
                horizontalLineTo(4f)
                close()
            }
        }.build()
    }

    /** Greek script icon: stylised Alpha (Α) glyph. */
    val Greek: ImageVector by lazy {
        ImageVector.Builder(
            name = "Greek",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 3f)
                lineTo(20f, 21f)
                lineTo(18f, 21f)
                lineTo(15.5f, 15f)
                horizontalLineTo(8.5f)
                lineTo(6f, 21f)
                lineTo(4f, 21f)
                close()
                // Crossbar cutout
                moveTo(9.5f, 13f)
                horizontalLineTo(14.5f)
                lineTo(12f, 7f)
                close()
            }
        }.build()
    }
}
