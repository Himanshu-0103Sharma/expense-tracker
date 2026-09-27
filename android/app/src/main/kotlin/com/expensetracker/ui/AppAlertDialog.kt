package com.expensetracker.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.expensetracker.ui.theme.AppColors
import com.expensetracker.ui.theme.AppShapes
import com.expensetracker.ui.theme.AppTypography

@Composable
fun AppAlertDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    confirmColor: Color = AppColors.negative,
    dismissColor: Color = AppColors.secondaryText,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, style = AppTypography.headline, color = AppColors.primaryText)
        },
        text = {
            Text(message, style = AppTypography.body, color = AppColors.secondaryText)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, style = AppTypography.body.copy(fontWeight = FontWeight.SemiBold), color = confirmColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, style = AppTypography.body, color = dismissColor)
            }
        },
        shape = AppShapes.large,
        containerColor = AppColors.glassElevated
    )
}
