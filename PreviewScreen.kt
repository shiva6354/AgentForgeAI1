package com.agentforge.ai.ui.preview

import android.annotation.SuppressLint
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.agentforge.ai.data.local.ProjectFileManager
import com.agentforge.ai.data.model.Project

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PreviewScreen(
    projectId: String,
    fileManager: ProjectFileManager,
    navController: NavController
) {
    var project by remember { mutableStateOf<Project?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var consoleLogs by remember { mutableStateOf<List<String>>(emptyList()) }
    var showConsole by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) {
        project = fileManager.getProjectById(projectId)
    }

    if (project == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentProj = project!!
    val entryFile = currentProj.files.find { it.path == currentProj.entryFile || it.path == "index.html" }
        ?: currentProj.files.firstOrNull()
    val htmlContent = entryFile?.content ?: "<h1>No entry HTML file found</h1>"

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Toolbar
        Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Live Sandbox: ${currentProj.name}",
                        fontSize = 13.sp,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { showConsole = !showConsole }) {
                        Text(if (showConsole) "Hide Logs" else "Logs (${consoleLogs.size})", fontSize = 11.sp)
                    }
                    IconButton(
                        onClick = {
                            webViewRef?.loadDataWithBaseURL("https://local.agentforge/", htmlContent, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload")
                    }
                }
            }
        }

        // Live WebView sandbox
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                                msg?.let {
                                    consoleLogs = consoleLogs + "[${it.messageLevel()}] ${it.message()}"
                                }
                                return super.onConsoleMessage(msg)
                            }
                        }

                        webViewClient = WebViewClient()
                        loadDataWithBaseURL("https://local.agentforge/", htmlContent, "text/html", "UTF-8", null)
                        webViewRef = this
                    }
                },
                update = { webView ->
                    webViewRef = webView
                }
            )
        }

        // Optional bottom console logs drawer
        if (showConsole) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFF030712))
                    .padding(8.dp)
            ) {
                Text("SANDBOX CONSOLE LOGS", fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(4.dp))
                consoleLogs.takeLast(6).forEach { log ->
                    Text(log, fontSize = 10.sp, color = Color(0xFFE5E7EB), fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
