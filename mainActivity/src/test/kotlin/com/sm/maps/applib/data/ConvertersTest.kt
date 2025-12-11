package com.sm.maps.applib.data

import com.sm.maps.applib.data.local.database.Converters
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setup() {
        converters = Converters()
    }

    @Test
    fun `fromTimestamp converts timestamp to Date correctly`() {
        // Given
        val timestamp = 1609459200L // 2021-01-01 00:00:00 UTC

        // When
        val result = converters.fromTimestamp(timestamp)

        // Then
        assertNotNull(result)
        assertEquals(1609459200000L, result?.time)
    }

    @Test
    fun `fromTimestamp returns null for null timestamp`() {
        // When
        val result = converters.fromTimestamp(null)

        // Then
        assertNull(result)
    }

    @Test
    fun `dateToTimestamp converts Date to timestamp correctly`() {
        // Given
        val date = Date(1609459200000L) // 2021-01-01 00:00:00 UTC

        // When
        val result = converters.dateToTimestamp(date)

        // Then
        assertNotNull(result)
        assertEquals(1609459200L, result)
    }

    @Test
    fun `dateToTimestamp returns null for null date`() {
        // When
        val result = converters.dateToTimestamp(null)

        // Then
        assertNull(result)
    }

    @Test
    fun `roundtrip conversion preserves timestamp`() {
        // Given
        val originalTimestamp = 1609459200L

        // When
        val date = converters.fromTimestamp(originalTimestamp)
        val resultTimestamp = converters.dateToTimestamp(date)

        // Then
        assertEquals(originalTimestamp, resultTimestamp)
    }

    @Test
    fun `roundtrip conversion preserves date`() {
        // Given
        val originalDate = Date(1609459200000L)

        // When
        val timestamp = converters.dateToTimestamp(originalDate)
        val resultDate = converters.fromTimestamp(timestamp)

        // Then
        assertEquals(originalDate.time, resultDate?.time)
    }
}
