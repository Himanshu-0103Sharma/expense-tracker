package com.expensetracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensetracker.network.ApiService
import com.expensetracker.network.ExpenseResponse
import com.expensetracker.network.TokenStore
import com.expensetracker.ui.*
import com.expensetracker.ui.theme.AppColors
import com.expensetracker.ui.theme.AppShapes
import com.expensetracker.ui.theme.AppSpacing
import com.expensetracker.ui.theme.AppTypography
import com.expensetracker.viewmodel.AuthState
import com.expensetracker.viewmodel.AuthViewModel
import com.expensetracker.viewmodel.ExpenseViewModel
import com.expensetracker.viewmodel.SplitViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        setContent {
            val tokenStore = remember { TokenStore(this@MainActivity) }
            val apiService = remember { ApiService(tokenStore) }
            val authViewModel = remember { AuthViewModel(apiService) }
            val expenseViewModel = remember { ExpenseViewModel(apiService) }
            val splitViewModel = remember { SplitViewModel(apiService) }

            var currentScreen by remember { mutableStateOf("home") }
            var currentTab by remember { mutableStateOf(0) }
            var editingExpense by remember { mutableStateOf<ExpenseResponse?>(null) }
            var viewingSplit by remember { mutableStateOf<com.expensetracker.network.SplitExpenseResponse?>(null) }

            when {
                authViewModel.authState !is AuthState.Success -> {
                    LoginScreen(viewModel = authViewModel)
                }
                currentScreen == "expense_detail" && editingExpense != null -> {
                    ExpenseDetailScreen(
                        expense = editingExpense!!,
                        onBack = { editingExpense = null; currentScreen = "home" },
                        onEdit = { currentScreen = "add_expense" }
                    )
                }
                currentScreen == "add_expense" -> {
                    AddExpenseScreen(
                        onSave = { amount, description, category, existingImageUrl, newImageBytes ->
                            if (editingExpense != null) {
                                expenseViewModel.editExpense(editingExpense!!.id, amount, description, category, existingImageUrl, newImageBytes)
                            } else {
                                expenseViewModel.addExpense(amount, description, category, newImageBytes)
                            }
                            editingExpense = null
                            currentScreen = "home"
                        },
                        onBack = {
                            if (editingExpense != null) {
                                currentScreen = "expense_detail"
                            } else {
                                currentScreen = "home"
                            }
                        },
                        editAmount = editingExpense?.amount,
                        editDescription = editingExpense?.description,
                        editCategory = editingExpense?.category,
                        editImageUrl = editingExpense?.imageUrl
                    )
                }
                currentScreen == "add_split" -> {
                    val user = (authViewModel.authState as AuthState.Success).user
                    AddSplitScreen(
                        viewModel = splitViewModel,
                        currentUserId = user.id,
                        onDone = { currentScreen = "home" },
                        onBack = { currentScreen = "home" }
                    )
                }
                currentScreen == "split_detail" && viewingSplit != null -> {
                    val user = (authViewModel.authState as AuthState.Success).user
                    SplitDetailScreen(
                        split = viewingSplit!!,
                        currentUserId = user.id,
                        onBack = { viewingSplit = null; currentScreen = "home" },
                        onEdit = { currentScreen = "edit_split" }
                    )
                }
                currentScreen == "edit_split" && viewingSplit != null -> {
                    val user = (authViewModel.authState as AuthState.Success).user
                    AddSplitScreen(
                        viewModel = splitViewModel,
                        currentUserId = user.id,
                        onDone = { viewingSplit = null; currentScreen = "home" },
                        onBack = { currentScreen = "split_detail" },
                        editSplitId = viewingSplit!!.id,
                        editDescription = viewingSplit!!.description,
                        editTotalAmount = viewingSplit!!.totalAmount,
                        editSplitType = viewingSplit!!.splitType,
                        editParticipantIds = viewingSplit!!.participants.map { it.user.id }.toSet(),
                        editImageUrl = viewingSplit!!.imageUrl
                    )
                }
                else -> {
                    val user = (authViewModel.authState as AuthState.Success).user

                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            when (currentTab) {
                                0 -> HomeScreen(
                                    userName = user.name,
                                    viewModel = expenseViewModel,
                                    onAddExpense = { editingExpense = null; currentScreen = "add_expense" },
                                    onEditExpense = { editingExpense = it; currentScreen = "expense_detail" },
                                    onLogout = { authViewModel.logout() }
                                )
                                1 -> SplitScreen(
                                    viewModel = splitViewModel,
                                    currentUserId = user.id,
                                    onAddSplit = { currentScreen = "add_split" },
                                    onSplitClick = { split ->
                                        viewingSplit = split
                                        currentScreen = "split_detail"
                                    },
                                    onLogout = { authViewModel.logout() }
                                )
                            }
                        }

                        // Bottom tab bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.95f))
                                .padding(vertical = AppSpacing.md)
                                .navigationBarsPadding(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TabItem(
                                label = "Expenses",
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 }
                            )
                            TabItem(
                                label = "Split",
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun TabItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = AppTypography.secondary.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (selected) AppColors.accent else AppColors.tertiaryText
        )
        if (selected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(2.dp)
                    .background(AppColors.accent, AppShapes.pill)
            )
        }
    }
}
