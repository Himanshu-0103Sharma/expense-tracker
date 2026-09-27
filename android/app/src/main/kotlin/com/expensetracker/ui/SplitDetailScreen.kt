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
import com.expensetracker.network.SplitExpenseResponse
import com.expensetracker.ui.components.GlassLevel
import com.expensetracker.ui.components.GlassSurface
import com.expensetracker.ui.theme.*

private val detailGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFF0EEFF), Color(0xFFF6F7FA), Color(0xFFF6F7FA))
)

@Composable
fun SplitDetailScreen(
    split: SplitExpenseResponse,
    currentUserId: Int,
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

            // Top bar
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

            // Description + amount
            Column(
                modifier = Modifier.padding(horizontal = AppSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = split.description,
                    style = AppTypography.title,
                    color = AppColors.primaryText
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                Text(
                    text = "$${String.format("%.2f", split.totalAmount)}",
                    style = AppTypography.financialHero,
                    color = AppColors.primaryText
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                val paidByName = if (split.paidBy.id == currentUserId) "You" else split.paidBy.name
                Text(
                    text = "Paid by $paidByName • ${split.splitType.lowercase()}",
                    style = AppTypography.secondary,
                    color = AppColors.tertiaryText
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.major))

            // Split breakdown
            GlassSurface(
                modifier = Modifier.padding(horizontal = AppSpacing.xl),
                level = GlassLevel.Standard
            ) {
                Column(modifier = Modifier.padding(AppSpacing.xl)) {
                    Text(
                        text = "Split breakdown",
                        style = AppTypography.caption,
                        color = AppColors.tertiaryText
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.lg))

                    split.participants.forEach { participant ->
                        val name = if (participant.user.id == currentUserId) "You" else participant.user.name
                        val isPayer = participant.user.id == split.paidBy.id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AppSpacing.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = name,
                                    style = AppTypography.body,
                                    color = AppColors.primaryText
                                )
                                if (isPayer) {
                                    Text(
                                        text = "paid the bill",
                                        style = AppTypography.caption,
                                        color = AppColors.accent
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$${String.format("%.2f", participant.amount)}",
                                    style = AppTypography.financialBody,
                                    color = AppColors.primaryText
                                )
                                if (!isPayer) {
                                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                                    Text(
                                        text = if (participant.settled) "✓ settled" else "pending",
                                        style = AppTypography.caption,
                                        color = if (participant.settled) AppColors.positive else AppColors.negative
                                    )
                                }
                            }
                        }

                        if (participant != split.participants.last()) {
                            Divider(
                                color = AppColors.separator.copy(alpha = 0.3f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = AppSpacing.xs)
                            )
                        }
                    }
                }
            }

            if (split.imageUrl != null) {
                Spacer(modifier = Modifier.height(AppSpacing.major))
                Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                    Text(
                        text = "Receipt",
                        style = AppTypography.caption,
                        color = AppColors.tertiaryText
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    AsyncImage(
                        model = split.imageUrl,
                        contentDescription = "Receipt",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 400.dp)
                            .clip(AppShapes.medium),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xxl))

            // Summary
            Column(modifier = Modifier.padding(horizontal = AppSpacing.xl)) {
                Text(
                    text = "Summary",
                    style = AppTypography.caption,
                    color = AppColors.tertiaryText
                )
                Spacer(modifier = Modifier.height(AppSpacing.md))

                val unsettled = split.participants.filter { !it.settled && it.user.id != split.paidBy.id }
                if (unsettled.isEmpty()) {
                    Text(
                        text = "All settled up ✓",
                        style = AppTypography.body,
                        color = AppColors.positive
                    )
                } else {
                    unsettled.forEach { p ->
                        val ownerName = if (p.user.id == currentUserId) "You" else p.user.name
                        val payeeName = if (split.paidBy.id == currentUserId) "you" else split.paidBy.name
                        Text(
                            text = "$ownerName owes $payeeName $${String.format("%.2f", p.amount)}",
                            style = AppTypography.body,
                            color = AppColors.primaryText
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
