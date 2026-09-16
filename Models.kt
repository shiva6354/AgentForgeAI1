package com.agentforge.ai.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProjectFile(
    val path: String = "",
    val content: String = ""
)

@Serializable
data class Project(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val platform: String = "android", // "android" or "web"
    val entryFile: String = "",
    val files: List<ProjectFile> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList()
)

@Serializable
data class ChatMessage(
    val id: String = "",
    val role: String = "assistant", // "user" or "assistant"
    val content: String = "",
    val timestamp: String = ""
)

@Serializable
data class AgentPlan(
    val title: String = "AgentForge Project",
    val overview: String = "",
    val architecture: String = "",
    val features: List<String> = emptyList(),
    val fileStructure: List<String> = emptyList()
)

@Serializable
data class AgentCheckIssue(
    val file: String = "",
    val line: String? = null,
    val severity: String = "medium", // "low", "medium", "high"
    val description: String = "",
    val suggestedFix: String = ""
)

@Serializable
data class AgentCheckReport(
    val score: Int = 95,
    val passed: Boolean = true,
    val status: String = "passed",
    val issues: List<AgentCheckIssue> = emptyList(),
    val verificationNotes: String = ""
)

@Serializable
data class AutoFixProposal(
    val explanation: String = "",
    val originalCode: String = "",
    val fixedCode: String = "",
    val filePath: String = "",
    val rootCause: String = ""
)

