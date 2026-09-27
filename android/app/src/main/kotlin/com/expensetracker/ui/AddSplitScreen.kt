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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.expensetracker.network.UserInfo
import com.expensetracker.ui.theme.*
import com.expensetracker.viewmodel.SplitViewModel

private val addSplitGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFF6F7FA), Color(0xFFF0EEFF), Color(0xFFF6F7FA))
)

@Composable
fun AddSplitScreen(
    viewModel: SplitViewModel,
    currentUserId: Int,
    onDone: () -> Unit,
    onBack: () -> Unit,
    editSplitId: Int? = null,
    editDescription: String? = null,
    editTotalAmount: Double? = null,
    editSplitType: String? = null,
    editParticipantIds: Set<Int>? = null,
    editImageUrl: String? = null
) {
    LaunchedEffect(Unit) { viewModel.loadUsers() }

    val isEditMode = editSplitId != null
    val context = LocalContext.current

    var description by remember { mutableStateOf(editDescription ?: "") }
    var totalAmount by remember { mutableStateOf(editTotalAmount?.let { String.format("%.2f", it) } ?: "") }
    var splitType by remember { mutableStateOf(editSplitType ?: "EQUAL") }
    var selectedUserIds by remember { mutableStateOf(editParticipantIds ?: setOf(currentUserId)) }
    var customAmounts by remember { mutableStateOf(mutableMapOf<Int, String>()) }
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

    val allUsers = viewModel.allUsers
    val splitTypes = listOf("EQUAL", "AMOUNT", "PERCENTAGE")

    Box(modifier = Modifier.fillMaxSize().background(addSplitGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xl),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBack) {
                    Text("Cancel", style = AppTypography.body, color = AppColors.secondaryText)
                }
                TextButton(
                    onClick = {
                        val amount = totalAmount.toDoubleOrNull() ?: return@TextButton
                        if (description.isBlank()) return@TextButton

                        val existingUrl = if (keepExistingImage) editImageUrl else null
                        if (isEditMode) {
                            viewModel.editSplitExpense(
                                splitId = editSplitId!!,
                                totalAmount = amount,
                                description = description,
                                category = "General",
                                splitType = splitType,
                                selectedUserIds = selectedUserIds,
                                customAmounts = customAmounts,
                                existingImageUrl = existingUrl,
                                newImageBytes = imageBytes
                            )
                        } else {
                            viewModel.createSplitExpense(
                                totalAmount = amount,
                                description = description,
                                category = "General",
                                splitType = splitType,
                                selectedUserIds = selectedUserIds,
                                customAmounts = customAmounts,
                                imageBytes = imageBytes
                            )
                        }
                        onDone()
                    }
                ) {
                    Text(
                        if (isEditMode) "Save" else "Split",
                        style = AppTypography.body.copy(fontWeight = FontWeight.SemiBold),
                        color = AppColors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.major))

            // Description
            Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                Text("What was it for?", style = AppTypography.caption, color = AppColors.tertiaryText)
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Lunch, Uber, Movie...", style = AppTypography.title, color = AppColors.tertiaryText.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = AppTypography.title.copy(color = AppColors.primaryText),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = AppColors.accent,
                        cursorColor = AppColors.accent
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.xxl))

            // Amount
            Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                Text("Total amount", style = AppTypography.caption, color = AppColors.tertiaryText)
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                TextField(
                    value = totalAmount,
                    onValueChange = { totalAmount = it },
                    placeholder = { Text("0", style = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Bold), color = AppColors.tertiaryText.copy(alpha = 0.3f)) },
                    prefix = { Text("$ ", style = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Bold), color = AppColors.primaryText) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Bold, color = AppColors.primaryText),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = AppColors.accent,
                        cursorColor = AppColors.accent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.major))

            // Split type
            Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                Text("Split type", style = AppTypography.caption, color = AppColors.tertiaryText)
                Spacer(modifier = Modifier.height(AppSpacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    splitTypes.forEach { type ->
                        val selected = type == splitType
                        Text(
                            text = type.lowercase().replaceFirstChar { it.uppercase() },
                            style = AppTypography.secondary.copy(fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal),
                            color = if (selected) Color.White else AppColors.secondaryText,
                            modifier = Modifier
                                .clip(AppShapes.pill)
                                .background(if (selected) AppColors.accent else Color(0xFFF0F0F5))
                                .clickable { splitType = type }
                                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.major))

            // Select people + amounts
            Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                Text("Split between", style = AppTypography.caption, color = AppColors.tertiaryText)
                Spacer(modifier = Modifier.height(AppSpacing.md))

                // You (always selected)
                PersonSplitRow(
                    name = "You",
                    userId = currentUserId,
                    selected = true,
                    splitType = splitType,
                    customValue = customAmounts[currentUserId] ?: "",
                    onValueChange = { customAmounts = customAmounts.toMutableMap().apply { put(currentUserId, it) } },
                    onSelect = {}
                )

                // Other users
                allUsers.forEach { user ->
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    PersonSplitRow(
                        name = user.name,
                        userId = user.id,
                        selected = user.id in selectedUserIds,
                        splitType = splitType,
                        customValue = customAmounts[user.id] ?: "",
                        onValueChange = { customAmounts = customAmounts.toMutableMap().apply { put(user.id, it) } },
                        onSelect = {
                            selectedUserIds = if (user.id in selectedUserIds)
                                selectedUserIds - user.id
                            else
                                selectedUserIds + user.id
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.major))

            // Receipt image
            Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                Text("Receipt (optional)", style = AppTypography.caption, color = AppColors.tertiaryText)
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
                        Text("+ Attach receipt photo", style = AppTypography.body, color = AppColors.accent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xxl))

            // Validation summary from ViewModel
            val amount = totalAmount.toDoubleOrNull()
            if (amount != null && selectedUserIds.size >= 2) {
                val validation = viewModel.validateSplit(amount, splitType, selectedUserIds, customAmounts)
                Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                    when (splitType) {
                        "EQUAL" -> {
                            Text(
                                text = "$${String.format("%.2f", amount / selectedUserIds.size)} per person",
                                style = AppTypography.body.copy(fontWeight = FontWeight.Medium),
                                color = AppColors.accent
                            )
                        }
                        "AMOUNT" -> {
                            Text(
                                text = "Total assigned: $${String.format("%.2f", validation.assignedTotal)} / $${String.format("%.2f", validation.expectedTotal)}",
                                style = AppTypography.secondary,
                                color = if (validation.isValid) AppColors.positive else AppColors.negative
                            )
                            if (!validation.isValid) {
                                Text(text = validation.message, style = AppTypography.caption, color = AppColors.negative)
                            }
                        }
                        "PERCENTAGE" -> {
                            Text(
                                text = "Total: ${String.format("%.1f", validation.assignedTotal)}% / 100%",
                                style = AppTypography.secondary,
                                color = if (validation.isValid) AppColors.positive else AppColors.negative
                            )
                            if (!validation.isValid) {
                                Text(text = validation.message, style = AppTypography.caption, color = AppColors.negative)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun PersonSplitRow(
    name: String,
    userId: Int,
    selected: Boolean,
    splitType: String,
    customValue: String,
    onValueChange: (String) -> Unit,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.small)
            .background(if (selected) Color(0xFFF5F3FF) else Color.Transparent)
            .clickable { onSelect() }
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(AppShapes.pill)
                .background(if (selected) AppColors.accent else Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Text("✓", fontSize = 12.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.width(AppSpacing.md))

        // Name
        Text(
            text = name,
            style = AppTypography.body,
            color = if (selected) AppColors.primaryText else AppColors.secondaryText,
            modifier = Modifier.weight(1f)
        )

        // Amount/Percentage input (only when not EQUAL and person is selected)
        if (selected && splitType != "EQUAL") {
            TextField(
                value = customValue,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        if (splitType == "PERCENTAGE") "%" else "$",
                        style = AppTypography.secondary,
                        color = AppColors.tertiaryText
                    )
                },
                modifier = Modifier.width(100.dp),
                textStyle = AppTypography.body.copy(color = AppColors.primaryText),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = AppColors.accent,
                    cursorColor = AppColors.accent
                ),
                shape = AppShapes.small,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = {
                    if (splitType == "PERCENTAGE") Text("%", style = AppTypography.caption, color = AppColors.tertiaryText)
                }
            )
        }
    }
}
