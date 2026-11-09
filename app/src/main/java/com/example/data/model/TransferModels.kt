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

data class ActiveTransferProgress(
    val id: String = "",
    val fileName: String = "",
    val bytesTransferred: Long = 0L,
    val totalBytes: Long = 0L,
    val isReceiving: Boolean = true, // true: receiving from PC, false: sending to PC
    val speedMBps: Float = 0f,
    val isActive: Boolean = false
) {
    val progress: Float
        get() = if (totalBytes > 0) (bytesTransferred.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val percentage: Int
        get() = (progress * 100).toInt()

    val formattedTransferred: String
        get() = formatFileSize(bytesTransferred)

    val formattedTotal: String
        get() = formatFileSize(totalBytes)

    val etaSeconds: Int
        get() {
            if (speedMBps <= 0f || totalBytes <= bytesTransferred) return 0
            val remainingMB = (totalBytes - bytesTransferred) / (1024f * 1024f)
            return (remainingMB / speedMBps).toInt().coerceAtLeast(0)
        }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format(java.util.Locale.US, "%.1f %s", value, units[digitGroups])
}
