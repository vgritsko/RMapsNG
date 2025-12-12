package com.sm.maps.applib.presentation.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Base ViewModel class that provides common functionality for all ViewModels
 * - Error handling with CoroutineExceptionHandler
 * - Loading state management
 * - Common navigation and message events
 */
abstract class BaseViewModel : ViewModel() {

    /**
     * Loading state - true when any operation is in progress
     */
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Error event - emitted when an error occurs
     */
    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent: LiveData<Event<String>> = _errorEvent

    /**
     * Message event - for showing toasts/snackbars
     */
    private val _messageEvent = MutableLiveData<Event<String>>()
    val messageEvent: LiveData<Event<String>> = _messageEvent

    /**
     * Exception handler for coroutines
     * Automatically logs errors and emits error events
     */
    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(getLogTag(), "Coroutine exception caught", throwable)
        handleError(throwable)
    }

    /**
     * Get the log tag for this ViewModel
     * Override to provide custom tag
     */
    protected open fun getLogTag(): String = this::class.java.simpleName

    /**
     * Launch a coroutine with automatic error handling and loading state management
     *
     * @param showLoading Whether to show loading state during execution
     * @param onError Optional custom error handler
     * @param block The suspend function to execute
     */
    protected fun launchWithLoading(
        showLoading: Boolean = true,
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(exceptionHandler) {
            try {
                if (showLoading) _isLoading.value = true
                block()
            } catch (e: Exception) {
                Log.e(getLogTag(), "Error in coroutine", e)
                onError?.invoke(e) ?: handleError(e)
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }

    /**
     * Handle error by emitting error event
     * Override to customize error handling
     */
    protected open fun handleError(throwable: Throwable) {
        val message = throwable.message ?: "An unknown error occurred"
        _errorEvent.value = Event(message)
        Log.e(getLogTag(), "Error: $message", throwable)
    }

    /**
     * Show a message to the user (toast/snackbar)
     */
    protected fun showMessage(message: String) {
        _messageEvent.value = Event(message)
    }

    /**
     * Set loading state manually
     */
    protected fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    /**
     * Clear any current loading state
     */
    protected fun clearLoading() {
        _isLoading.value = false
    }
}
