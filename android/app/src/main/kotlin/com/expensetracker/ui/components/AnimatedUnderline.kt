package com.expensetracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.expensetracker.ui.theme.AppColors

@Composable
fun AnimatedUnderline(focused: Boolean) {
    val width by animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = tween(durationMillis = 250)
    )

    Box(modifier = Modifier.fillMaxWidth().height(2.dp)) {
        // Gray base line (always visible, subtle)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .align(Alignment.BottomCenter)
                .background(AppColors.separator.copy(alpha = 0.3f))
        )
        // Purple animated line (expands left to right on focus)
        Box(
            modifier = Modifier
                .fillMaxWidth(width)
                .height(2.dp)
                .align(Alignment.BottomStart)
                .background(if (focused) AppColors.accent else Color.Transparent)
        )
    }
}
