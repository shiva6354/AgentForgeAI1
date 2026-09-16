package com.agentforge.ai.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.agentforge.ai.data.local.ProjectFileManager
import com.agentforge.ai.domain.repository.GeminiRepository
import com.agentforge.ai.ui.agent.AgentScreen
import com.agentforge.ai.ui.agent.AgentViewModel
import com.agentforge.ai.ui.builder.BuilderScreen
import com.agentforge.ai.ui.builder.BuilderViewModel
import com.agentforge.ai.ui.chat.ChatScreen
import com.agentforge.ai.ui.chat.ChatViewModel
import com.agentforge.ai.ui.editor.CodeEditorScreen
import com.agentforge.ai.ui.home.HomeScreen
import com.agentforge.ai.ui.preview.PreviewScreen
import com.agentforge.ai.ui.projects.ProjectsScreen
import com.agentforge.ai.ui.projects.ProjectsViewModel
import com.agentforge.ai.ui.settings.SettingsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    repository: GeminiRepository,
    fileManager: ProjectFileManager,
    chatViewModel: ChatViewModel,
    builderViewModel: BuilderViewModel,
    agentViewModel: AgentViewModel,
    projectsViewModel: ProjectsViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarScreens = listOf(
        Screen.Home,
        Screen.Chat,
        Screen.Builder,
        Screen.Agent,
        Screen.Projects
    )

    val showBottomBar = bottomBarScreens.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(tonalElevation = 8.dp) {
                    bottomBarScreens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 10.sp) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(fileManager = fileManager, navController = navController)
            }
            composable(Screen.Chat.route) {
                ChatScreen(viewModel = chatViewModel, navController = navController)
            }
            composable(Screen.Builder.route) {
                BuilderScreen(viewModel = builderViewModel, navController = navController)
            }
            composable(Screen.Agent.route) {
                AgentScreen(viewModel = agentViewModel, navController = navController)
            }
            composable(Screen.Projects.route) {
                ProjectsScreen(viewModel = projectsViewModel, navController = navController)
            }
            composable(
                route = Screen.Editor.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStack ->
                val projectId = backStack.arguments?.getString("projectId") ?: ""
                CodeEditorScreen(
                    projectId = projectId,
                    fileManager = fileManager,
                    repository = repository,
                    navController = navController
                )
            }
            composable(
                route = Screen.Preview.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStack ->
                val projectId = backStack.arguments?.getString("projectId") ?: ""
                PreviewScreen(
                    projectId = projectId,
                    fileManager = fileManager,
                    navController = navController
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(repository = repository)
            }
        }
    }
}
