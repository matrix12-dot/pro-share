package com.example.server

import android.content.Context
import android.net.Uri
import com.example.data.model.ReceivedFile
import com.example.data.model.SharedFile
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class HttpServerEngine(
    private val context: Context,
    private val onFileReceived: (ReceivedFile) -> Unit,
    private val onClipboardUpdated: (String) -> Unit,
    private val getSharedFiles: () -> List<SharedFile>,
    private val getClipboardText: () -> String,
    private val getPinCode: () -> Pair<Boolean, String> // (isPinEnabled, pinCode)
) {
    private var serverSocket: ServerSocket? = null
    private val isRunning = AtomicBoolean(false)
    private val threadPool = Executors.newCachedThreadPool()

    val totalBytesSent = AtomicLong(0L)
    val totalBytesReceived = AtomicLong(0L)
    val recentTransferredBytes = AtomicLong(0L)
    val activeConnections = ConcurrentHashMap.newKeySet<Socket>()

    var actualPort: Int = 8080
        private set

    fun start(preferredPort: Int = 8080): Int {
        if (isRunning.get()) return actualPort
        try {
            val candidatePorts = intArrayOf(preferredPort, 8080, 8081, 8082, 8888, 8000, 0)
            var boundSocket: ServerSocket? = null
            for (p in candidatePorts) {
                try {
                    val ss = ServerSocket()
                    ss.reuseAddress = true
                    ss.bind(java.net.InetSocketAddress(p))
                    boundSocket = ss
                    actualPort = ss.localPort
                    break
                } catch (e: Exception) {
                    // Try next port
                }
            }

            if (boundSocket == null) {
                return -1
            }

            serverSocket = boundSocket
            isRunning.set(true)

            threadPool.execute {
                while (isRunning.get()) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        activeConnections.add(clientSocket)
                        threadPool.execute {
                            try {
                                handleClient(clientSocket)
                            } catch (e: Exception) {
                                // Socket closed or client disconnected
                            } finally {
                                activeConnections.remove(clientSocket)
                                try {
                                    clientSocket.close()
                                } catch (_: Exception) {}
                            }
                        }
                    } catch (e: Exception) {
                        if (!isRunning.get()) break
                    }
                }
            }
            return actualPort
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
    }

    fun stop() {
        isRunning.set(false)
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
        for (sock in activeConnections) {
            try { sock.close() } catch (_: Exception) {}
        }
        activeConnections.clear()
    }

    private fun handleClient(socket: Socket) {
        val inputStream = BufferedInputStream(socket.getInputStream())
        val outputStream = BufferedOutputStream(socket.getOutputStream())

        // Read Request Line
        val requestLine = readLine(inputStream) ?: return
        val parts = requestLine.split(" ")
        if (parts.size < 2) return

        val method = parts[0]
        val rawUri = parts[1]

        // Read Headers
        val headers = mutableMapOf<String, String>()
        while (true) {
            val line = readLine(inputStream) ?: break
            if (line.isEmpty()) break
            val colonIdx = line.indexOf(':')
            if (colonIdx > 0) {
                val key = line.substring(0, colonIdx).trim().lowercase()
                val value = line.substring(colonIdx + 1).trim()
                headers[key] = value
            }
        }

        val (path, queryParams) = parseUri(rawUri)

        // PIN Security Check
        val (isPinEnabled, currentPin) = getPinCode()
        if (isPinEnabled && path.startsWith("/api/") && path != "/api/verify-pin") {
            val authToken = headers["x-auth-token"] ?: queryParams["token"]
            if (authToken != currentPin) {
                sendJsonResponse(outputStream, 401, """{"error":"PIN required"}""")
                return
            }
        }

        when {
            method == "GET" && (path == "/" || path == "/index.html") -> {
                sendHtmlResponse(outputStream, WebPortalHtml.getHtml())
            }

            method == "GET" && path == "/api/files" -> {
                val files = getSharedFiles()
                val json = buildFilesJson(files)
                sendJsonResponse(outputStream, 200, json)
            }

            method == "GET" && path == "/api/download" -> {
                val fileId = queryParams["id"]
                val sharedFile = getSharedFiles().find { it.id == fileId }
                if (sharedFile != null) {
                    streamFileDownload(outputStream, sharedFile)
                } else {
                    sendNotFound(outputStream)
                }
            }

            method == "GET" && path == "/download-all" -> {
                val files = getSharedFiles()
                if (files.isNotEmpty()) {
                    streamZipDownload(outputStream, files)
                } else {
                    sendJsonResponse(outputStream, 400, """{"error":"No files"}""")
                }
            }

            method == "POST" && path == "/api/upload" -> {
                val rawName = queryParams["filename"] ?: "upload_${System.currentTimeMillis()}.bin"
                val filename = URLDecoder.decode(rawName, "UTF-8")
                val contentLength = headers["content-length"]?.toLongOrNull() ?: -1L
                handleFileUpload(inputStream, outputStream, filename, contentLength)
            }

            method == "GET" && path == "/api/clipboard" -> {
                val text = getClipboardText()
                sendTextResponse(outputStream, 200, text)
            }

            method == "POST" && path == "/api/clipboard" -> {
                val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
                val body = if (contentLength > 0) {
                    val bytes = ByteArray(contentLength)
                    var read = 0
                    while (read < contentLength) {
                        val r = inputStream.read(bytes, read, contentLength - read)
                        if (r <= 0) break
                        read += r
                    }
                    String(bytes, 0, read, Charsets.UTF_8)
                } else {
                    ""
                }
                onClipboardUpdated(body)
                sendJsonResponse(outputStream, 200, """{"status":"ok"}""")
            }

            method == "GET" && path == "/api/verify-pin" -> {
                val pinParam = queryParams["pin"] ?: ""
                if (pinParam == currentPin) {
                    sendJsonResponse(outputStream, 200, """{"status":"ok","token":"$currentPin"}""")
                } else {
                    sendJsonResponse(outputStream, 403, """{"error":"Invalid PIN"}""")
                }
            }

            else -> {
                sendNotFound(outputStream)
            }
        }
    }

    private fun streamFileDownload(outputStream: OutputStream, file: SharedFile) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(file.uri) ?: run {
                sendNotFound(outputStream)
                return
            }

            val safeFilename = file.name.replace("\"", "\\\"")
            val headers = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: ${file.mimeType}\r\n" +
                    "Content-Length: ${file.sizeBytes}\r\n" +
                    "Content-Disposition: attachment; filename=\"$safeFilename\"\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "Connection: close\r\n\r\n"

            outputStream.write(headers.toByteArray(Charsets.UTF_8))
            outputStream.flush()

            val buffer = ByteArray(64 * 1024) // 64 KB buffer for high LAN throughput
            var bytesRead: Int
            inputStream.use { inStream ->
                while (inStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesSent.addAndGet(bytesRead.toLong())
                    recentTransferredBytes.addAndGet(bytesRead.toLong())
                }
            }
            outputStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun streamZipDownload(outputStream: OutputStream, files: List<SharedFile>) {
        try {
            val headers = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/zip\r\n" +
                    "Content-Disposition: attachment; filename=\"SwiftShare_${System.currentTimeMillis()}.zip\"\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "Connection: close\r\n\r\n"

            outputStream.write(headers.toByteArray(Charsets.UTF_8))
            outputStream.flush()

            val zipOut = ZipOutputStream(BufferedOutputStream(outputStream))
            val buffer = ByteArray(64 * 1024)

            for (file in files) {
                try {
                    context.contentResolver.openInputStream(file.uri)?.use { inStream ->
                        val entry = ZipEntry(file.name)
                        zipOut.putNextEntry(entry)
                        var read: Int
                        while (inStream.read(buffer).also { read = it } != -1) {
                            zipOut.write(buffer, 0, read)
                            totalBytesSent.addAndGet(read.toLong())
                            recentTransferredBytes.addAndGet(read.toLong())
                        }
                        zipOut.closeEntry()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            zipOut.finish()
            zipOut.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleFileUpload(
        inputStream: InputStream,
        outputStream: OutputStream,
        filename: String,
        contentLength: Long
    ) {
        try {
            val downloadsDir = File(context.filesDir, "received").apply { mkdirs() }
            val uniqueFile = getUniqueFile(downloadsDir, filename)
            val fileOut = BufferedOutputStream(FileOutputStream(uniqueFile))

            val buffer = ByteArray(64 * 1024)
            var remaining = if (contentLength > 0) contentLength else Long.MAX_VALUE
            var totalWritten = 0L

            fileOut.use { out ->
                while (remaining > 0) {
                    val toRead = if (contentLength > 0) minOf(buffer.size.toLong(), remaining).toInt() else buffer.size
                    val read = inputStream.read(buffer, 0, toRead)
                    if (read == -1) break
                    out.write(buffer, 0, read)
                    totalWritten += read
                    totalBytesReceived.addAndGet(read.toLong())
                    recentTransferredBytes.addAndGet(read.toLong())
                    if (contentLength > 0) remaining -= read
                }
            }

            val mimeType = getMimeType(uniqueFile.name)
            val received = ReceivedFile(
                id = uniqueFile.name,
                name = uniqueFile.name,
                sizeBytes = totalWritten,
                mimeType = mimeType,
                filePath = uniqueFile.absolutePath,
                timestamp = System.currentTimeMillis()
            )

            onFileReceived(received)
            sendJsonResponse(outputStream, 200, """{"status":"success","filename":"${uniqueFile.name}","size":$totalWritten}""")
        } catch (e: Exception) {
            e.printStackTrace()
            sendJsonResponse(outputStream, 500, """{"error":"Upload failed: ${e.message}"}""")
        }
    }

    private fun getUniqueFile(dir: File, name: String): File {
        var file = File(dir, name)
        if (!file.exists()) return file

        val dotIdx = name.lastIndexOf('.')
        val base = if (dotIdx > 0) name.substring(0, dotIdx) else name
        val ext = if (dotIdx > 0) name.substring(dotIdx) else ""

        var counter = 1
        while (file.exists()) {
            file = File(dir, "${base}_$counter$ext")
            counter++
        }
        return file
    }

    private fun buildFilesJson(files: List<SharedFile>): String {
        val sb = StringBuilder("{\"files\":[")
        files.forEachIndexed { i, f ->
            if (i > 0) sb.append(",")
            val safeName = f.name.replace("\"", "\\\"")
            sb.append("{\"id\":\"${f.id}\",\"name\":\"$safeName\",\"sizeBytes\":${f.sizeBytes},\"formattedSize\":\"${f.formattedSize}\",\"mimeType\":\"${f.mimeType}\"}")
        }
        sb.append("]}")
        return sb.toString()
    }

    private fun sendHtmlResponse(outputStream: OutputStream, html: String) {
        val bytes = html.toByteArray(Charsets.UTF_8)
        val headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n"
        outputStream.write(headers.toByteArray(Charsets.UTF_8))
        outputStream.write(bytes)
        outputStream.flush()
    }

    private fun sendJsonResponse(outputStream: OutputStream, status: Int, json: String) {
        val bytes = json.toByteArray(Charsets.UTF_8)
        val statusText = if (status == 200) "OK" else if (status == 401) "Unauthorized" else if (status == 403) "Forbidden" else "Error"
        val headers = "HTTP/1.1 $status $statusText\r\n" +
                "Content-Type: application/json; charset=UTF-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n"
        outputStream.write(headers.toByteArray(Charsets.UTF_8))
        outputStream.write(bytes)
        outputStream.flush()
    }

    private fun sendTextResponse(outputStream: OutputStream, status: Int, text: String) {
        val bytes = text.toByteArray(Charsets.UTF_8)
        val headers = "HTTP/1.1 $status OK\r\n" +
                "Content-Type: text/plain; charset=UTF-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n"
        outputStream.write(headers.toByteArray(Charsets.UTF_8))
        outputStream.write(bytes)
        outputStream.flush()
    }

    private fun sendNotFound(outputStream: OutputStream) {
        val msg = "404 Not Found"
        val bytes = msg.toByteArray(Charsets.UTF_8)
        val headers = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Connection: close\r\n\r\n"
        outputStream.write(headers.toByteArray(Charsets.UTF_8))
        outputStream.write(bytes)
        outputStream.flush()
    }

    private fun readLine(inputStream: InputStream): String? {
        val sb = StringBuilder()
        var prev = -1
        while (true) {
            val ch = inputStream.read()
            if (ch == -1) {
                if (sb.isEmpty()) return null
                break
            }
            if (ch == '\n'.code) {
                break
            }
            if (ch != '\r'.code) {
                sb.append(ch.toChar())
            }
            prev = ch
        }
        return sb.toString()
    }

    private fun parseUri(uri: String): Pair<String, Map<String, String>> {
        val qIdx = uri.indexOf('?')
        if (qIdx == -1) return Pair(uri, emptyMap())
        val path = uri.substring(0, qIdx)
        val queryString = uri.substring(qIdx + 1)
        val params = mutableMapOf<String, String>()
        for (pair in queryString.split("&")) {
            val eqIdx = pair.indexOf('=')
            if (eqIdx > 0) {
                val key = URLDecoder.decode(pair.substring(0, eqIdx), "UTF-8")
                val value = URLDecoder.decode(pair.substring(eqIdx + 1), "UTF-8")
                params[key] = value
            }
        }
        return Pair(path, params)
    }

    private fun getMimeType(name: String): String {
        val dotIdx = name.lastIndexOf('.')
        if (dotIdx == -1) return "application/octet-stream"
        return when (name.substring(dotIdx + 1).lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "pdf" -> "application/pdf"
            "zip" -> "application/zip"
            "txt" -> "text/plain"
            "json" -> "application/json"
            else -> "application/octet-stream"
        }
    }
}
