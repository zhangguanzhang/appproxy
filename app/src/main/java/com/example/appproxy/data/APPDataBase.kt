package com.example.appproxy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    version = 1,
    entities = [ProxyConfig::class],
    exportSchema = false
)
abstract class APPDataBase(): RoomDatabase() {
    abstract fun proxyConfigDao(): ProxyConfigDao

    companion object {
        @Volatile
        private var Instance: APPDataBase? = null

        fun getDatabase(context: Context): APPDataBase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, APPDataBase::class.java, "app_database")
                    /**
                     * Setting this option in your app's database builder means that Room
                     * permanently deletes all data from the tables in your database when it
                     * attempts to perform a migration with no defined migration path.
                     */
                    .fallbackToDestructiveMigration()
                    .build().also { Instance = it }
            }
        }

    }
}