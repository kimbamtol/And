package com.and.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.and.datamodel.AlarmDao
import com.and.datamodel.DrugDataModel
import com.and.datamodel.FirebaseDbAlarmDataModel
import com.and.datamodel.RoomDbAlarmDataModel
import com.and.datamodel.TimeLineDataModel
import com.and.datamodel.UserDataModel
import com.and.setting.FBRef
import com.and.setting.NetworkManager
import com.and.setting.Setting
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserRepository(private val application: Application) {
    private val userRef = FBRef.userRef.child(Setting.email)
    private val alarmDao: AlarmDao
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val TAG = "UserRepository"  // 로그 태그 추가

    init {
        val db = LocalUserDatabase.getInstance(application)
        alarmDao = db!!.alarmDao()
    }

    suspend fun observeUser(
        userInfo: MutableLiveData<UserDataModel>,
        drugInfos: MutableLiveData<MutableList<DrugDataModel>>,
        timeLineInfos: MutableLiveData<HashMap<String, MutableList<TimeLineDataModel>>>,
        warningInfos: MutableLiveData<MutableList<String>>,
        successGetData: MutableLiveData<Boolean>
    ) {
        if(!NetworkManager.checkNetworkState(application)) {
            successGetData.postValue(false)
            return
        }

        try {
            val userInfoJob = coroutineScope.async {
                val dataSnapshot = userRef.child("userInfo").get().await()
                Log.d(TAG, "UserInfo dataSnapshot: $dataSnapshot")  // 로그 추가
                dataSnapshot.getValue(UserDataModel::class.java)
            }

            val drugInfoJob = suspendCoroutine { continuation ->
                userRef.child("category")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d(TAG, "Category dataSnapshot: $snapshot")  // 로그 추가
                            val categories = mutableListOf<DrugDataModel>()
                            if (snapshot.exists()) {
                                for (categoryData in snapshot.children) {
                                    val categoryName = categoryData.key.toString()
                                    categories.add(
                                        DrugDataModel(
                                            categoryName, (categoryData.child("details")
                                                .getValue<MutableList<String>>()
                                                ?: mutableListOf<String>()),
                                            categoryData.child("creationTime")
                                                .getValue(Long::class.java) ?: 0L,
                                            categoryData.child("firstAlarm")
                                                .getValue(FirebaseDbAlarmDataModel::class.java)
                                                ?: FirebaseDbAlarmDataModel(),
                                            categoryData.child("secondAlarm")
                                                .getValue(FirebaseDbAlarmDataModel::class.java)
                                                ?: FirebaseDbAlarmDataModel(),
                                            categoryData.child("thirdAlarm")
                                                .getValue(FirebaseDbAlarmDataModel::class.java)
                                                ?: FirebaseDbAlarmDataModel()
                                        )
                                    )
                                }
                            }

                            continuation.resume(categories.sortedBy { it.creationTime }
                                .toMutableList())
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "DatabaseError: $error")  // 로그 추가
                            continuation.resume(mutableListOf<DrugDataModel>())
                        }
                    })
            }

            val timeLineInfoJob = suspendCoroutine { continuation ->
                userRef.child("timeLine")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d(TAG, "TimeLine dataSnapshot: $snapshot")  // 로그 추가
                            var timeLines = HashMap<String, MutableList<TimeLineDataModel>>()
                            if (snapshot.exists()) {
                                timeLines = snapshot.getValue<HashMap<String, MutableList<TimeLineDataModel>>>()!!
                            }
                            continuation.resume(timeLines)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "DatabaseError: $error")  // 로그 추가
                            continuation.resume(hashMapOf<String, MutableList<TimeLineDataModel>>())
                        }
                    })
            }

            val warningInfoJob = suspendCoroutine { continuation ->
                userRef.child("warning")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var warnings = mutableListOf<String>()
                            if (snapshot.exists()) {
                                warnings = snapshot.getValue<MutableList<String>>()!!
                            }
                            continuation.resume(warnings)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "DatabaseError: $error")  // 로그 추가
                            continuation.resume(mutableListOf<String>())
                        }
                    })
            }

            userInfo.postValue(userInfoJob.await())
            drugInfos.postValue(drugInfoJob)
            timeLineInfos.postValue(timeLineInfoJob)
            warningInfos.postValue(warningInfoJob)
            successGetData.postValue(true)

        } catch (e: Exception) {
            Log.e(TAG, "Exception in observeUser: ${e.message}")  // 예외 로그 추가
            successGetData.postValue(false)
        }
    }

    fun setUserInfo(name: String, birth: String) {
        userRef.child("userInfo").child("name").setValue(name)
        userRef.child("userInfo").child("birth").setValue(birth)
    }

    fun addCategory(drugInfo: DrugDataModel) {
        userRef.child("category").child(drugInfo.category)
            .setValue(drugInfo)
    }

    fun changeCategoryName(oldDrugDataModel: DrugDataModel, newDrugDataModel: DrugDataModel) {
        userRef.child("category").child(oldDrugDataModel.category).removeValue()
        userRef.child("category").child(newDrugDataModel.category).setValue(oldDrugDataModel)
        userRef.child("category").child(newDrugDataModel.category).child("category").setValue(oldDrugDataModel.category)
    }

    fun removeCategory(drugInfo: DrugDataModel) {
        userRef.child("category").child(drugInfo.category).removeValue()
    }

    fun addDetail(addedDetailsCategory: DrugDataModel) {
        userRef.child("category").child(addedDetailsCategory.category).child("details")
            .setValue(addedDetailsCategory.details)
    }

    fun removeDetail(removedDetailsCategory: DrugDataModel) {
        userRef.child("category").child(removedDetailsCategory.category).child("details")
            .setValue(removedDetailsCategory.details)
    }

    fun addTimeLine(timeLineMap: HashMap<String, MutableList<TimeLineDataModel>>) {
        userRef.child("timeLine").setValue(timeLineMap)
    }

    fun addWarningInfo(warningList: MutableList<String>) {
        userRef.child("warning").setValue(warningList)
    }

    fun getAlarmList() : List<RoomDbAlarmDataModel> {
        return alarmDao.getAlarmsList()
    }

    fun addAlarm(alarm: RoomDbAlarmDataModel) {
        alarmDao.addAlarm(alarm)
    }

    fun deleteAlarm(alarmCode: Int) {
        alarmDao.deleteAlarm(alarmCode)
    }

    fun updateAlarmTime(code: Int, time: Long) {
        alarmDao.updateAlarmTime(code, time)
    } // 알람을 반복 적으로 울리기 위한 함수. 데이터 베이스를 업데이트 하는 함수x.

    fun updateFirebaseAlarmTime(
        drugDataModel: DrugDataModel,
        alarmModels: List<FirebaseDbAlarmDataModel>)
    {
        val alarmNames = listOf("firstAlarm", "secondAlarm", "thirdAlarm")
        for (i in alarmNames.indices) {
            userRef.child("category").child(drugDataModel.category).child(alarmNames[i])
                .setValue(alarmModels[i])
        }
    }

    fun deleteInfo() {
        userRef.removeValue()
    }
}

