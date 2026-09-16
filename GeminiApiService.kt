package com.agentforge.ai.data.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

@Serializable
data class ChatApiRequest(
    val messages: List<ApiMessage>,
    val context: String = ""
)

@Serializable
data class ApiMessage(
    val id: String? = null,
    val role: String,
    val content: String,
    val timestamp: String? = null
)

@Serializable
data class ChatApiResponse(
    val reply: String
)

@Serializable
data class GenerateProjectRequest(
    val prompt: String,
    val projectType: String,
    val framework: String = "compose"
)

@Serializable
data class GenerateProjectResponse(
    val name: String,
    val description: String,
    val entryFile: String,
    val files: List<ApiFileResponse>,
    val dependencies: List<String> = emptyList()
)

@Serializable
data class ApiFileResponse(
    val path: String,
    val content: String
)

@Serializable
data class AgentStepRequest(
    val step: String,
    val prompt: String? = null,
    val platform: String = "android",
    val plan: AgentPlanDto? = null,
    val files: List<ApiFileResponse>? = null,
    val checkReport: kotlinx.serialization.json.JsonObject? = null
)

@Serializable
data class AgentPlanDto(
    val title: String,
    val overview: String,
    val architecture: String,
    val features: List<String>,
    val fileStructure: List<String>
)

@Serializable
data class AgentStepResponse(
    val step: String,
    val data: kotlinx.serialization.json.JsonObject
)

@Serializable
data class AutoFixRequest(
    val code: String,
    val errorMessage: String,
    val filePath: String
)

@Serializable
data class AutoFixResponse(
    val fixedCode: String,
    val explanation: String,
    val rootCause: String
)

@Serializable
data class HealthResponse(
    val status: String,
    val hasApiKey: Boolean
)

interface GeminiApiService {
    @GET("/api/health")
    suspend fun checkHealth(): Response<HealthResponse>

    @POST("/api/chat")
    suspend fun sendChat(@Body request: ChatApiRequest): Response<ChatApiResponse>

    @POST("/api/generate-project")
    suspend fun generateProject(@Body request: GenerateProjectRequest): Response<GenerateProjectResponse>

    @POST("/api/agent-step")
    suspend fun runAgentStep(@Body request: AgentStepRequest): Response<AgentStepResponse>

    @POST("/api/auto-fix")
    suspend fun autoFixCode(@Body request: AutoFixRequest): Response<AutoFixResponse>
}
