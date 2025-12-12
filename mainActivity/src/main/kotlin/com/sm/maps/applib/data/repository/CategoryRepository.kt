package com.sm.maps.applib.data.repository

import com.sm.maps.applib.data.local.database.dao.CategoryDao
import com.sm.maps.applib.data.local.database.entity.CategoryEntity
import com.sm.maps.applib.di.IoDispatcher
import com.sm.maps.applib.domain.repository.ICategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of Category repository
 * Handles data operations for categories using Room database
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ICategoryRepository {

    override fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    override fun getVisibleCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getVisibleCategories()

    override suspend fun getCategoryById(id: Int): CategoryEntity? =
        withContext(ioDispatcher) {
            categoryDao.getCategoryById(id)
        }

    override suspend fun insertCategory(category: CategoryEntity): Long =
        withContext(ioDispatcher) {
            categoryDao.insert(category)
        }

    override suspend fun updateCategory(category: CategoryEntity) =
        withContext(ioDispatcher) {
            categoryDao.update(category)
        }

    override suspend fun deleteCategory(category: CategoryEntity) =
        withContext(ioDispatcher) {
            categoryDao.delete(category)
        }

    override suspend fun toggleCategoryVisibility(id: Int) =
        withContext(ioDispatcher) {
            categoryDao.toggleVisibility(id)
        }
}
