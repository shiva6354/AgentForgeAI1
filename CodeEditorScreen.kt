package com.agentforge.ai.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agentforge.ai.data.local.ProjectFileManager
import com.agentforge.ai.data.model.Project
import com.agentforge.ai.data.model.ProjectFile
import com.agentforge.ai.domain.repository.GeminiRepository
import com.agentforge.ai.navigation.Screen
import com.agentforge.ai.ui.components.LoadingIndicator
import kotlinx.coroutines.launch

@Composable
fun CodeEditorScreen(
    projectId: String,
    fileManager: ProjectFileManager,
    repository: GeminiRepository,
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    var project by remember { mutableStateOf<Project?>(null) }
    var activeFilePath by remember { mutableStateOf("") }
    var currentContent by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("") }

    // Auto-fix states
    var isFixing by remember { mutableStateOf(false) }
    var activeProposal by remember { mutableStateOf<com.agentforge.ai.data.model.AutoFixProposal?>(null) }
    var fixError by remember { mutableStateOf<String?>(null) }
    var fixSuccessMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(projectId) {
        val loaded = fileManager.getProjectById(projectId)
        if (loaded != null) {
            project = loaded
            activeFilePath = loaded.entryFile.ifEmpty { loaded.files.firstOrNull()?.path ?: "" }
            currentContent = loaded.files.find { it.path == activeFilePath }?.content ?: ""
        }
    }

    if (project == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentProj = project!!

    Column(modifier = Modifier.fillMaxSize()) {
        // Top action bar
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
                    Column {
                        Text(currentProj.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(activeFilePath, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Auto Fix action
                    FilledTonalIconButton(
                        onClick = {
                            isFixing = true
                            fixError = null
                            coroutineScope.launch {
                                val res = repository.autoFix(currentContent, "Syntax validation, optimizations, and bug checking", activeFilePath)
                                isFixing = false
                                res.onSuccess { proposal ->
                                    activeProposal = proposal
                                }.onFailure { err ->
                                    fixError = err.message ?: "Failed to run Auto-Fix"
                                }
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        enabled = !isFixing
                    ) {
                        Icon(Icons.Default.Build, contentDescription = "Auto Fix with Gemini", modifier = Modifier.size(16.dp))
                    }

                    // Save action
                    IconButton(
                        onClick = {
                            isSaving = true
                            coroutineScope.launch {
                                val updatedFiles = currentProj.files.map {
                                    if (it.path == activeFilePath) it.copy(content = currentContent) else it
                                }
                                val updatedProj = currentProj.copy(files = updatedFiles, updatedAt = System.currentTimeMillis())
                                fileManager.saveProject(updatedProj)
                                project = updatedProj
                                isSaving = false
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                    }

                    // Preview action if web
                    if (currentProj.platform == "web") {
                        IconButton(
                            onClick = { navController.navigate(Screen.Preview.createRoute(currentProj.id)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Preview", tint = Color(0xFF10B981))
                        }
                    }
                }
            }
        }

        // File tabs bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(currentProj.files) { file ->
                val isSelected = file.path == activeFilePath
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.clickable {
                        // Switch file after saving current edits in memory
                        val updated = currentProj.files.map {
                            if (it.path == activeFilePath) it.copy(content = currentContent) else it
                        }
                        project = currentProj.copy(files = updated)
                        activeFilePath = file.path
                        currentContent = file.content
                    }
                ) {
                    Text(
                        text = file.path.substringAfterLast('/'),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            item {
                IconButton(onClick = { showNewFileDialog = true }, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add File", modifier = Modifier.size(16.dp))
                }
            }
        }

        if (isFixing) {
            LoadingIndicator("Gemini is analyzing and preparing auto-fix proposals...", modifier = Modifier.padding(horizontal = 12.dp))
        }

        fixError?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(Modifier.width(6.dp))
                    Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                    IconButton(onClick = { fixError = null }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        fixSuccessMessage?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(it, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { fixSuccessMessage = null }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Full Screen Mobile Code Area
        OutlinedTextField(
            value = currentContent,
            onValueChange = { currentContent = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New File", fontSize = 14.sp) },
            text = {
                OutlinedTextField(
                    value = newFileNameInput,
                    onValueChange = { newFileNameInput = it },
                    placeholder = { Text("e.g. Utils.kt or style.css", fontSize = 12.sp) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newFileNameInput.isNotBlank()) {
                        val path = newFileNameInput.trim()
                        val updatedFiles = currentProj.files + ProjectFile(path, "// $path\n")
                        val updated = currentProj.copy(files = updatedFiles)
                        coroutineScope.launch {
                            fileManager.saveProject(updated)
                            project = updated
                            activeFilePath = path
                            currentContent = "// $path\n"
                        }
                        newFileNameInput = ""
                        showNewFileDialog = false
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Auto-Fix Proposed Changes Review Dialog (Requirement 9)
    activeProposal?.let { proposal ->
        AlertDialog(
            onDismissRequest = { activeProposal = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Proposed Auto-Fix", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "File: ${if (proposal.filePath.isNotBlank()) proposal.filePath else activeFilePath}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (proposal.rootCause.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Root Cause:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(proposal.rootCause, fontSize = 11.sp, lineHeight = 15.sp)
                            }
                        }
                    }

                    Text(proposal.explanation, fontSize = 12.sp, lineHeight = 16.sp)

                    Text("Proposed Code Fix:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF030712),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        Text(
                            text = proposal.fixedCode,
                            color = Color(0xFFE5E7EB),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(8.dp),
                            maxLines = 12
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fixed = proposal.fixedCode
                        currentContent = fixed
                        val updatedFiles = currentProj.files.map {
                            if (it.path == activeFilePath) it.copy(content = fixed) else it
                        }
                        val updatedProj = currentProj.copy(files = updatedFiles, updatedAt = System.currentTimeMillis())
                        coroutineScope.launch {
                            fileManager.saveProject(updatedProj)
                            project = updatedProj
                        }
                        fixSuccessMessage = "Auto-fix applied and saved successfully!"
                        activeProposal = null
                    }
                ) {
                    Text("Apply Fix")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeProposal = null }) {
                    Text("Discard")
                }
            }
        )
    }
}
