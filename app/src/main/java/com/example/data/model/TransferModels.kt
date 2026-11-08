package com.example.data.model

import android.net.Uri

enum class FileCategory(val labelArabic: String, val labelEnglish: String) {
    ALL("الكل", "All"),
    PHOTO("صور", "Photos"),
    VIDEO("فيديو", "Videos"),
    AUDIO("صوتيات", "Audio"),
    DOCUMENT("مستندات", "Documents"),
    OTHER("أخرى", "Other");

    companion object {
        fun fromMime(mime: String): FileCategory {
            return when {
                mime.startsWith("image/") -> PHOTO
                mime.startsWith("video/") -> VIDEO
                mime.startsWith("audio/") -> AUDIO
                mime.contains("pdf") || mime.contains("document") || mime.contains("sheet") ||
                        mime.contains("text") || mime.contains("msword") || mime.contains("presentation") -> DOCUMENT
                else -> OTHER
            }
        }
    }
}

data class SharedFile(
    val id: String,
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String,
    val category: FileCategory = FileCategory.fromMime(mimeType),
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedSize: String
        get() = formatFileSize(sizeBytes)
}

data class ReceivedFile(
    val id: String,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String,
    val filePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: FileCategory = FileCategory.fromMime(mimeType)
) {
    val formattedSize: String
        get() = formatFileSize(sizeBytes)
}

data class ServerStatus(
    val isRunning: Boolean = false,
    val ipAddress: String = "127.0.0.1",
    val port: Int = 8080,
    val pinCode: String = "1234",
    val isPinEnabled: Boolean = false,
    val networkSsid: String = "Wi-Fi",
    val isHotspot: Boolean = false,
    val connectedClients: Int = 0,
    val bytesSent: Long = 0L,
    val bytesReceived: Long = 0L,
    val currentSpeedMbps: Float = 0f
) {
    val serverUrl: String
        get() = "http://$ipAddress:$port"
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format(java.util.Locale.US, "%.1f %s", value, units[digitGroups])
}
