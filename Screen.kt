package com.agentforge.ai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Chat : Screen("chat", "Chat", Icons.Default.ChatBubble)
    object Builder : Screen("builder", "Builder", Icons.Default.AutoAwesome)
    object Agent : Screen("agent", "Agent", Icons.Default.Layers)
    object Projects : Screen("projects", "Projects", Icons.Default.Folder)
    object Editor : Screen("editor/{projectId}", "Editor", Icons.Default.Code) {
        fun createRoute(projectId: String) = "editor/$projectId"
    }
    object Preview : Screen("preview/{projectId}", "Preview", Icons.Default.Visibility) {
        fun createRoute(projectId: String) = "preview/$projectId"
    }
    object AutoFix : Screen("autofix/{projectId}", "Auto Fix", Icons.Default.Build) {
        fun createRoute(projectId: String) = "autofix/$projectId"
    }
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
