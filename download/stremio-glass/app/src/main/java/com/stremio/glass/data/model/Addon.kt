package com.stremio.glass.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Addon(
    val manifestUrl: String = "",
    val manifest: Manifest = Manifest(),
    val installed: Boolean = false,
    val enabled: Boolean = true,
    val order: Int = 0,
    val lastUpdated: Long = 0L
)

@Serializable
data class AddonCollection(
    val addons: List<Addon> = emptyList()
)
