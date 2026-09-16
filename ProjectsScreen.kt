package com.agentforge.ai.ui.projects

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.agentforge.ai.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel,
    navController: NavController
) {
    val projects by viewModel.projects.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterPlatform by viewModel.filterPlatform.collectAsState()
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val filteredList = projects.filter { p ->
        val matchesFilter = filterPlatform == "all" || p.platform == filterPlatform
        val matchesSearch = p.name.contains(searchQuery, ignoreCase = true) ||
                p.description.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search and Filters
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search projects...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = filterPlatform == "all",
                    onClick = { viewModel.setFilter("all") },
                    label = { Text("All (${projects.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = filterPlatform == "android",
                    onClick = { viewModel.setFilter("android") },
                    label = { Text("Android", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = filterPlatform == "web",
                    onClick = { viewModel.setFilter("web") },
                    label = { Text("Web", fontSize = 11.sp) }
                )
            }

            IconButton(onClick = { navController.navigate(Screen.Builder.route) }) {
                Icon(Icons.Default.Add, contentDescription = "New Project", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Projects List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList) { project ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("editor/${project.id}") }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(project.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            AssistChip(
                                onClick = {},
                                label = { Text(project.platform.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                            )
                        }

                        Text(
                            text = project.description,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 2,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = "${project.files.size} files • Updated ${dateFormat.format(Date(project.updatedAt))}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        // Actions row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { navController.navigate("editor/${project.id}") },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Edit Code", fontSize = 11.sp)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (project.platform == "web") {
                                    IconButton(
                                        onClick = { navController.navigate(Screen.Preview.createRoute(project.id)) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Preview", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.duplicateProject(project) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = Color.Gray)
                                }

                                IconButton(
                                    onClick = {
                                        val zipFile = viewModel.exportProjectZip(project, context)
                                        if (zipFile != null) {
                                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", zipFile)
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/zip"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Project ZIP"))
                                        }
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Export ZIP", tint = Color.Gray)
                                }

                                IconButton(
                                    onClick = { viewModel.deleteProject(project.id) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
