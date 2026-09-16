package com.agentforge.ai.data.local

import android.content.Context
import com.agentforge.ai.data.model.Project
import com.agentforge.ai.data.model.ProjectFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ProjectFileManager(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val projectsDir: File
        get() = File(context.filesDir, "agentforge_projects").apply {
            if (!exists()) mkdirs()
        }

    suspend fun saveProject(project: Project): Unit = withContext(Dispatchers.IO) {
        val folder = File(projectsDir, project.id)
        if (!folder.exists()) folder.mkdirs()

        val metaFile = File(folder, "project_meta.json")
        metaFile.writeText(json.encodeToString(project))

        project.files.forEach { file ->
            val target = File(folder, file.path)
            target.parentFile?.mkdirs()
            target.writeText(file.content)
        }
    }

    suspend fun loadAllProjects(): List<Project> = withContext(Dispatchers.IO) {
        val results = mutableListOf<Project>()
        val folders = projectsDir.listFiles { f -> f.isDirectory } ?: emptyArray()

        for (folder in folders) {
            val metaFile = File(folder, "project_meta.json")
            if (metaFile.exists()) {
                try {
                    val p = json.decodeFromString<Project>(metaFile.readText())
                    results.add(p)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (results.isEmpty()) {
            val initial = listOf(getDemoSweetBakeryProject(), getComposeCounterProject())
            initial.forEach { saveProject(it) }
            return@withContext initial
        }
        results
    }

    suspend fun getProjectById(id: String): Project? = withContext(Dispatchers.IO) {
        loadAllProjects().find { it.id == id }
    }

    suspend fun deleteProject(id: String): Boolean = withContext(Dispatchers.IO) {
        val folder = File(projectsDir, id)
        folder.deleteRecursively()
    }

    suspend fun exportProjectToZip(project: Project, targetZipFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            ZipOutputStream(FileOutputStream(targetZipFile)).use { zos ->
                project.files.forEach { file ->
                    val entry = ZipEntry(file.path)
                    zos.putNextEntry(entry)
                    zos.write(file.content.toByteArray())
                    zos.closeEntry()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getDemoSweetBakeryProject(): Project {
        return Project(
            id = "demo-sweet-bakery",
            name = "Sweet Bakery",
            description = "Artisanal bakery website with fresh croissants, interactive basket, and responsive layout.",
            platform = "web",
            entryFile = "index.html",
            files = listOf(
                ProjectFile(
                    path = "index.html",
                    content = """<!DOCTYPE html>
<html lang="en" class="scroll-smooth">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Sweet Bakery</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-amber-50 text-amber-950 min-h-screen p-4">
  <header class="max-w-xl mx-auto py-6 text-center border-b border-amber-200">
    <h1 class="text-3xl font-bold">🥐 Sweet Bakery</h1>
    <p class="text-xs text-amber-800 mt-1">Stone Oven Breads & Pastries</p>
  </header>
  <main class="max-w-xl mx-auto space-y-4 pt-4">
    <div class="p-4 bg-white rounded-2xl shadow-sm border border-amber-200">
      <h2 class="text-base font-bold">Butter Croissant - $4.25</h2>
      <p class="text-xs text-slate-500 mt-1">27-layer flaky laminated pastry baked with European butter.</p>
    </div>
    <div class="p-4 bg-white rounded-2xl shadow-sm border border-amber-200">
      <h2 class="text-base font-bold">Artisan Sourdough - $7.50</h2>
      <p class="text-xs text-slate-500 mt-1">36-hour cold fermented loaf with honeycomb open crumb.</p>
    </div>
  </main>
</body>
</html>"""
                )
            ),
            tags = listOf("Web", "HTML", "Demo")
        )
    }

    fun getComposeCounterProject(): Project {
        return Project(
            id = "compose-counter-starter",
            name = "Compose Counter Pro",
            description = "Jetpack Compose counter application demonstrating unidirectional data flow and Material 3 state hoisting.",
            platform = "android",
            entryFile = "app/src/main/java/com/agentforge/app/MainActivity.kt",
            files = listOf(
                ProjectFile(
                    path = "app/src/main/java/com/agentforge/app/MainActivity.kt",
                    content = """package com.agentforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var count by remember { mutableIntStateOf(0) }
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Counter: ${'$'}count", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { count-- }) { Text("-1") }
                            Button(onClick = { count++ }) { Text("+1") }
                        }
                    }
                }
            }
        }
    }
}"""
                ),
                ProjectFile(
                    path = "app/src/main/AndroidManifest.xml",
                    content = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.agentforge.app">
    <application
        android:label="Compose Counter"
        android:theme="@android:style/Theme.Material.NoActionBar">
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>"""
                )
            ),
            tags = listOf("Android", "Kotlin", "Compose")
        )
    }
}
