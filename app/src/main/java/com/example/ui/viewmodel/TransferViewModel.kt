package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FileCategory
import com.example.data.model.ReceivedFile
import com.example.data.model.ServerStatus
import com.example.data.model.SharedFile
import com.example.server.HttpServerEngine
import com.example.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

class TransferViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _serverStatus = MutableStateFlow(ServerStatus())
    val serverStatus = _serverStatus.asStateFlow()

    private val _sharedFiles = MutableStateFlow<List<SharedFile>>(emptyList())
    val sharedFiles = _sharedFiles.asStateFlow()

    private val _receivedFiles = MutableStateFlow<List<ReceivedFile>>(emptyList())
    val receivedFiles = _receivedFiles.asStateFlow()

    private val _clipboardText = MutableStateFlow("")
    val clipboardText = _clipboardText.asStateFlow()

    private val _selectedCategory = MutableStateFlow(FileCategory.ALL)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _activeTab = MutableStateFlow(0) // 0: Send to PC, 1: Received, 2: Clipboard, 3: Help
    val activeTab = _activeTab.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    private var serverEngine: HttpServerEngine? = null

    init {
        loadExistingReceivedFiles()
        initServer()
        startSpeedMonitor()
    }

    private fun initServer() {
        serverEngine = HttpServerEngine(
            context = context,
            onFileReceived = { file ->
                viewModelScope.launch(Dispatchers.Main) {
                    _receivedFiles.update { listOf(file) + it }
                    _userMessage.value = "تم استلام ملف جديد من الحاسوب: ${file.name}"
                }
            },
            onClipboardUpdated = { text ->
                viewModelScope.launch(Dispatchers.Main) {
                    _clipboardText.value = text
                    copyToSystemClipboard(text)
                    _userMessage.value = "تم تحديث الحافظة من الحاسوب"
                }
            },
            getSharedFiles = { _sharedFiles.value },
            getClipboardText = { _clipboardText.value },
            getPinCode = { Pair(_serverStatus.value.isPinEnabled, _serverStatus.value.pinCode) }
        )
        // Auto-start server in background on Dispatchers.IO
        startServer()
    }

    fun refreshNetworkInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            refreshNetworkInfoSync()
        }
    }

    private fun refreshNetworkInfoSync() {
        try {
            val ip = NetworkUtils.getLocalIpAddress()
            val (networkName, isHotspot) = NetworkUtils.getNetworkName(context)
            _serverStatus.update {
                it.copy(
                    ipAddress = ip,
                    networkSsid = networkName,
                    isHotspot = isHotspot
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startServer() {
        viewModelScope.launch(Dispatchers.IO) {
            refreshNetworkInfoSync()
            val preferredPort = _serverStatus.value.port
            val boundPort = serverEngine?.start(preferredPort) ?: -1
            if (boundPort > 0) {
                _serverStatus.update { it.copy(isRunning = true, port = boundPort) }
            } else {
                _serverStatus.update { it.copy(isRunning = false) }
            }
        }
    }

    fun stopServer() {
        viewModelScope.launch(Dispatchers.IO) {
            serverEngine?.stop()
            _serverStatus.update { it.copy(isRunning = false, connectedClients = 0, currentSpeedMbps = 0f) }
        }
    }

    fun toggleServer() {
        if (_serverStatus.value.isRunning) {
            stopServer()
        } else {
            startServer()
        }
    }

    fun togglePinSecurity() {
        _serverStatus.update { it.copy(isPinEnabled = !it.isPinEnabled) }
    }

    fun generateNewPin() {
        val newPin = String.format("%04d", Random.nextInt(1000, 9999))
        _serverStatus.update { it.copy(pinCode = newPin) }
    }

    fun setActiveTab(index: Int) {
        _activeTab.value = index
    }

    fun setCategory(category: FileCategory) {
        _selectedCategory.value = category
    }

    fun addFilesFromUris(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val newFiles = mutableListOf<SharedFile>()
            for (uri in uris) {
                val metadata = queryUriMetadata(uri)
                val id = "file_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
                newFiles.add(
                    SharedFile(
                        id = id,
                        uri = uri,
                        name = metadata.first,
                        sizeBytes = metadata.second,
                        mimeType = metadata.third
                    )
                )
            }
            _sharedFiles.update { it + newFiles }
            _userMessage.value = "تمت إضافة ${newFiles.size} ملفات، جاهزة للتنزيل على الحاسوب!"
        }
    }

    fun removeSharedFile(fileId: String) {
        _sharedFiles.update { it.filterNot { f -> f.id == fileId } }
    }

    fun clearAllSharedFiles() {
        _sharedFiles.value = emptyList()
    }

    fun deleteReceivedFile(file: ReceivedFile) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val f = File(file.filePath)
                if (f.exists()) f.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _receivedFiles.update { it.filterNot { rf -> rf.id == file.id } }
        }
    }

    fun openReceivedFile(file: ReceivedFile) {
        try {
            val localFile = File(file.filePath)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                localFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, file.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _userMessage.value = "لا يوجد تطبيق مناسب لفتح هذا الملف"
        }
    }

    fun shareReceivedFile(file: ReceivedFile) {
        try {
            val localFile = File(file.filePath)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                localFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = file.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة الملف").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            _userMessage.value = "تعذر مشاركة الملف"
        }
    }

    fun updateClipboard(text: String) {
        _clipboardText.value = text
        copyToSystemClipboard(text)
        _userMessage.value = "تم نسخ النص وإرساله للحاسوب"
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    private fun copyToSystemClipboard(text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("SwiftShare", text)
            clipboard?.setPrimaryClip(clip)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadExistingReceivedFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = File(context.filesDir, "received")
            if (dir.exists()) {
                val files = dir.listFiles()?.filter { it.isFile }?.map { f ->
                    val mime = getMimeFromName(f.name)
                    ReceivedFile(
                        id = f.name,
                        name = f.name,
                        sizeBytes = f.length(),
                        mimeType = mime,
                        filePath = f.absolutePath,
                        timestamp = f.lastModified()
                    )
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                _receivedFiles.value = files
            }
        }
    }

    private fun startSpeedMonitor() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(1000)
                val engine = serverEngine
                if (engine != null && _serverStatus.value.isRunning) {
                    val bytesLastSec = engine.recentTransferredBytes.getAndSet(0L)
                    val speedMbps = (bytesLastSec * 8f) / (1024f * 1024f) // Megabits per sec
                    val clientsCount = engine.activeConnections.size
                    _serverStatus.update {
                        it.copy(
                            bytesSent = engine.totalBytesSent.get(),
                            bytesReceived = engine.totalBytesReceived.get(),
                            currentSpeedMbps = speedMbps,
                            connectedClients = clientsCount
                        )
                    }
                }
            }
        }
    }

    private fun queryUriMetadata(uri: Uri): Triple<String, Long, String> {
        var name = "file_${System.currentTimeMillis()}"
        var size = 0L
        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                    if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Triple(name, size, mime)
    }

    private fun getMimeFromName(name: String): String {
        val dot = name.lastIndexOf('.')
        if (dot == -1) return "application/octet-stream"
        return when (name.substring(dot + 1).lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            "pdf" -> "application/pdf"
            "zip" -> "application/zip"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }

    override fun onCleared() {
        super.onCleared()
        serverEngine?.stop()
    }
}
