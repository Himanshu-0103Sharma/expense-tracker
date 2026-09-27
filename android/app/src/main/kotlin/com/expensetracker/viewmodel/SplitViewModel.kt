package com.expensetracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.expensetracker.network.*
import com.expensetracker.notifications.NotificationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val uploader = CloudinaryUploader()

data class SplitValidation(
    val isValid: Boolean,
    val assignedTotal: Double,
    val expectedTotal: Double,
    val message: String
)

class SplitViewModel(private val apiService: ApiService) {

    var splitExpenses by mutableStateOf<List<SplitExpenseResponse>>(emptyList())
        private set

    var balances by mutableStateOf<List<BalanceResponse>>(emptyList())
        private set

    var allUsers by mutableStateOf<List<UserInfo>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        scope.launch {
            NotificationEvent.refreshTrigger.collect {
                loadSplitExpenses()
            }
        }
    }

    fun loadSplitExpenses() {
        scope.launch {
            isLoading = true
            error = null
            try {
                splitExpenses = apiService.getSplitExpenses()
                balances = apiService.getBalances()
            } catch (e: Exception) {
                error = e.message ?: "Failed to load split expenses"
            }
            isLoading = false
        }
    }

    fun loadUsers() {
        scope.launch {
            try {
                allUsers = apiService.getAllUsers()
            } catch (e: Exception) {
                error = e.message ?: "Failed to load users"
            }
        }
    }

    fun validateSplit(
        totalAmount: Double,
        splitType: String,
        selectedUserIds: Set<Int>,
        customAmounts: Map<Int, String>
    ): SplitValidation {
        if (selectedUserIds.size < 2) {
            return SplitValidation(false, 0.0, totalAmount, "Select at least 2 people")
        }

        return when (splitType) {
            "EQUAL" -> SplitValidation(true, totalAmount, totalAmount, "")
            "AMOUNT" -> {
                val assigned = selectedUserIds.sumOf { customAmounts[it]?.toDoubleOrNull() ?: 0.0 }
                val matches = kotlin.math.abs(assigned - totalAmount) < 0.01
                SplitValidation(
                    isValid = matches,
                    assignedTotal = assigned,
                    expectedTotal = totalAmount,
                    message = if (matches) "" else "Amounts must add up to the total"
                )
            }
            "PERCENTAGE" -> {
                val totalPct = selectedUserIds.sumOf { customAmounts[it]?.toDoubleOrNull() ?: 0.0 }
                val matches = kotlin.math.abs(totalPct - 100.0) < 0.01
                SplitValidation(
                    isValid = matches,
                    assignedTotal = totalPct,
                    expectedTotal = 100.0,
                    message = if (matches) "" else "Percentages must add up to 100%"
                )
            }
            else -> SplitValidation(false, 0.0, totalAmount, "Invalid split type")
        }
    }

    fun buildParticipants(
        totalAmount: Double,
        splitType: String,
        selectedUserIds: Set<Int>,
        customAmounts: Map<Int, String>
    ): List<ParticipantShare> {
        return when (splitType) {
            "EQUAL" -> {
                val perPerson = totalAmount / selectedUserIds.size
                selectedUserIds.map { ParticipantShare(it, perPerson) }
            }
            "AMOUNT" -> {
                selectedUserIds.map { id ->
                    ParticipantShare(id, customAmounts[id]?.toDoubleOrNull() ?: 0.0)
                }
            }
            "PERCENTAGE" -> {
                selectedUserIds.map { id ->
                    val pct = customAmounts[id]?.toDoubleOrNull() ?: 0.0
                    ParticipantShare(id, totalAmount * pct / 100.0)
                }
            }
            else -> emptyList()
        }
    }

    fun createSplitExpense(
        totalAmount: Double,
        description: String,
        category: String,
        splitType: String,
        selectedUserIds: Set<Int>,
        customAmounts: Map<Int, String>,
        imageBytes: ByteArray? = null
    ) {
        val validation = validateSplit(totalAmount, splitType, selectedUserIds, customAmounts)
        if (!validation.isValid) {
            error = validation.message
            return
        }

        val participants = buildParticipants(totalAmount, splitType, selectedUserIds, customAmounts)

        scope.launch {
            try {
                val response = apiService.createSplitExpense(
                    CreateSplitExpenseRequest(
                        totalAmount = totalAmount,
                        description = description,
                        category = category,
                        splitType = splitType,
                        imageUrl = null,
                        participants = participants
                    )
                )
                loadSplitExpenses()

                if (imageBytes != null && response != null) {
                    scope.launch(Dispatchers.IO) {
                        val url = uploader.upload(imageBytes)
                        if (url != null) {
                            apiService.editSplitExpense(
                                response.id,
                                CreateSplitExpenseRequest(
                                    totalAmount = totalAmount,
                                    description = description,
                                    category = category,
                                    splitType = splitType,
                                    imageUrl = url,
                                    participants = participants
                                )
                            )
                            launch(Dispatchers.Main) { loadSplitExpenses() }
                        }
                    }
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to create split expense"
            }
        }
    }

    fun editSplitExpense(
        splitId: Int,
        totalAmount: Double,
        description: String,
        category: String,
        splitType: String,
        selectedUserIds: Set<Int>,
        customAmounts: Map<Int, String>,
        existingImageUrl: String? = null,
        newImageBytes: ByteArray? = null
    ) {
        val validation = validateSplit(totalAmount, splitType, selectedUserIds, customAmounts)
        if (!validation.isValid) {
            error = validation.message
            return
        }

        val participants = buildParticipants(totalAmount, splitType, selectedUserIds, customAmounts)

        scope.launch {
            try {
                apiService.editSplitExpense(
                    splitId,
                    CreateSplitExpenseRequest(
                        totalAmount = totalAmount,
                        description = description,
                        category = category,
                        splitType = splitType,
                        imageUrl = existingImageUrl,
                        participants = participants
                    )
                )
                loadSplitExpenses()

                if (newImageBytes != null) {
                    scope.launch(Dispatchers.IO) {
                        val url = uploader.upload(newImageBytes)
                        if (url != null) {
                            apiService.editSplitExpense(
                                splitId,
                                CreateSplitExpenseRequest(
                                    totalAmount = totalAmount,
                                    description = description,
                                    category = category,
                                    splitType = splitType,
                                    imageUrl = url,
                                    participants = participants
                                )
                            )
                            launch(Dispatchers.Main) { loadSplitExpenses() }
                        }
                    }
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to edit split expense"
            }
        }
    }

    fun settleUp(splitExpenseId: Int, userId: Int) {
        scope.launch {
            try {
                apiService.settleUp(splitExpenseId, userId)
                loadSplitExpenses()
            } catch (e: Exception) {
                error = e.message ?: "Failed to settle"
            }
        }
    }
}
