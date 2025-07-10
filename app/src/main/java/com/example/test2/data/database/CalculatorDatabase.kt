package com.example.test2.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.test2.data.model.CalculationEntry

/**
 * アプリのメインデータベース
 */
@Database(
    entities = [CalculationEntry::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CalculatorDatabase : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao

    companion object {
        @Volatile
        private var INSTANCE: CalculatorDatabase? = null

        fun getDatabase(context: Context): CalculatorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalculatorDatabase::class.java,
                    "calculator_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
