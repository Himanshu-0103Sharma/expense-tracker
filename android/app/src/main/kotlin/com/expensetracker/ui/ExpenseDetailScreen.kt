package com.expensetracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.expensetracker.network.ExpenseResponse
import com.expensetracker.ui.components.GlassLevel
import com.expensetracker.ui.components.GlassSurface
import com.expensetracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val detailGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFF0EEFF), Color(0xFFF6F7FA), Color(0xFFF6F7FA))
)

@Composable
fun ExpenseDetailScreen(
    expense: ExpenseResponse,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(detailGradient)) {
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
                TextButton(onClick = onBack) {
                    Text("← Back", style = AppTypography.body, color = AppColors.secondaryText)
                }
                TextButton(onClick = onEdit) {
                    Text("Edit", style = AppTypography.body.copy(fontWeight = FontWeight.SemiBold), color = AppColors.accent)
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.major))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = expense.description,
                    style = AppTypography.title,
                    color = AppColors.primaryText
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                Text(
                    text = "$${String.format("%.2f", expense.amount)}",
                    style = AppTypography.financialHero,
                    color = AppColors.primaryText
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                Text(
                    text = "${expense.category} · ${sdf.format(Date(expense.date))}",
                    style = AppTypography.secondary,
                    color = AppColors.tertiaryText
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.major))

            if (expense.imageUrl != null) {
                Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                    Text(
                        text = "Receipt",
                        style = AppTypography.caption,
                        color = AppColors.tertiaryText
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    AsyncImage(
                        model = expense.imageUrl,
                        contentDescription = "Receipt",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 400.dp)
                            .clip(AppShapes.medium),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
