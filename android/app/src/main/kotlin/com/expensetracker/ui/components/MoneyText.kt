package com.expensetracker.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.expensetracker.ui.theme.AppColors
import com.expensetracker.ui.theme.AppTypography
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MoneyText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = AppTypography.financialBody,
    showSign: Boolean = false,
    currencySymbol: String = "$"
) {
    val color = when {
        amount > 0 && showSign -> AppColors.positive
        amount < 0 && showSign -> AppColors.negative
        else -> AppColors.primaryText
    }

    val formatted = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }.format(kotlin.math.abs(amount))

    val sign = when {
        amount > 0 && showSign -> "+"
        amount < 0 && showSign -> "−"
        else -> ""
    }

    Text(
        text = "$sign$currencySymbol$formatted",
        style = style,
        color = color,
        modifier = modifier
    )
}
