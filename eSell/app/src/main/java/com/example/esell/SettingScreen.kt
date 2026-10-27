package com.example.esell

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val BIO_MAX = 500

@Composable
fun SettingsScreen(
    viewModel: ProfileViewModel,
    onSaved: () -> Unit,
    onClearProfile: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var draftName by rememberSaveable { mutableStateOf(viewModel.username) }
    var draftEmail by rememberSaveable { mutableStateOf(viewModel.email) }
    var draftBio by rememberSaveable { mutableStateOf(viewModel.aboutMe) }
    var draftNotifications by rememberSaveable { mutableStateOf(viewModel.notificationsEnabled) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var bioError by remember { mutableStateOf<String?>(null) }

    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            when {
                error.contains("Имя") -> nameError = error
                error.contains("Email") -> emailError = error
                error.contains("Описание") -> bioError = error
            }
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Настройки",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Измените данные профиля",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = draftName,
            onValueChange = {
                if (it.length <= 32) draftName = it
                if (nameError != null) nameError = null
            },
            label = { Text("Имя") },
            singleLine = true,
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = draftEmail,
            onValueChange = { },
            label = { Text("Email") },
            singleLine = true,
            readOnly = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError != null,
            supportingText = { emailError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = draftBio,
            onValueChange = {
                if (it.length <= BIO_MAX) draftBio = it
                if (bioError != null) bioError = null
            },
            label = { Text("О себе") },
            placeholder = { Text("Расскажите о себе") },
            minLines = 4,
            maxLines = 8,
            isError = bioError != null,
            supportingText = { bioError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "${draftBio.length} / $BIO_MAX",
            fontSize = 12.sp,
            color = if (draftBio.length == BIO_MAX)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, end = 4.dp),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Получать уведомления",
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = draftNotifications,
                onCheckedChange = { draftNotifications = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.saveProfileToServer(
                    name = draftName,
                    bio = draftBio,
                    notifications = draftNotifications
                ) {
                    Toast.makeText(context, "Профиль успешно обновлён", Toast.LENGTH_SHORT).show()
                    onSaved()
                }
            },
            enabled = !viewModel.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Сохранить", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { showClearDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Удалить профиль", fontSize = 16.sp)
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Удалить профиль?") },
            text = { Text("Вы уверены, что хотите удалить профиль? Все данные будут удалены безвозвратно.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        viewModel.clearProfile()
                        Toast.makeText(context, "Профиль удалён", Toast.LENGTH_SHORT).show()
                        onClearProfile()
                    }
                ) {
                    Text("Да", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Нет")
                }
            }
        )
    }
}