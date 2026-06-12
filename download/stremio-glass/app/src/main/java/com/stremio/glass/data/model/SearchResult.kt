package com.stremio.glass.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val query: String = "",
    val results: List<MetaItem> = emptyList()
)
