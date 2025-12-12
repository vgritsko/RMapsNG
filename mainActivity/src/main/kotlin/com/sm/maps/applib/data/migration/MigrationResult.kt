package com.sm.maps.applib.data.migration

/**
 * Represents the result of a data migration operation
 */
sealed class MigrationResult {
    /**
     * Migration completed successfully
     */
    data object Success : MigrationResult()

    /**
     * Migration already completed previously
     */
    data object AlreadyMigrated : MigrationResult()

    /**
     * Migration failed with an exception
     */
    data class Failed(val exception: Exception) : MigrationResult()

    /**
     * Migration skipped (user choice or configuration)
     */
    data object Skipped : MigrationResult()
}
