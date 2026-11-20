package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ChemicalPhRecord
import com.example.data.model.FreshnessVisionRecord
import com.example.data.model.ProductItem
import com.example.data.model.StorageMonitorRecord
import com.example.data.model.StudentTestAttempt
import com.example.data.model.SurveyResponse

@Database(
    entities = [
        ProductItem::class,
        SurveyResponse::class,
        StudentTestAttempt::class,
        FreshnessVisionRecord::class,
        ChemicalPhRecord::class,
        StorageMonitorRecord::class
    ],
    version = 3,
    exportSchema = false
)
abstract class FoodScanDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun surveyDao(): SurveyDao
    abstract fun studentTestDao(): StudentTestDao
    abstract fun foodScan20Dao(): FoodScan20Dao

    companion object {
        @Volatile
        private var INSTANCE: FoodScanDatabase? = null

        fun getDatabase(context: Context): FoodScanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoodScanDatabase::class.java,
                    "foodscan_ai_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
