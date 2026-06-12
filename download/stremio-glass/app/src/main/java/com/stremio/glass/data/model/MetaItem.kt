package com.stremio.glass.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetaItem(
    val id: String = "",
    val type: String = "movie",
    val name: String = "",
    val poster: String = "",
    val background: String = "",
    val logo: String = "",
    val description: String = "",
    val releaseInfo: String = "",
    val runtime: String = "",
    val rating: String = "",
    val year: String = "",
    val imdbRating: String = "",
    val genre: List<String> = emptyList(),
    val cast: List<String> = emptyList(),
    val director: List<String> = emptyList(),
    val writer: List<String> = emptyList(),
    val country: String = "",
    val language: String = "",
    val website: String = "",
    val trailer: String = "",
    val trailers: List<Trailer> = emptyList(),
    val links: List<Link> = emptyList(),
    val videos: List<Video> = emptyList(),
    val behaviorHints: MetaBehaviorHints = MetaBehaviorHints()
)

@Serializable
data class Trailer(
    val source: String = "",
    val type: String = ""
)

@Serializable
data class Link(
    val name: String = "",
    val category: String = "",
    val url: String = ""
)

@Serializable
data class Video(
    val id: String = "",
    val title: String = "",
    val thumbnail: String = "",
    val released: String = "",
    val overview: String = "",
    val season: Int? = null,
    val episode: Int? = null,
    val firstAired: String = "",
    val available: Boolean = true,
    val upcoming: Boolean = false
)

@Serializable
data class MetaBehaviorHints(
    val defaultVideoId: String? = null,
    val featuredVideoId: String? = null
)

@Serializable
data class MetaResult(
    val meta: MetaItem
)

@Serializable
data class CatalogResult(
    val metas: List<MetaItem>
)
