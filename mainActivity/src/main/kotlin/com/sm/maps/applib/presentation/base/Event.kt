package com.sm.maps.applib.presentation.base

import androidx.lifecycle.Observer

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 * Events are one-time occurrences (like navigation or showing a toast) that should not be
 * re-emitted on configuration changes.
 *
 * Example usage:
 * ```
 * // In ViewModel
 * private val _navigationEvent = MutableLiveData<Event<String>>()
 * val navigationEvent: LiveData<Event<String>> = _navigationEvent
 *
 * fun navigateToDetail(id: String) {
 *     _navigationEvent.value = Event(id)
 * }
 *
 * // In Fragment/Activity
 * viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
 *     event.getContentIfNotHandled()?.let { id ->
 *         // Navigate only if the event hasn't been handled
 *         findNavController().navigate(DetailFragmentDirections.actionToDetail(id))
 *     }
 * }
 * ```
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set

    /**
     * Returns the content and prevents its use again.
     * Returns null if already handled.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}

/**
 * An [Observer] for [Event]s, simplifying the pattern of checking if the [Event]'s content has
 * already been handled.
 *
 * [onEventUnhandledContent] is *only* called if the [Event]'s contents has not been handled.
 *
 * Example usage:
 * ```
 * viewModel.navigationEvent.observe(viewLifecycleOwner, EventObserver { id ->
 *     // This lambda is only called if event hasn't been handled
 *     findNavController().navigate(DetailFragmentDirections.actionToDetail(id))
 * })
 * ```
 */
class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(value: Event<T>) {
        value.getContentIfNotHandled()?.let { content ->
            onEventUnhandledContent(content)
        }
    }
}
