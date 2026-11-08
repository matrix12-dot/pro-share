package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TransferViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TransferViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val serverStatus by viewModel.serverStatus.collectAsStateWithLifecycle()
                    val sharedFiles by viewModel.sharedFiles.collectAsStateWithLifecycle()
                    val receivedFiles by viewModel.receivedFiles.collectAsStateWithLifecycle()
                    val clipboardText by viewModel.clipboardText.collectAsStateWithLifecycle()
                    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
                    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
                    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

                    val context = LocalContext.current
                    val snackbarHostState = remember { SnackbarHostState() }
                    var showQrDialog by remember { mutableStateOf(false) }

                    // Launchers for zero-permission photo picker and document picker
                    val photoPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickMultipleVisualMedia()
                    ) { uris ->
                        if (uris.isNotEmpty()) {
                            viewModel.addFilesFromUris(uris)
                        }
                    }

                    val documentPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenMultipleDocuments()
                    ) { uris ->
                        if (uris.isNotEmpty()) {
                            viewModel.addFilesFromUris(uris)
                        }
                    }

                    // Handle toast/snackbar message
                    LaunchedEffect(userMessage) {
                        userMessage?.let { msg ->
                            snackbarHostState.showSnackbar(msg)
                            viewModel.clearUserMessage()
                        }
                    }

                    // QR Dialog
                    if (showQrDialog) {
                        QrCodeDialog(
                            url = serverStatus.serverUrl,
                            onDismiss = { showQrDialog = false },
                            onCopyUrl = {
                                copyTextToClipboard(context, serverStatus.serverUrl)
                                Toast.makeText(context, "تم نسخ الرابط", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("main_scaffold"),
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_app_logo_1794157712038),
                                            contentDescription = "شعار SwiftShare",
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Text(
                                            text = "SwiftShare",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { showQrDialog = true },
                                        modifier = Modifier.testTag("appbar_qr_button")
                                    ) {
                                        Icon(
                                            Icons.Default.QrCode,
                                            contentDescription = "عرض رمز QR",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                NavigationBarItem(
                                    selected = activeTab == 0,
                                    onClick = { viewModel.setActiveTab(0) },
                                    icon = {
                                        BadgedBox(badge = {
                                            if (sharedFiles.isNotEmpty()) {
                                                Badge { Text(sharedFiles.size.toString()) }
                                            }
                                        }) {
                                            Icon(Icons.Default.Upload, contentDescription = "إرسال للحاسوب")
                                        }
                                    },
                                    label = { Text("إرسال", fontSize = 12.sp) },
                                    modifier = Modifier.testTag("tab_send")
                                )
                                NavigationBarItem(
                                    selected = activeTab == 1,
                                    onClick = { viewModel.setActiveTab(1) },
                                    icon = {
                                        BadgedBox(badge = {
                                            if (receivedFiles.isNotEmpty()) {
                                                Badge { Text(receivedFiles.size.toString()) }
                                            }
                                        }) {
                                            Icon(Icons.Default.Download, contentDescription = "المستلم")
                                        }
                                    },
                                    label = { Text("المستلم", fontSize = 12.sp) },
                                    modifier = Modifier.testTag("tab_received")
                                )
                                NavigationBarItem(
                                    selected = activeTab == 2,
                                    onClick = { viewModel.setActiveTab(2) },
                                    icon = {
                                        Icon(Icons.Default.ContentPaste, contentDescription = "الحافظة")
                                    },
                                    label = { Text("الحافظة", fontSize = 12.sp) },
                                    modifier = Modifier.testTag("tab_clipboard")
                                )
                                NavigationBarItem(
                                    selected = activeTab == 3,
                                    onClick = { viewModel.setActiveTab(3) },
                                    icon = {
                                        Icon(Icons.Default.Info, contentDescription = "دليل وسرعة")
                                    },
                                    label = { Text("دليل وسرعة", fontSize = 12.sp) },
                                    modifier = Modifier.testTag("tab_help")
                                )
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Top Persistent Connection Card
                            ConnectionCard(
                                status = serverStatus,
                                onToggleServer = { viewModel.toggleServer() },
                                onCopyUrl = {
                                    copyTextToClipboard(context, serverStatus.serverUrl)
                                    Toast.makeText(context, "تم نسخ الرابط للحافظة", Toast.LENGTH_SHORT).show()
                                },
                                onShowQrCode = { showQrDialog = true },
                                onRefreshNetwork = { viewModel.refreshNetworkInfo() },
                                onTogglePin = { viewModel.togglePinSecurity() },
                                onGenerateNewPin = { viewModel.generateNewPin() }
                            )

                            // Tabs Content
                            AnimatedContent(
                                targetState = activeTab,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "tabTransition"
                            ) { tab ->
                                when (tab) {
                                    0 -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            CategoryFilterRow(
                                                selectedCategory = selectedCategory,
                                                onCategorySelected = { viewModel.setCategory(it) }
                                            )
                                            StagedFilesList(
                                                files = sharedFiles,
                                                selectedCategory = selectedCategory,
                                                onRemoveFile = { viewModel.removeSharedFile(it) },
                                                onClearAll = { viewModel.clearAllSharedFiles() },
                                                onPickPhotos = {
                                                    photoPickerLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                                    )
                                                },
                                                onPickFiles = {
                                                    documentPickerLauncher.launch(arrayOf("*/*"))
                                                }
                                            )
                                        }
                                    }
                                    1 -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            CategoryFilterRow(
                                                selectedCategory = selectedCategory,
                                                onCategorySelected = { viewModel.setCategory(it) }
                                            )
                                            ReceivedFilesList(
                                                files = receivedFiles,
                                                selectedCategory = selectedCategory,
                                                onOpenFile = { viewModel.openReceivedFile(it) },
                                                onShareFile = { viewModel.shareReceivedFile(it) },
                                                onDeleteFile = { viewModel.deleteReceivedFile(it) }
                                            )
                                        }
                                    }
                                    2 -> {
                                        ClipboardSyncCard(
                                            clipboardText = clipboardText,
                                            onSendText = { viewModel.updateClipboard(it) }
                                        )
                                    }
                                    3 -> {
                                        HelpTipsCard()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun copyTextToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("SwiftShare URL", text)
        clipboard?.setPrimaryClip(clip)
    }
}
