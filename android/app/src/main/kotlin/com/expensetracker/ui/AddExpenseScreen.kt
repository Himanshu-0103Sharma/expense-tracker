package com.expensetracker.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.expensetracker.ui.components.GlassLevel
import com.expensetracker.ui.components.GlassSurface
import com.expensetracker.ui.theme.*

private val categories = listOf("Food", "Transport", "Bills", "Entertainment", "Shopping", "Health", "Other")

private val addGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF6F7FA),
        Color(0xFFF0EEFF),
        Color(0xFFF6F7FA)
    )
)

@Composable
fun AddExpenseScreen(
    onSave: (amount: Double, description: String, category: String, existingImageUrl: String?, newImageBytes: ByteArray?) -> Unit,
    onBack: () -> Unit,
    editAmount: Double? = null,
    editDescription: String? = null,
    editCategory: String? = null,
    editImageUrl: String? = null
) {
    val isEditMode = editAmount != null
    val context = LocalContext.current

    var amount by remember { mutableStateOf(editAmount?.let { String.format("%.2f", it) } ?: "") }
    var description by remember { mutableStateOf(editDescription ?: "") }
    var selectedCategory by remember { mutableStateOf(editCategory ?: categories[0]) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var keepExistingImage by remember { mutableStateOf(editImageUrl != null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            val inputStream = context.contentResolver.openInputStream(it)
            imageBytes = inputStream?.readBytes()
            inputStream?.close()
            keepExistingImage = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(addGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(60.dp))

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("Cancel", style = AppTypography.body, color = AppColors.secondaryText)
                }
                TextButton(
                    onClick = {
                        val parsedAmount = amount.toDoubleOrNull()
                        if (parsedAmount != null && parsedAmount > 0 && description.isNotBlank()) {
                            val existingUrl = if (keepExistingImage) editImageUrl else null
                            onSave(parsedAmount, description, selectedCategory, existingUrl, imageBytes)
                        }
                    }
                ) {
                    Text(
                        text = if (isEditMode) "Save" else "Add",
                        style = AppTypography.body.copy(fontWeight = FontWeight.SemiBold),
                        color = AppColors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.hero))

            // Description (what was it for?)
            Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                var descFocused by remember { mutableStateOf(false) }
                Text(
                    text = "What was it for?",
                    style = AppTypography.caption,
                    color = AppColors.tertiaryText
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = {
                        Text("Dinner, Uber, Groceries...", style = AppTypography.title, color = AppColors.tertiaryText.copy(alpha = 0.5f))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { descFocused = it.isFocused },
                    textStyle = AppTypography.title.copy(color = AppColors.primaryText),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        cursorColor = AppColors.accent
                    ),
                    singleLine = true
                )
                com.expensetracker.ui.components.AnimatedUnderline(focused = descFocused)
            }

            Spacer(modifier = Modifier.height(AppSpacing.major))

            Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                var amountFocused by remember { mutableStateOf(false) }
                Text(
                    text = "Amount",
                    style = AppTypography.caption,
                    color = AppColors.tertiaryText
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = {
                        Text(
                            "0",
                            style = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold),
                            color = AppColors.tertiaryText.copy(alpha = 0.3f)
                        )
                    },
                    prefix = {
                        Text(
                            "$ ",
                            style = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold),
                            color = AppColors.primaryText
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { amountFocused = it.isFocused },
                    textStyle = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.primaryText
                    ),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        cursorColor = AppColors.accent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                com.expensetracker.ui.components.AnimatedUnderline(focused = amountFocused)
            }

            Spacer(modifier = Modifier.height(AppSpacing.major))

            // Category
            Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                Text(
                    text = "Category",
                    style = AppTypography.caption,
                    color = AppColors.tertiaryText
                )
                Spacer(modifier = Modifier.height(AppSpacing.md))
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    categories.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            row.forEach { category ->
                                CategoryChip(
                                    label = category,
                                    selected = category == selectedCategory,
                                    onClick = { selectedCategory = category }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.major))

            // Receipt image
            Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                Text(
                    text = "Receipt (optional)",
                    style = AppTypography.caption,
                    color = AppColors.tertiaryText
                )
                Spacer(modifier = Modifier.height(AppSpacing.md))

                val hasImage = imageUri != null || keepExistingImage
                if (hasImage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(AppShapes.medium)
                            .border(0.5.dp, Color(0xFFE0E0E0), AppShapes.medium)
                            .clickable { imagePicker.launch("image/*") }
                    ) {
                        AsyncImage(
                            model = imageUri ?: editImageUrl,
                            contentDescription = "Receipt",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                        TextButton(onClick = { imagePicker.launch("image/*") }) {
                            Text("Replace", style = AppTypography.secondary, color = AppColors.accent)
                        }
                        TextButton(onClick = { imageUri = null; imageBytes = null; keepExistingImage = false }) {
                            Text("Remove", style = AppTypography.secondary, color = AppColors.negative)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(AppShapes.medium)
                            .border(1.dp, Color(0xFFE0E0E0), AppShapes.medium)
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Attach receipt photo",
                            style = AppTypography.body,
                            color = AppColors.accent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) AppColors.accent else Color(0xFFF0F0F5)
    val textColor = if (selected) Color.White else AppColors.secondaryText

    Text(
        text = label,
        style = AppTypography.secondary.copy(
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        ),
        color = textColor,
        modifier = Modifier
            .clip(AppShapes.pill)
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm)
    )
}
