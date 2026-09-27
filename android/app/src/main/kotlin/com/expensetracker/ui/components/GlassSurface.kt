package com.expensetracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.expensetracker.ui.theme.AppShapes

enum class GlassLevel {
    Subtle,
    Standard,
    Elevated
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    level: GlassLevel = GlassLevel.Standard,
    shape: Shape = AppShapes.large,
    content: @Composable ColumnScope.() -> Unit
) {
    val fillColor = when (level) {
        GlassLevel.Subtle -> Color.White.copy(alpha = 0.5f)
        GlassLevel.Standard -> Color.White.copy(alpha = 0.85f)
        GlassLevel.Elevated -> Color.White.copy(alpha = 0.95f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = fillColor,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE0E0E0))
    ) {
        Column(content = content)
    }
}
