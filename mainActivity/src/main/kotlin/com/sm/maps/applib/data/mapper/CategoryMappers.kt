package com.sm.maps.applib.data.mapper

import com.sm.maps.applib.data.local.database.entity.CategoryEntity
import com.sm.maps.applib.domain.model.Category

/**
 * Extension function to convert CategoryEntity to Category domain model
 */
fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        hidden = hidden != 0,
        iconId = iconId,
        minZoom = minZoom
    )
}

/**
 * Extension function to convert Category domain model to CategoryEntity
 */
fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        hidden = if (hidden) 1 else 0,
        iconId = iconId,
        minZoom = minZoom
    )
}

/**
 * Extension function to convert list of CategoryEntity to list of Category
 */
fun List<CategoryEntity>.toDomain(): List<Category> = map { it.toDomain() }

/**
 * Extension function to convert list of Category to list of CategoryEntity
 */
fun List<Category>.toEntity(): List<CategoryEntity> = map { it.toEntity() }
