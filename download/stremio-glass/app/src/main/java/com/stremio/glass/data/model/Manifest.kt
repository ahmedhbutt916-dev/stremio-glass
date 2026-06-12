package com.stremio.glass.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Manifest(
    val id: String = "",
    val version: String = "1.0.0",
    val name: String = "",
    val description: String = "",
    val logo: String = "",
    val background: String = "",
    val types: List<String> = emptyList(),
    val catalogs: List<Catalog> = emptyList(),
    val resources: List<String> = emptyList(),
    val idPrefixes: List<String> = emptyList(),
    val behaviorHints: BehaviorHints = BehaviorHints()
)

@Serializable
data class BehaviorHints(
    val adult: Boolean = false,
    val p2p: Boolean = false,
    @SerialName("configurable") val configurable: Boolean = false
)
