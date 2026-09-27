package com.expensetracker.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.network.BalanceResponse
import com.expensetracker.network.SplitExpenseResponse
import com.expensetracker.ui.components.MoneyText
import com.expensetracker.ui.theme.*
import com.expensetracker.viewmodel.SplitViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val splitGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF0EEFF),
        Color(0xFFF6F7FA),
        Color(0xFFF6F7FA)
    )
)

@Composable
fun SplitScreen(viewModel: SplitViewModel, currentUserId: Int, onAddSplit: () -> Unit, onSplitClick: (SplitExpenseResponse) -> Unit, onLogout: () -> Unit) {
    LaunchedEffect(Unit) { viewModel.loadSplitExpenses() }

    val balances = viewModel.balances
    val splits = viewModel.splitExpenses

    Box(modifier = Modifier.fillMaxSize().background(splitGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Split Expenses",
                    style = AppTypography.title,
                    color = AppColors.primaryText
                )
                var showLogoutDialog by remember { mutableStateOf(false) }
                TextButton(onClick = { showLogoutDialog = true }) {
                    Text("Logout", style = AppTypography.secondary, color = AppColors.accent)
                }
                if (showLogoutDialog) {
                    AppAlertDialog(
                        title = "Logout",
                        message = "You'll need to sign in again.",
                        confirmText = "Logout",
                        confirmColor = AppColors.accent,
                        onConfirm = { showLogoutDialog = false; onLogout() },
                        onDismiss = { showLogoutDialog = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xxl))

            // Balances summary — glass effect with purple tint
            if (balances.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.xl)
                        .clip(AppShapes.large)
                        .background(Color.White.copy(alpha = 0.92f))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF9B6FE8).copy(alpha = 0.08f),
                                    Color(0xFFB794F6).copy(alpha = 0.04f),
                                    Color(0xFF9B6FE8).copy(alpha = 0.06f)
                                )
                            )
                        )
                ) {
                    Column(modifier = Modifier.padding(AppSpacing.xl)) {
                        Text(
                            text = "Balances",
                            style = AppTypography.caption,
                            color = AppColors.tertiaryText
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.md))
                        balances.forEach { balance ->
                            BalanceRow(balance, currentUserId)
                            Spacer(modifier = Modifier.height(AppSpacing.sm))
                        }
                    }
                }
            } else if (!viewModel.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.hero),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "All settled up",
                        style = AppTypography.headline,
                        color = AppColors.primaryText
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Text(
                        text = "No pending balances.\nAdd a split expense to get started.",
                        style = AppTypography.secondary,
                        color = AppColors.secondaryText,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.major))

            // Split expenses list
            if (splits.isNotEmpty()) {
                Text(
                    text = "Recent splits",
                    style = AppTypography.caption,
                    color = AppColors.tertiaryText,
                    modifier = Modifier.padding(start = AppSpacing.xl, bottom = AppSpacing.md)
                )

                Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                    splits.forEach { split ->
                        SplitExpenseRow(split, onClick = { onSplitClick(split) })
                        Spacer(modifier = Modifier.height(AppSpacing.md))
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // FAB
        FloatingActionButton(
            onClick = onAddSplit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AppSpacing.xl),
            shape = AppShapes.pill,
            containerColor = AppColors.accent,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = AppSpacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Light, color = Color.White)
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Text("Split", style = AppTypography.body.copy(fontWeight = FontWeight.Medium), color = Color.White)
            }
        }
    }
}

@Composable
private fun BalanceRow(balance: BalanceResponse, currentUserId: Int) {
    val fromName = if (balance.from.id == currentUserId) "You" else balance.from.name
    val toName = if (balance.to.id == currentUserId) "you" else balance.to.name

    val text = if (balance.from.id == currentUserId) {
        "You owe $toName"
    } else {
        "$fromName owes you"
    }

    val color = if (balance.from.id == currentUserId) AppColors.negative else AppColors.positive

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = AppTypography.body,
            color = AppColors.primaryText
        )
        Text(
            text = "$${String.format("%.2f", balance.amount)}",
            style = AppTypography.financialBody,
            color = color
        )
    }
}

@Composable
private fun SplitExpenseRow(split: SplitExpenseResponse, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(100)
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = AppShapes.medium,
        color = Color.White.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE0E0E0)),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = split.description,
                    style = AppTypography.body,
                    color = AppColors.primaryText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${split.paidBy.name} paid · ${split.splitType.lowercase()} · ${formatDate(split.date)}",
                    style = AppTypography.secondary,
                    color = AppColors.tertiaryText
                )
            }
            MoneyText(
                amount = split.totalAmount,
                style = AppTypography.financialBody
            )
        }
    }
}

private fun formatDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
