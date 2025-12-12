package com.sm.maps.applib.presentation.base

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/**
 * Base Fragment class that provides common functionality for all Fragments
 * - Automatic ViewModel observation setup
 * - Common loading/error handling
 * - Lifecycle-aware operations
 *
 * @param layoutId The layout resource ID for this fragment
 */
abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    /**
     * Get the ViewModel for this fragment
     * Must be implemented by subclasses
     */
    abstract fun getViewModel(): BaseViewModel?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
    }

    /**
     * Setup views and click listeners
     * Called after onViewCreated
     */
    protected open fun setupViews() {
        // Override in subclasses to setup views
    }

    /**
     * Observe ViewModel LiveData
     * Automatically observes common BaseViewModel events
     */
    private fun observeViewModel() {
        getViewModel()?.let { viewModel ->
            // Observe loading state
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                handleLoading(isLoading)
            }

            // Observe error events
            viewModel.errorEvent.observe(viewLifecycleOwner, EventObserver { error ->
                handleError(error)
            })

            // Observe message events
            viewModel.messageEvent.observe(viewLifecycleOwner, EventObserver { message ->
                handleMessage(message)
            })
        }

        // Allow subclasses to observe their specific LiveData
        observeData()
    }

    /**
     * Observe fragment-specific LiveData
     * Override in subclasses to observe custom data
     */
    protected open fun observeData() {
        // Override in subclasses
    }

    /**
     * Handle loading state changes
     * Override to customize loading indicator behavior
     *
     * @param isLoading true if loading, false otherwise
     */
    protected open fun handleLoading(isLoading: Boolean) {
        // Override in subclasses to show/hide loading indicators
        // Example: progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    /**
     * Handle error messages
     * Override to customize error display
     *
     * @param error The error message to display
     */
    protected open fun handleError(error: String) {
        // Default: show toast
        Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_LONG).show()
    }

    /**
     * Handle general messages
     * Override to customize message display
     *
     * @param message The message to display
     */
    protected open fun handleMessage(message: String) {
        // Default: show toast
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Show a toast message
     */
    protected fun showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), message, length).show()
    }

    /**
     * Check if the fragment is safe to perform operations
     * (attached to context, not detached, view is available)
     */
    protected fun isSafeToPerformOperations(): Boolean {
        return isAdded && !isDetached && view != null
    }
}
