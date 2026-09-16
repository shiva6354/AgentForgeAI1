package com.agentforge.ai.ui.builder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.agentforge.ai.navigation.Screen
import com.agentforge.ai.ui.components.AgentForgeButton
import com.agentforge.ai.ui.components.AgentForgeCard
import com.agentforge.ai.ui.components.ErrorMessage
import com.agentforge.ai.ui.components.LoadingIndicator

@Composable
fun BuilderScreen(
    viewModel: BuilderViewModel,
    navController: NavController
) {
    val project by viewModel.project.collectAsState()
    val activeFilePath by viewModel.activeFilePath.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedPlatform by viewModel.selectedPlatform.collectAsState()

    var promptInput by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("code") } // "files", "code", "preview"
    var editableCode by remember { mutableStateOf("") }

    val currentFile = project.files.find { it.path == activeFilePath } ?: project.files.firstOrNull()

    LaunchedEffect(currentFile) {
        editableCode = currentFile?.content ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Card: Prompt Generator Box
        AgentForgeCard(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("AI Project Builder", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Gemini 3.8 Flash Code Synthesizer", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }

                // Platform Switch
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(2.dp)
                ) {
                    FilterChip(
                        selected = selectedPlatform == "android",
                        onClick = { viewModel.setPlatform("android") },
                        label = { Text("Android", fontSize = 10.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    FilterChip(
                        selected = selectedPlatform == "web",
                        onClick = { viewModel.setPlatform("web") },
                        label = { Text("Web", fontSize = 10.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = {
                    Text(
                        if (selectedPlatform == "android") "e.g. Build an Android Compose Audio Player with playlist..."
                        else "e.g. Build a mobile food delivery app with menu and cart...",
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AgentForgeButton(
                    text = if (isGenerating) "Generating..." else "Generate Project",
                    onClick = { viewModel.generateProject(promptInput) },
                    enabled = !isGenerating && promptInput.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        if (isGenerating) {
            LoadingIndicator("Synthesizing multi-file codebase...")
        }

        error?.let {
            ErrorMessage(message = it, onRetry = { viewModel.generateProject(promptInput) })
        }

        // Project Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(project.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${project.platform.uppercase()} • ${project.files.size} Files", fontSize = 11.sp, color = Color.Gray)
            }

            // Tab Selector: Files | Code | Preview
            TabRow(
                selectedTabIndex = when (activeTab) {
                    "files" -> 0
                    "code" -> 1
                    else -> 2
                },
                modifier = Modifier.width(220.dp),
                containerColor = Color.Transparent
            ) {
                Tab(selected = activeTab == "files", onClick = { activeTab = "files" }, text = { Text("Files", fontSize = 11.sp) })
                Tab(selected = activeTab == "code", onClick = { activeTab = "code" }, text = { Text("Code", fontSize = 11.sp) })
                Tab(selected = activeTab == "preview", onClick = {
                    navController.navigate(Screen.Preview.createRoute(project.id))
                }, text = { Text("Preview", fontSize = 11.sp) })
            }
        }

        // Active Tab Body
        when (activeTab) {
            "files" -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(project.files) { file ->
                        val isSelected = file.path == activeFilePath
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectFile(file.path)
                                    activeTab = "code"
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (file.path.endsWith(".kt")) Icons.Default.Code else Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(file.path, fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                                Text("${file.content.lines().size} L", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            "code" -> {
                Column(modifier = Modifier.weight(1f)) {
                    // File path label + Save button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentFile?.path ?: "No file selected",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    currentFile?.let { viewModel.updateFileContent(it.path, editableCode) }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Save", fontSize = 10.sp)
                            }
                        }
                    }

                    // Mobile-friendly Code Editor TextField
                    OutlinedTextField(
                        value = editableCode,
                        onValueChange = { editableCode = it },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}
