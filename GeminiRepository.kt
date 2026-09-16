package com.agentforge.ai.domain.repository

import com.agentforge.ai.data.model.*
import com.agentforge.ai.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class GeminiRepository(
    private var baseUrl: String = "http://10.0.2.2:3000" // Android Emulator loopback to host port 3000
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private var service: GeminiApiService = createService(baseUrl)

    private fun createService(url: String): GeminiApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(url.trimEnd('/') + "/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        return retrofit.create(GeminiApiService::class.java)
    }

    fun updateBaseUrl(newUrl: String) {
        baseUrl = newUrl
        service = createService(newUrl)
    }

    fun getBaseUrl(): String = baseUrl

    suspend fun checkHealth(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val resp = service.checkHealth()
            if (resp.isSuccessful) {
                Result.success(resp.body()?.status == "ok")
            } else {
                Result.failure(Exception("HTTP ${resp.code()}: ${resp.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendChat(messages: List<ChatMessage>, context: String = ""): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val apiMsgs = messages.map { ApiMessage(it.id, it.role, it.content, it.timestamp) }
                val resp = service.sendChat(ChatApiRequest(apiMsgs, context))
                if (resp.isSuccessful && resp.body() != null) {
                    Result.success(resp.body()!!.reply)
                } else {
                    Result.failure(Exception("AI Server Error: ${resp.errorBody()?.string() ?: resp.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun generateProject(prompt: String, platform: String): Result<Project> =
        withContext(Dispatchers.IO) {
            try {
                val resp = service.generateProject(
                    GenerateProjectRequest(
                        prompt = prompt,
                        projectType = platform,
                        framework = if (platform == "android") "compose" else "vanilla"
                    )
                )
                if (resp.isSuccessful && resp.body() != null) {
                    val body = resp.body()!!
                    val project = Project(
                        id = "proj-${System.currentTimeMillis()}",
                        name = body.name,
                        description = body.description,
                        platform = platform,
                        entryFile = body.entryFile,
                        files = body.files.map { ProjectFile(it.path, it.content) },
                        tags = listOf(platform.replaceFirstChar { it.uppercase() }, "Gemini")
                    )
                    Result.success(project)
                } else {
                    Result.failure(Exception("Generation failed: ${resp.errorBody()?.string() ?: resp.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun runAgentPlan(prompt: String, platform: String): Result<AgentPlan> =
        withContext(Dispatchers.IO) {
            try {
                val resp = service.runAgentStep(AgentStepRequest(step = "plan", prompt = prompt, platform = platform))
                if (resp.isSuccessful && resp.body() != null) {
                    val plan = json.decodeFromJsonElement<AgentPlan>(resp.body()!!.data)
                    Result.success(plan)
                } else {
                    Result.failure(Exception(resp.errorBody()?.string() ?: "Failed to generate plan"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun runAgentGenerate(prompt: String, plan: AgentPlan, platform: String): Result<List<ProjectFile>> =
        withContext(Dispatchers.IO) {
            try {
                val planDto = AgentPlanDto(plan.title, plan.overview, plan.architecture, plan.features, plan.fileStructure)
                val resp = service.runAgentStep(
                    AgentStepRequest(step = "generate", prompt = prompt, plan = planDto, platform = platform)
                )
                if (resp.isSuccessful && resp.body() != null) {
                    val rawFiles = resp.body()!!.data["files"]
                    if (rawFiles != null) {
                        val files = json.decodeFromJsonElement<List<ProjectFile>>(rawFiles)
                        Result.success(files)
                    } else {
                        Result.failure(Exception("No files returned by agent"))
                    }
                } else {
                    Result.failure(Exception(resp.errorBody()?.string() ?: "Generation failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun runAgentCheck(prompt: String, files: List<ProjectFile>, platform: String): Result<AgentCheckReport> =
        withContext(Dispatchers.IO) {
            try {
                val fileDtos = files.map { ApiFileResponse(it.path, it.content) }
                val resp = service.runAgentStep(
                    AgentStepRequest(step = "check", prompt = prompt, files = fileDtos, platform = platform)
                )
                if (resp.isSuccessful && resp.body() != null) {
                    val report = json.decodeFromJsonElement<AgentCheckReport>(resp.body()!!.data)
                    Result.success(report)
                } else {
                    Result.failure(Exception(resp.errorBody()?.string() ?: "Check step failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun runAgentFix(prompt: String, files: List<ProjectFile>, report: AgentCheckReport, platform: String): Result<List<ProjectFile>> =
        withContext(Dispatchers.IO) {
            try {
                val fileDtos = files.map { ApiFileResponse(it.path, it.content) }
                val reportElement = json.encodeToJsonElement(report).jsonObject
                val resp = service.runAgentStep(
                    AgentStepRequest(step = "fix", prompt = prompt, files = fileDtos, platform = platform, checkReport = reportElement)
                )
                if (resp.isSuccessful && resp.body() != null) {
                    val rawFiles = resp.body()!!.data["fixedFiles"]
                    if (rawFiles != null) {
                        val fixedFiles = json.decodeFromJsonElement<List<ProjectFile>>(rawFiles)
                        val updated = files.map { orig ->
                            fixedFiles.find { it.path == orig.path } ?: orig
                        }
                        Result.success(updated)
                    } else {
                        Result.success(files)
                    }
                } else {
                    Result.success(files)
                }
            } catch (e: Exception) {
                Result.success(files)
            }
        }

    suspend fun autoFix(code: String, error: String, filePath: String): Result<AutoFixProposal> =
        withContext(Dispatchers.IO) {
            try {
                val resp = service.autoFixCode(AutoFixRequest(code, error, filePath))
                if (resp.isSuccessful && resp.body() != null) {
                    val b = resp.body()!!
                    Result.success(
                        AutoFixProposal(
                            explanation = b.explanation,
                            originalCode = code,
                            fixedCode = b.fixedCode,
                            filePath = filePath,
                            rootCause = b.rootCause
                        )
                    )
                } else {
                    Result.failure(Exception("AutoFix error: ${resp.errorBody()?.string() ?: resp.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
