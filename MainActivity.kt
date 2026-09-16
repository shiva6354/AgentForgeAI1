package com.agentforge.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.agentforge.ai.data.local.ProjectFileManager
import com.agentforge.ai.domain.repository.GeminiRepository
import com.agentforge.ai.navigation.AppNavigation
import com.agentforge.ai.ui.agent.AgentViewModel
import com.agentforge.ai.ui.builder.BuilderViewModel
import com.agentforge.ai.ui.chat.ChatViewModel
import com.agentforge.ai.ui.projects.ProjectsViewModel
import com.agentforge.ai.ui.theme.AgentForgeAITheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize dependencies
        val fileManager = ProjectFileManager(applicationContext)
        val repository = GeminiRepository()

        // ViewModels
        val chatViewModel = ChatViewModel(repository)
        val builderViewModel = BuilderViewModel(repository, fileManager)
        val agentViewModel = AgentViewModel(repository, fileManager)
        val projectsViewModel = ProjectsViewModel(fileManager)

        setContent {
            AgentForgeAITheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        repository = repository,
                        fileManager = fileManager,
                        chatViewModel = chatViewModel,
                        builderViewModel = builderViewModel,
                        agentViewModel = agentViewModel,
                        projectsViewModel = projectsViewModel
                    )
                }
            }
        }
    }
}
