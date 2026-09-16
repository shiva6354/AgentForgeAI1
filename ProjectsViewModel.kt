package com.agentforge.ai.ui.projects

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentforge.ai.data.local.ProjectFileManager
import com.agentforge.ai.data.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ProjectsViewModel(
    private val fileManager: ProjectFileManager
) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterPlatform = MutableStateFlow("all")
    val filterPlatform: StateFlow<String> = _filterPlatform.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _projects.value = fileManager.loadAllProjects()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(platform: String) {
        _filterPlatform.value = platform
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            fileManager.deleteProject(id)
            loadProjects()
        }
    }

    fun duplicateProject(project: Project) {
        viewModelScope.launch {
            val copy = project.copy(
                id = "proj-copy-${System.currentTimeMillis()}",
                name = "${project.name} (Copy)",
                updatedAt = System.currentTimeMillis()
            )
            fileManager.saveProject(copy)
            loadProjects()
        }
    }

    fun exportProjectZip(project: Project, context: Context): File? {
        val exportFile = File(context.cacheDir, "${project.name.replace(" ", "_")}.zip")
        val success = kotlinx.coroutines.runBlocking {
            fileManager.exportProjectToZip(project, exportFile)
        }
        return if (success) exportFile else null
    }
}
