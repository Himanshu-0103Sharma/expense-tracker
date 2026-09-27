package com.expensetracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.expensetracker.ui.components.GlassLevel
import com.expensetracker.ui.components.GlassSurface
import com.expensetracker.ui.theme.*
import com.expensetracker.viewmodel.AuthState
import com.expensetracker.viewmodel.AuthViewModel

private val ambientGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF0EEFF),
        Color(0xFFF6F7FA),
        Color(0xFFF2F8F6)
    )
)

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    val authState = viewModel.authState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ambientGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = if (isRegisterMode) "Create Account" else "Welcome Back",
                style = AppTypography.display,
                color = AppColors.primaryText
            )

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            Text(
                text = if (isRegisterMode) "Sign up to start tracking expenses"
                       else "Sign in to continue",
                style = AppTypography.secondary,
                color = AppColors.secondaryText
            )

            Spacer(modifier = Modifier.height(AppSpacing.hero))

            Column {
                if (isRegisterMode) {
                    GlassTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Name"
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                }

                GlassTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))

                GlassTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    isPassword = true
                )
            }

            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(AppSpacing.md))
                Text(
                    text = authState.message,
                    style = AppTypography.caption,
                    color = AppColors.negative
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.xxl))

            Button(
                onClick = {
                    if (isRegisterMode) viewModel.register(email, name, password)
                    else viewModel.login(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = AppShapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.accent,
                    disabledContainerColor = AppColors.accent.copy(alpha = 0.4f)
                ),
                enabled = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isRegisterMode) "Create Account" else "Sign In",
                        style = AppTypography.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                Text(
                    text = if (isRegisterMode) "Already have an account? Sign In"
                           else "Don't have an account? Create Account",
                    style = AppTypography.secondary,
                    color = AppColors.accent,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    var focused by remember { mutableStateOf(false) }

    Column {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(label, style = AppTypography.body, color = AppColors.tertiaryText.copy(alpha = 0.6f)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .onFocusChanged { focused = it.isFocused },
            textStyle = AppTypography.body.copy(color = AppColors.primaryText),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF5F5F8),
                focusedContainerColor = Color(0xFFF0EEFF),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                cursorColor = AppColors.accent
            ),
            shape = AppShapes.small,
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
        com.expensetracker.ui.components.AnimatedUnderline(focused = focused)
    }
}
