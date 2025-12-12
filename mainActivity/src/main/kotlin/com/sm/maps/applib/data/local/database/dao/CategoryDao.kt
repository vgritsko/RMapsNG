package com.sm.maps.applib.data.local.database.dao

import androidx.room.*
import com.sm.maps.applib.data.local.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY name")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM category WHERE hidden = 0 ORDER BY name")
    fun getVisibleCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM category WHERE categoryid = :id")
    suspend fun getCategoryById(id: Int): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("UPDATE category SET hidden = 1 - hidden WHERE categoryid = :id")
    suspend fun toggleVisibility(id: Int)

    // Synchronous methods for data migration
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategorySync(category: CategoryEntity): Long
}
