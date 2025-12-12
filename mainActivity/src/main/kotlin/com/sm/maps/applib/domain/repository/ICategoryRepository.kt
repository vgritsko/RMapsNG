package com.sm.maps.applib.domain.repository

import com.sm.maps.applib.data.local.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Category data operations
 * Follows clean architecture - defines contract for data layer
 */
interface ICategoryRepository {

    /**
     * Get all categories as a Flow
     */
    fun getAllCategories(): Flow<List<CategoryEntity>>

    /**
     * Get only visible categories (not hidden)
     */
    fun getVisibleCategories(): Flow<List<CategoryEntity>>

    /**
     * Get a specific category by ID
     */
    suspend fun getCategoryById(id: Int): CategoryEntity?

    /**
     * Insert a new category
     * @return the ID of the inserted category
     */
    suspend fun insertCategory(category: CategoryEntity): Long

    /**
     * Update an existing category
     */
    suspend fun updateCategory(category: CategoryEntity)

    /**
     * Delete a category
     */
    suspend fun deleteCategory(category: CategoryEntity)

    /**
     * Toggle category visibility (show/hide)
     */
    suspend fun toggleCategoryVisibility(id: Int)
}
