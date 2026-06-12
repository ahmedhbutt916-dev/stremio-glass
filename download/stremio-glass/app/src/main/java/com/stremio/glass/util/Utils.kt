package com.stremio.glass.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * App-wide dispatchers for coroutine usage.
 */
object AppDispatchers {
    val io: CoroutineDispatcher = Dispatchers.IO
    val main: CoroutineDispatcher = Dispatchers.Main
    val default: CoroutineDispatcher = Dispatchers.Default
}

/**
 * Safe API call wrapper that handles exceptions.
 */
suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher = AppDispatchers.io,
    block: suspend () -> T
): Result<T> = try {
    withContext(dispatcher) {
        Result.success(block())
    }
} catch (e: Exception) {
    Result.failure(e)
}

/**
 * Format seconds into HH:MM:SS or MM:SS string.
 */
fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format("%d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}

/**
 * Extract quality label from stream name.
 */
fun getQualityLabel(name: String): String = when {
    name.contains("4K", ignoreCase = true) -> "4K"
    name.contains("2160", ignoreCase = true) -> "4K"
    name.contains("1080", ignoreCase = true) -> "FHD"
    name.contains("720", ignoreCase = true) -> "HD"
    name.contains("480", ignoreCase = true) -> "SD"
    name.contains("CAM", ignoreCase = true) -> "CAM"
    name.contains("HDR", ignoreCase = true) -> "HDR"
    else -> "AUTO"
}

/**
 * Validate a Stremio addon manifest URL.
 */
fun isValidManifestUrl(url: String): Boolean {
    return url.startsWith("http") && url.contains("manifest.json")
}
