package com.expensetracker.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.expensetracker.network.ExpenseResponse
import com.expensetracker.ui.components.MoneyText
import com.expensetracker.ui.theme.*
import com.expensetracker.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val homeGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF0EEFF),
        Color(0xFFF6F7FA),
        Color(0xFFF6F7FA)
    )
)

@Composable
fun HomeScreen(
    userName: String,
    viewModel: ExpenseViewModel,
    onAddExpense: () -> Unit,
    onEditExpense: (ExpenseResponse) -> Unit,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.loadExpenses() }

    val expenses = viewModel.expenses
    val totalSpent = expenses.sumOf { it.amount }

    Box(modifier = Modifier.fillMaxSize().background(homeGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good morning,",
                        style = AppTypography.secondary,
                        color = AppColors.secondaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = userName,
                        style = AppTypography.title,
                        color = AppColors.primaryText
                    )
                }
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

            // Balance Card — glass effect with purple tint
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.xxl, vertical = AppSpacing.major),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total spent",
                        style = AppTypography.secondary,
                        color = AppColors.secondaryText
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    MoneyText(
                        amount = totalSpent,
                        style = AppTypography.financialHero
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Text(
                        text = "${expenses.size} expense${if (expenses.size != 1) "s" else ""}",
                        style = AppTypography.caption,
                        color = AppColors.tertiaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.major))

            // Quick action with press animation
            val addInteraction = remember { MutableInteractionSource() }
            val addPressed by addInteraction.collectIsPressedAsState()
            val addScale by animateFloatAsState(
                targetValue = if (addPressed) 0.96f else 1f,
                animationSpec = tween(100)
            )

            Surface(
                onClick = onAddExpense,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xl)
                    .graphicsLayer(scaleX = addScale, scaleY = addScale),
                shape = AppShapes.medium,
                color = Color(0xFFF5F3FF),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE0E0E0)),
                interactionSource = addInteraction
            ) {
                Box(
                    modifier = Modifier.padding(vertical = AppSpacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Add expense",
                        style = AppTypography.body.copy(fontWeight = FontWeight.Medium),
                        color = AppColors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.major))

            // Expense list
            if (viewModel.isLoading) {
                Text(
                    text = "Loading...",
                    style = AppTypography.secondary,
                    color = AppColors.tertiaryText,
                    modifier = Modifier.padding(horizontal = AppSpacing.xl)
                )
            } else if (expenses.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.hero),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No expenses yet",
                        style = AppTypography.headline,
                        color = AppColors.primaryText
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Text(
                        text = "Add your first expense and\nstart tracking shared spending.",
                        style = AppTypography.secondary,
                        color = AppColors.secondaryText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "Recent activity",
                    style = AppTypography.caption,
                    color = AppColors.tertiaryText,
                    modifier = Modifier.padding(start = AppSpacing.xl, bottom = AppSpacing.md)
                )

                Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                    expenses.forEach { expense ->
                        ExpenseRow(
                            expense = expense,
                            onDelete = { id -> viewModel.deleteExpense(id) },
                            onEdit = { onEditExpense(it) }
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.md))
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // FAB with press animation
        val fabInteraction = remember { MutableInteractionSource() }
        val fabPressed by fabInteraction.collectIsPressedAsState()
        val fabScale by animateFloatAsState(
            targetValue = if (fabPressed) 0.92f else 1f,
            animationSpec = tween(120)
        )

        FloatingActionButton(
            onClick = onAddExpense,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AppSpacing.xl)
                .graphicsLayer(scaleX = fabScale, scaleY = fabScale),
            shape = AppShapes.pill,
            containerColor = AppColors.accent,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp
            ),
            interactionSource = fabInteraction
        ) {
            Row(
                modifier = Modifier.padding(horizontal = AppSpacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Light, color = Color.White)
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Text("Add", style = AppTypography.body.copy(fontWeight = FontWeight.Medium), color = Color.White)
            }
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: ExpenseResponse,
    onDelete: (Int) -> Unit,
    onEdit: (ExpenseResponse) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(100)
    )

    Surface(
        onClick = { onEdit(expense) },
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (expense.imageUrl != null) {
                AsyncImage(
                    model = expense.imageUrl,
                    contentDescription = "Receipt",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(AppShapes.small),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(AppSpacing.md))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    style = AppTypography.body,
                    color = AppColors.primaryText
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    text = "${expense.category} · ${formatDate(expense.date)}",
                    style = AppTypography.secondary,
                    color = AppColors.tertiaryText
                )
            }
            MoneyText(
                amount = expense.amount,
                style = AppTypography.financialBody
            )
            Spacer(modifier = Modifier.width(AppSpacing.md))
            IconButton(
                onClick = { onEdit(expense) },
                modifier = Modifier.size(32.dp)
            ) {
                Text("✎", fontSize = 15.sp, color = AppColors.accent)
            }
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(32.dp)
            ) {
                Text("✕", fontSize = 15.sp, color = AppColors.negative.copy(alpha = 0.7f))
            }
        }
    }

    if (showDeleteDialog) {
        AppAlertDialog(
            title = "Delete Expense",
            message = "Delete \"${expense.description}\"?",
            confirmText = "Delete",
            onConfirm = { onDelete(expense.id); showDeleteDialog = false },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

private fun formatDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
