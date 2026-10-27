package com.example.esell

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esell.dto.LoginRequest
import com.example.esell.dto.RegisterRequest
import com.example.esell.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: (name: String, email: String, bio: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenStorage = remember { TokenStorage(context) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var serverError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        var ok = true
        if (!name.trim().matches(Regex("^[a-zA-Z0-9_]{3,32}$"))) {
            nameError = "Имя: 3-32 символа, латиница, цифры, _"
            ok = false
        } else nameError = null

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            emailError = "Введите корректный email"
            ok = false
        } else emailError = null

        if (password.length < 8 || password.length > 64) {
            passwordError = "Пароль: от 8 до 64 символов"
            ok = false
        } else passwordError = null

        if (password != confirmPassword) {
            confirmError = "Пароли не совпадают"
            ok = false
        } else confirmError = null

        return ok
    }

    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Регистрация", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name, onValueChange = { name = it; if (nameError != null) nameError = null },
            label = { Text("Имя пользователя") }, singleLine = true,
            isError = nameError != null, supportingText = { nameError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it; if (emailError != null) emailError = null },
            label = { Text("Email") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError != null, supportingText = { emailError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it; if (passwordError != null) passwordError = null },
            label = { Text("Пароль") }, singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { TextButton(onClick = { passwordVisible = !passwordVisible }) { Text(if (passwordVisible) "Скрыть" else "Показать") } },
            isError = passwordError != null, supportingText = { passwordError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword, onValueChange = { confirmPassword = it; if (confirmError != null) confirmError = null },
            label = { Text("Повторите пароль") }, singleLine = true,
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { TextButton(onClick = { confirmVisible = !confirmVisible }) { Text(if (confirmVisible) "Скрыть" else "Показать") } },
            isError = confirmError != null, supportingText = { confirmError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                serverError = null
                if (!validate()) return@Button

                isLoading = true
                scope.launch {
                    try {
                        RetrofitClient.apiService.register(
                            RegisterRequest(username = name.trim(), email = email.trim(), password = password)
                        )
                        val tokenResponse = RetrofitClient.apiService.login(
                            LoginRequest(email = email.trim(), password = password)
                        )
                        tokenStorage.saveTokens(tokenResponse.access_token, tokenResponse.refresh_token)

                        isLoading = false
                        onRegisterSuccess(name.trim(), email.trim(), "")
                    } catch (e: Exception) {
                        isLoading = false
                        serverError = "Ошибка регистрации: ${e.message}"
                        Toast.makeText(context, serverError, Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Зарегистрироваться", fontSize = 16.sp)
        }

        if (serverError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(serverError!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Уже есть аккаунт? Войти",
            fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clickable { onBack() }.padding(vertical = 12.dp)
        )
    }
}