package com.example.test2.data.database

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room用のTypeConverter
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
