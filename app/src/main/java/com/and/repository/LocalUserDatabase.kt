package com.and.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.and.datamodel.AlarmDao
import com.and.datamodel.RoomDbAlarmDataModel
import com.and.datamodel.WeekListConverters

@Database(entities = [RoomDbAlarmDataModel::class], version = 1)
@TypeConverters(WeekListConverters::class)
abstract class LocalUserDatabase: RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        private var instance: LocalUserDatabase? = null

        @Synchronized
        fun getInstance(context: Context): LocalUserDatabase? {
            if (instance == null) {
                synchronized(LocalUserDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        LocalUserDatabase::class.java,
                        "user-database"
                    ).build()
                }
            }
            return instance
        }
    }
}