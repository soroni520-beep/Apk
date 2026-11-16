package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities =
    [
      TopUpPackageEntity::class,
      CategoryConfigEntity::class,
      TopUpOrderEntity::class,
      StoreSettingsEntity::class,
    ],
  version = 5,
  exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun topUpDao(): TopUpDao

  companion object {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE
        ?: synchronized(this) {
          INSTANCE
            ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "master_jd_topup.db",
              )
              .fallbackToDestructiveMigration(dropAllTables = true)
              .build()
              .also { INSTANCE = it }
        }
    }
  }
}
