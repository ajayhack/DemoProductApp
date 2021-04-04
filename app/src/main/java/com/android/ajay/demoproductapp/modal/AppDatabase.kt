package com.android.ajay.demoproductapp.modal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * =========Written By Ajay Thakur (4th April 2021)==========
 **/

@Database(
    entities = [ProductTable::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    //region==============Accessing Dao for further uses:-
    abstract fun dao(): AppDao?
    //endregion

    companion object {
        private var dbInstance: AppDatabase? = null
        private var dbName: String = "product_db"

        @Synchronized
        fun getDatabase(context: Context): AppDatabase? {
            if (dbInstance == null) {
                dbInstance = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return dbInstance
        }

        //region===============================Below method is to clean up DB Instance:-
        fun cleanUpDB() {
            dbInstance = null
        }
        //endregion
    }
}