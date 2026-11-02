package com.example.esell

import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.esell.dto.CreateItemRequest
import com.example.esell.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

val apiCategories = listOf(
    Pair(1, "Электроника"), Pair(2, "Одежда"), Pair(3, "Автомобили"),
    Pair(4, "Недвижимость"), Pair(5, "Дом и сад"), Pair(6, "Бытовая техника"),
    Pair(7, "Детские товары"), Pair(8, "Хобби и отдых"), Pair(9, "Спорт"),
    Pair(10, "Животные"), Pair(11, "Услуги"), Pair(12, "Работа"),
    Pair(13, "Запчасти"), Pair(14, "Строительство и ремонт"),
    Pair(15, "Красота и здоровье"), Pair(16, "Книги"), Pair(17, "Прочее")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdsScreen(
    onBack: () -> Unit,
    onCreateSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val types = listOf("Продам", "Отдам", "Обмен")
    var selectedType by rememberSaveable { mutableStateOf(types[0]) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    var selectedCategory by rememberSaveable { mutableStateOf(apiCategories[0]) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {}
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Новое объявление", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .clickable { imagePickerLauncher.launch(arrayOf("image/*")) },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AndroidView(
                    factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
                    update = { it.setImageURI(selectedImageUri) },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Выбрать фото", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (selectedImageUri != null) {
            TextButton(onClick = { selectedImageUri = null }, modifier = Modifier.fillMaxWidth()) {
                Text("Убрать фото", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title, onValueChange = { title = it },
            label = { Text("Название") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Минимум 5 символов") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = price, onValueChange = { price = it },
            label = { Text("Цена") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = typeDropdownExpanded,
            onExpandedChange = { typeDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedType, onValueChange = {}, readOnly = true,
                label = { Text("Вид") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = typeDropdownExpanded, onDismissRequest = { typeDropdownExpanded = false }) {
                types.forEach { type ->
                    DropdownMenuItem(text = { Text(type) }, onClick = { selectedType = type; typeDropdownExpanded = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = categoryDropdownExpanded,
            onExpandedChange = { categoryDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedCategory.second, onValueChange = {}, readOnly = true,
                label = { Text("Категория") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = categoryDropdownExpanded, onDismissRequest = { categoryDropdownExpanded = false }) {
                apiCategories.forEach { category ->
                    DropdownMenuItem(text = { Text(category.second) }, onClick = { selectedCategory = category; categoryDropdownExpanded = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description, onValueChange = { description = it },
            label = { Text("Описание") }, minLines = 4, maxLines = 8, modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Минимум 10 символов") }
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (title.trim().length < 5) {
                    Toast.makeText(context, "Название должно содержать хотя бы 5 символов", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (description.trim().length < 10) {
                    Toast.makeText(context, "Описание должно содержать хотя бы 10 символов", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val parsedPrice = price.toIntOrNull() ?: 0
                val dealTypeApi = when (selectedType) {
                    "Отдам" -> "free"
                    "Обмен" -> "exchange"
                    else -> "sale"
                }

                isLoading = true
                scope.launch {
                    try {
                        var uploadedPhotoUrl: String? = null

                        if (selectedImageUri != null) {
                            val inputStream = context.contentResolver.openInputStream(selectedImageUri!!)
                            val bytes = inputStream?.readBytes()
                            if (bytes != null) {
                                val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                                val body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)
                                val uploadResponse = RetrofitClient.apiService.uploadImage(body)
                                uploadedPhotoUrl = uploadResponse.url
                            }
                        }

                        RetrofitClient.apiService.createItem(
                            CreateItemRequest(
                                deal_type = dealTypeApi,
                                title = title.trim(),
                                description = description.trim(),
                                price = parsedPrice,
                                category_id = selectedCategory.first,
                                photo_url = uploadedPhotoUrl
                            )
                        )

                        isLoading = false
                        Toast.makeText(context, "Объявление создано!", Toast.LENGTH_SHORT).show()
                        onCreateSuccess()

                    } catch (e: Exception) {
                        isLoading = false
                        Toast.makeText(context, "Ошибка сервера: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Создать", fontSize = 16.sp)
        }
    }
}