package com.expensetracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- COLORS (from design spec) ---
object AppColors {
    val background = Color(0xFFF6F7FA)
    val surface = Color(0xFFFFFFF1A)
    val elevatedGlass = Color(0xFFFFFFFF).copy(alpha = 0.26f)
    val primaryText = Color(0xFF0E0F11)
    val secondaryText = Color(0xFF63666A)
    val tertiaryText = Color(0xFF8E9095)
    val accent = Color(0xFF8C5CE7)
    val positive = Color(0xFF22C55E)
    val negative = Color(0xFFEF4444)
    val warning = Color(0xFFF59E0B)
    val separator = Color(0xFFE5E7EB)
    val darkBackground = Color(0xFF0B0C0F)

    val glassBorder = Color.White.copy(alpha = 0.12f)
    val glassFill = Color.White.copy(alpha = 0.65f)
    val glassSubtle = Color.White.copy(alpha = 0.45f)
    val glassElevated = Color.White.copy(alpha = 0.80f)
}

// --- SPACING (4pt system) ---
object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val section = 28.dp
    val major = 32.dp
    val hero = 40.dp
    val jumbo = 48.dp
}

// --- SHAPES ---
object AppShapes {
    val small = RoundedCornerShape(10.dp)
    val medium = RoundedCornerShape(14.dp)
    val large = RoundedCornerShape(20.dp)
    val extraLarge = RoundedCornerShape(28.dp)
    val pill = RoundedCornerShape(999.dp)
}

// --- TYPOGRAPHY ---
object AppTypography {
    val display = TextStyle(
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.37.sp
    )
    val largeTitle = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold
    )
    val title = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold
    )
    val headline = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    )
    val body = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    )
    val secondary = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    )
    val caption = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    )
    val financialHero = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold
    )
    val financialLarge = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold
    )
    val financialBody = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
}
