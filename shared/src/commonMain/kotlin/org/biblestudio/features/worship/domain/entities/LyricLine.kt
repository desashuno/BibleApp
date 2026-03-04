package org.biblestudio.features.worship.domain.entities

data class LyricLine(
    val lineIndex: Int,
    val startMs: Long,
    val endMs: Long,
    val text: String
)
