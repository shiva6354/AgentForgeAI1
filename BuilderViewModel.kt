package com.agentforge.ai.ui.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentforge.ai.data.local.ProjectFileManager
import com.agentforge.ai.data.model.Project
import com.agentforge.ai.data.model.ProjectFile
import com.agentforge.ai.domain.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BuilderViewModel(
    private val repository: GeminiRepository,
    private val fileManager: ProjectFileManager
) : ViewModel() {

    private val _project = MutableStateFlow<Project>(fileManager.getDemoSweetBakeryProject())
    val project: StateFlow<Project> = _project.asStateFlow()

    private val _activeFilePath = MutableStateFlow("index.html")
    val activeFilePath: StateFlow<String> = _activeFilePath.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedPlatform = MutableStateFlow("android")
    val selectedPlatform: StateFlow<String> = _selectedPlatform.asStateFlow()

    fun setPlatform(platform: String) {
        _selectedPlatform.value = platform
    }

    fun selectFile(path: String) {
        _activeFilePath.value = path
    }

    fun loadProject(loaded: Project) {
        _project.value = loaded
        _selectedPlatform.value = loaded.platform
        _activeFilePath.value = loaded.entryFile.ifEmpty { loaded.files.firstOrNull()?.path ?: "" }
    }

    fun generateProject(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty() || _isGenerating.value) return

        _isGenerating.value = true
        _error.value = null

        viewModelScope.launch {
            val result = repository.generateProject(trimmed, _selectedPlatform.value)
            _isGenerating.value = false

            result.onSuccess { newProject ->
                _project.value = newProject
                _activeFilePath.value = newProject.entryFile
                fileManager.saveProject(newProject)
            }.onFailure { err ->
                _error.value = err.message ?: "Failed to generate project codebase."
            }
        }
    }

    fun updateFileContent(path: String, newContent: String) {
        val current = _project.value
        val updatedFiles = current.files.map { f ->
            if (f.path == path) f.copy(content = newContent) else f
        }
        val updatedProj = current.copy(files = updatedFiles, updatedAt = System.currentTimeMillis())
        _project.value = updatedProj
        viewModelScope.launch {
            fileManager.saveProject(updatedProj)
        }
    }

    fun addFile(newPath: String) {
        val current = _project.value
        if (current.files.any { it.path == newPath }) return

        val updatedFiles = current.files + ProjectFile(newPath, "// New file: $newPath\n")
        val updatedProj = current.copy(files = updatedFiles, updatedAt = System.currentTimeMillis())
        _project.value = updatedProj
        _activeFilePath.value = newPath
        viewModelScope.launch {
            fileManager.saveProject(updatedProj)
        }
    }
}
