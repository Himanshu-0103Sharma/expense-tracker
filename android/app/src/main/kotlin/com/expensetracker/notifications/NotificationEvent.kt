package com.expensetracker.notifications

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NotificationEvent {
    private val _refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshTrigger = _refreshTrigger.asSharedFlow()

    fun triggerRefresh() {
        _refreshTrigger.tryEmit(Unit)
    }
}
