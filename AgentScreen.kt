package com.agentforge.ai.ui.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agentforge.ai.navigation.Screen
import com.agentforge.ai.ui.components.AgentForgeButton
import com.agentforge.ai.ui.components.AgentForgeCard
import com.agentforge.ai.ui.components.ErrorMessage

@Composable
fun AgentScreen(
    viewModel: AgentViewModel,
    navController: NavController
) {
    val steps by viewModel.steps.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val generatedProject by viewModel.generatedProject.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var promptInput by remember {
        mutableStateOf("Build an Android Jetpack Compose Audio Player with playback state and playlist cards")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Agent Mission Box
        AgentForgeCard(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text("Autonomous AI Coding Agent", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Controlled execution pipeline: PLAN → ANALYZE → GENERATE → CHECK → FIX → PREVIEW",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = { Text("Describe agent mission...", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isRunning) {
                    AgentForgeButton(
                        text = "Start Autonomous Pipeline",
                        onClick = { viewModel.runAgent(promptInput) },
                        enabled = promptInput.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                } else {
                    Button(
                        onClick = { viewModel.cancelAgent() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Stop Agent Loop", fontSize = 13.sp)
                    }
                }
            }
        }

        errorMessage?.let {
            ErrorMessage(message = it, onRetry = { viewModel.runAgent(promptInput) })
        }

        // Stepper Progress List
        Text("Pipeline Lifecycle", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(steps) { step ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (step.status) {
                            "done" -> Color(0xFF064E3B).copy(alpha = 0.25f)
                            "running" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    when (step.status) {
                                        "done" -> Color(0xFF10B981)
                                        "running" -> MaterialTheme.colorScheme.primary
                                        else -> Color.Gray.copy(alpha = 0.3f)
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when (step.status) {
                                "done" -> Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                "running" -> CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                else -> Icon(Icons.Default.Circle, contentDescription = null, tint = Color.White, modifier = Modifier.size(8.dp))
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(step.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(step.detail, fontSize = 11.sp, color = Color.Gray)
                        }

                        Text(
                            text = step.status.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (step.status) {
                                "done" -> Color(0xFF10B981)
                                "running" -> MaterialTheme.colorScheme.primary
                                else -> Color.Gray
                            }
                        )
                    }
                }
            }

            generatedProject?.let { proj ->
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Agent Mission Completed!", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Project '${proj.name}' synthesized with ${proj.files.size} verified source files.", fontSize = 12.sp)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { navController.navigate("editor/${proj.id}") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Open in Editor")
                                }
                                if (proj.platform == "web") {
                                    OutlinedButton(
                                        onClick = { navController.navigate(Screen.Preview.createRoute(proj.id)) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Preview")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
