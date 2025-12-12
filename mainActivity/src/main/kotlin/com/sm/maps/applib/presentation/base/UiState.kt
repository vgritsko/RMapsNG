package com.sm.maps.applib.presentation.base

/**
 * Sealed class representing the different UI states
 * Used to model loading, success, error, and empty states in ViewModels
 *
 * @param T The type of data when the state is successful
 */
sealed class UiState<out T> {

    /**
     * Initial or loading state
     * UI should show loading indicators
     */
    data object Loading : UiState<Nothing>()

    /**
     * Success state with data
     * UI should display the data
     *
     * @param data The successfully loaded data
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Error state
     * UI should show error message
     *
     * @param exception The error that occurred
     * @param message Optional user-friendly error message
     */
    data class Error(
        val exception: Throwable,
        val message: String? = exception.message
    ) : UiState<Nothing>()

    /**
     * Empty state (no data available)
     * UI should show empty state message
     */
    data object Empty : UiState<Nothing>()
}

/**
 * Extension function to check if state is loading
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

/**
 * Extension function to check if state is success
 */
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success

/**
 * Extension function to check if state is error
 */
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error

/**
 * Extension function to check if state is empty
 */
fun <T> UiState<T>.isEmpty(): Boolean = this is UiState.Empty

/**
 * Extension function to get data if state is success, null otherwise
 */
fun <T> UiState<T>.getDataOrNull(): T? = when (this) {
    is UiState.Success -> data
    else -> null
}
