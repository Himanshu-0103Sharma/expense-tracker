package com.expensetracker.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.expensetracker.network.ApiService
import com.expensetracker.network.CloudinaryUploader
import com.expensetracker.network.ExpenseResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpenseViewModel(private val apiService: ApiService) {

    var expenses by mutableStateOf<List<ExpenseResponse>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    private val scope = CoroutineScope(Dispatchers.Main)
    private val uploader = CloudinaryUploader()

    fun loadExpenses() {
        scope.launch {
            isLoading = true
            error = null
            try {
                expenses = apiService.getPersonalExpenses()
            } catch (e: Exception) {
                error = e.message ?: "Failed to load expenses"
            }
            isLoading = false
        }
    }

    fun addExpense(amount: Double, description: String, category: String, imageBytes: ByteArray? = null) {
        scope.launch {
            try {
                val response = apiService.addPersonalExpense(amount, description, category, imageUrl = null)
                loadExpenses()

                if (imageBytes != null && response != null) {
                    scope.launch(Dispatchers.IO) {
                        val url = uploader.upload(imageBytes)
                        if (url != null) {
                            apiService.editPersonalExpense(response.id, amount, description, category, url)
                            launch(Dispatchers.Main) { loadExpenses() }
                        }
                    }
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to add expense"
            }
        }
    }

    fun editExpense(id: Int, amount: Double, description: String, category: String, existingImageUrl: String? = null, newImageBytes: ByteArray? = null) {
        scope.launch {
            try {
                apiService.editPersonalExpense(id, amount, description, category, existingImageUrl)
                loadExpenses()

                if (newImageBytes != null) {
                    scope.launch(Dispatchers.IO) {
                        val url = uploader.upload(newImageBytes)
                        if (url != null) {
                            apiService.editPersonalExpense(id, amount, description, category, url)
                            launch(Dispatchers.Main) { loadExpenses() }
                        }
                    }
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to edit expense"
            }
        }
    }

    fun deleteExpense(id: Int) {
        scope.launch {
            try {
                apiService.deletePersonalExpense(id)
                expenses = expenses.filter { it.id != id }
            } catch (e: Exception) {
                error = e.message ?: "Failed to delete expense"
            }
        }
    }
}
