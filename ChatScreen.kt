package com.agentforge.ai.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.navigation.NavController
import com.agentforge.ai.ui.components.CodeBlock
import com.agentforge.ai.ui.components.ErrorMessage
import com.agentforge.ai.ui.components.LoadingIndicator
import com.agentforge.ai.utils.VoiceInputManager

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    navController: NavController
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val voiceManager = remember { VoiceInputManager(context) }
    val isListening by voiceManager.isListening.collectAsState()
    val spokenText by voiceManager.spokenText.collectAsState()
    val voiceError by voiceManager.error.collectAsState()

    LaunchedEffect(spokenText) {
        if (spokenText.isNotBlank()) {
            textInput = if (textInput.isBlank()) spokenText else "$textInput $spokenText"
        }
    }

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "Compose state hoisting pattern",
        "Android ViewModel coroutines flow",
        "Responsive Tailwind card",
        "Clean architecture repository pattern"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Header
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Gemini AI Assistant", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Powered by gemini-3.8-flash", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Clear Chat", tint = Color.Gray)
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.widthIn(max = 340.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Render code blocks or text
                            val parts = msg.content.split("```")
                            if (parts.size > 1) {
                                parts.forEachIndexed { idx, part ->
                                    if (idx % 2 == 1) {
                                        val firstLineBreak = part.indexOf('\n')
                                        val lang = if (firstLineBreak > 0) part.substring(0, firstLineBreak).trim() else "code"
                                        val code = if (firstLineBreak > 0) part.substring(firstLineBreak + 1).trim() else part.trim()
                                        CodeBlock(code = code, language = lang, modifier = Modifier.padding(vertical = 4.dp))
                                    } else if (part.isNotBlank()) {
                                        Text(
                                            text = part.trim(),
                                            fontSize = 13.sp,
                                            color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = msg.content,
                                    fontSize = 13.sp,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = msg.timestamp,
                                fontSize = 9.sp,
                                color = (if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    LoadingIndicator("Gemini is generating response...")
                }
            }
        }

        // Voice error banner if any
        voiceError?.let {
            Text(
                text = "Voice: $it",
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }

        // API error banner
        error?.let {
            ErrorMessage(message = it, onRetry = {
                val lastUserMsg = messages.lastOrNull { it.role == "user" }
                if (lastUserMsg != null) viewModel.sendMessage(lastUserMsg.content)
            }, modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp))
        }

        // Quick Prompt Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickPrompts) { prompt ->
                AssistChip(
                    onClick = { viewModel.sendMessage(prompt) },
                    label = { Text(prompt, fontSize = 11.sp) }
                )
            }
        }

        // Bottom Input Bar
        Surface(
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice button
                IconButton(
                    onClick = {
                        if (isListening) voiceManager.stopListening() else voiceManager.startListening()
                    }
                ) {
                    Icon(
                        if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text(if (isListening) "Listening... speak now" else "Ask Gemini anything about code...", fontSize = 12.sp) },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f),
                    maxLines = 3
                )

                Spacer(Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        viewModel.sendMessage(textInput)
                        textInput = ""
                    },
                    enabled = !isLoading && textInput.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (!isLoading && textInput.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
    }
}
