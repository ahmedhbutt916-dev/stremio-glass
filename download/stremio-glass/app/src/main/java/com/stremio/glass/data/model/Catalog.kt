package com.stremio.glass.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Catalog(
    val id: String = "",
    val type: String = "movie",
    val name: String = "",
    val extra: List<CatalogExtra> = emptyList()
)

@Serializable
data class CatalogExtra(
    val name: String = "",
    val isRequired: Boolean = false,
    val options: List<String> = emptyList()
)
