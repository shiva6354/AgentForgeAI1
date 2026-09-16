package com.agentforge.ai.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agentforge.ai.data.local.ProjectFileManager
import com.agentforge.ai.data.model.Project
import com.agentforge.ai.navigation.Screen
import com.agentforge.ai.ui.components.AgentForgeCard
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    fileManager: ProjectFileManager,
    navController: NavController
) {
    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        projects = fileManager.loadAllProjects()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF4F46E5), Color(0xFF06B6D4))
                        ),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "GEMINI 3.8 FLASH",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { navController.navigate(Screen.Settings.route) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        "AgentForge AI",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Turn natural language into full Android Compose apps and interactive web code.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Quick Action Grid (Chat, Builder, Agent, Projects)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionTile(
                title = "AI Chat",
                sub = "Architecture & Code",
                icon = Icons.Default.ChatBubble,
                color = Color(0xFF6366F1),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Chat.route) }
            )
            QuickActionTile(
                title = "Builder",
                sub = "Prompt-to-App",
                icon = Icons.Default.AutoAwesome,
                color = Color(0xFF06B6D4),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Builder.route) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionTile(
                title = "Agent",
                sub = "6-Step Pipeline",
                icon = Icons.Default.Layers,
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Agent.route) }
            )
            QuickActionTile(
                title = "Projects",
                sub = "${projects.size} Saved",
                icon = Icons.Default.Folder,
                color = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Projects.route) }
            )
        }

        // Recent Projects section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Projects", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            TextButton(onClick = { navController.navigate(Screen.Projects.route) }) {
                Text("View All", fontSize = 11.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(projects.take(4)) { project ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("editor/${project.id}") }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (project.platform == "android") Color(0xFF6366F1).copy(alpha = 0.2f)
                                    else Color(0xFF06B6D4).copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (project.platform == "android") Icons.Default.Android else Icons.Default.Language,
                                contentDescription = null,
                                tint = if (project.platform == "android") Color(0xFF6366F1) else Color(0xFF06B6D4),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(project.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${project.files.size} files • ${project.platform.uppercase()}", fontSize = 11.sp, color = Color.Gray)
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionTile(
    title: String,
    sub: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(sub, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
