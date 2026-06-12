package com.stremio.glass.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Stream(
    val url: String = "",
    val title: String = "",
    val name: String = "",
    val description: String = "",
    val ytId: String = "",
    val externalUrl: String = "",
    val behaviorHints: StreamBehaviorHints = StreamBehaviorHints(),
    val infoHash: String = "",
    val fileIdx: Int? = null,
    val sources: List<String> = emptyList()
)

@Serializable
data class StreamBehaviorHints(
    val filename: String = "",
    val headers: Map<String, String> = emptyMap(),
    val proxyHeaders: ProxyHeaders = ProxyHeaders(),
    val notWebReady: Boolean = false,
    val bingeGroup: String = "",
    val group: String = ""
)

@Serializable
data class ProxyHeaders(
    val request: Map<String, String> = emptyMap(),
    val response: Map<String, String> = emptyMap()
)

@Serializable
data class StreamResult(
    val streams: List<Stream>
)
