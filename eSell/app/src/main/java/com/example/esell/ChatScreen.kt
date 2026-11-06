package com.example.esell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esell.dto.MessageResponse
import kotlinx.coroutines.delay

@Composable
fun ChatScreen(
    itemId: Int,
    itemTitle: String,
    currentUsername: String,
    messages: List<MessageResponse>,
    isLoading: Boolean,
    isSending: Boolean,
    errorMessage: String?,
    onSendMessage: (String) -> Unit,
    onRefreshMessages: () -> Unit,
    onBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(itemId) {
        while (true) {
            delay(5000)
            onRefreshMessages()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Text(
                text = itemTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isLoading && messages.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null && messages.isEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else if (messages.isEmpty()) {
                Text(
                    text = "Напишите первое сообщение...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        val isMine = message.user.username == currentUsername
                        MessageBubble(message = message, isMine = isMine)
                    }
                }
            }
        }

        if (errorMessage != null && messages.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    if (it.length <= 500) {
                        inputText = it
                    }
                },
                placeholder = { Text("Сообщение...") },
                modifier = Modifier.weight(1f),
                maxLines = 4,
                isError = errorMessage != null,
                supportingText = {
                    if (inputText.length > 400) {
                        Text("${inputText.length} / 500")
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                enabled = !isSending && inputText.isNotBlank(),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .background(
                        if (inputText.isNotBlank() && !isSending) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(50)
                    )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Отправить",
                        tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: MessageResponse, isMine: Boolean) {
    val bubbleColor = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isMine) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
            if (!isMine) {
                Text(
                    text = message.user.username,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
            Surface(
                color = bubbleColor,
                shape = shape,
                tonalElevation = 1.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}