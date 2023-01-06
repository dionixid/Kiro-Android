package id.dionix.kiro.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import id.dionix.kiro.model.SurahProperties

@Database(entities = [SurahProperties::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val surahDao: SurahDao

    companion object {
        @Volatile
        private lateinit var mInstance: AppDatabase

        fun initialize(context: Context) {
            if (!this::mInstance.isInitialized) {
                synchronized(this) {
                    mInstance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
        }

        fun getInstance(): AppDatabase {
            if (!this::mInstance.isInitialized) {
                throw IllegalStateException("Attempt to access AppDatabase before it was initialized.")
            }
            return mInstance
        }
    }
}