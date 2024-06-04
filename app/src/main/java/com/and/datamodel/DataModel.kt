package com.and.datamodel

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@kotlinx.parcelize.Parcelize
data class DrugDataModel(
    val category: String = "",
    var details: MutableList<String> = mutableListOf(),
    val creationTime: Long = 0,
    var firstAlarm: FirebaseDbAlarmDataModel = FirebaseDbAlarmDataModel(),
    var secondAlarm: FirebaseDbAlarmDataModel = FirebaseDbAlarmDataModel(),
    var thirdAlarm: FirebaseDbAlarmDataModel  = FirebaseDbAlarmDataModel()
) :  Parcelable {
    fun copy(): DrugDataModel {
        val copyDetails = mutableListOf<String>()
        this.details.forEach {
            copyDetails.add(it)
        }

        return DrugDataModel(category, copyDetails, creationTime, firstAlarm, secondAlarm, thirdAlarm)
    }
}

data class UserDataModel(
    val name: String = "",
    val birth: String = "",
    val myEmail: String = ""
)

data class TimeLineDataModel(
    val time: String = "",
    val creationTime: Long = 0,
    val context: String = ""
)

@kotlinx.parcelize.Parcelize
data class FirebaseDbAlarmDataModel(
    val alarmCode: Int = 0,
    val code: Int = 0,
    val time: Long = 0,
    val week: ArrayList<Boolean> = arrayListOf()
) : Parcelable


@Entity(tableName = "Alarm")
@kotlinx.parcelize.Parcelize
data class RoomDbAlarmDataModel(
    @PrimaryKey(autoGenerate = true)
    val alarmCode: Int,
    val code: Int,
    val time: Long,
    val week: ArrayList<Boolean>
) : Parcelable


class WeekListConverters {
    @TypeConverter
    fun listToJson(value: ArrayList<Boolean>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): ArrayList<Boolean>? {
        val type = object : TypeToken<ArrayList<Boolean>?>() {}.type
        return Gson().fromJson(value, type)
    }
}

@Dao
interface AlarmDao {
    @Query("select * from Alarm")
    fun getAlarmsList() : List<RoomDbAlarmDataModel> // 반면 일반적인 리스트를 반환 할때는 코루틴을 사용해 반환 받아야 함.

    @Insert(onConflict = OnConflictStrategy.REPLACE) // 알람은 중복되지 않게 저장
    fun addAlarm(item: RoomDbAlarmDataModel)

    @Query("DELETE FROM Alarm WHERE alarmCode = :alarmCode") // 알람 코드로 삭제
    fun deleteAlarm(alarmCode: Int)

    @Query("UPDATE Alarm SET time = :time WHERE alarmCode = :alarmCode")
    fun updateAlarmTime(alarmCode: Int, time: Long)
}