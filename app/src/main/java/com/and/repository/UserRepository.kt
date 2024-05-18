package com.and.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.and.setting.FBRef
import com.and.setting.Setting
import com.and.datamodel.DrugDataModel
import com.and.datamodel.UserDataModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserRepository(private val viewModelScope: CoroutineScope) {
    private val userRef = FBRef.userRef.child(Setting.email)
    private val TAG = "UserRepository"  // 로그 태그 추가

    fun observeUser(
        userInfo: MutableLiveData<UserDataModel>,
        drugInfos: MutableLiveData<MutableList<DrugDataModel>>,
        successGetData: MutableLiveData<Boolean>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userInfoJob = async {
                    val dataSnapshot = userRef.child("userInfo").get().await()
                    Log.d(TAG, "UserInfo dataSnapshot: $dataSnapshot")  // 로그 추가
                    dataSnapshot.getValue(UserDataModel::class.java)
                }

                val drugInfoJob = suspendCoroutine<MutableList<DrugDataModel>> { continuation ->
                    userRef.child("category").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d(TAG, "Category dataSnapshot: $snapshot")  // 로그 추가
                            val categories = mutableListOf<DrugDataModel>()
                            if (snapshot.exists()) {
                                for (categoryData in snapshot.children) {
                                    val categoryName = categoryData.key.toString()
                                    val details = categoryData.child("details")
                                        .getValue<MutableList<String>>() ?: mutableListOf()
                                    val creationTime = categoryData.child("creationTime")
                                        .getValue(Long::class.java) ?: 0L
                                    categories.add(DrugDataModel(categoryName, details, creationTime))
                                }
                            }
                            continuation.resume(categories.sortedBy { it.creationTime }.toMutableList())
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "DatabaseError: $error")  // 로그 추가
                            continuation.resume(mutableListOf())
                        }
                    })
                }

                userInfo.postValue(userInfoJob.await())
                drugInfos.postValue(drugInfoJob)

                successGetData.postValue(true)
            } catch (e: Exception) {
                Log.e(TAG, "Exception in observeUser: ${e.message}")  // 예외 로그 추가
                successGetData.postValue(false)
            }
        }
    }

    fun addCategory(drugInfo: DrugDataModel) {
        userRef.child("category").child(drugInfo.category)
            .child("creationTime").setValue(System.currentTimeMillis())
    }

    fun changeCategoryName(oldDrugDataModel: DrugDataModel, newDrugDataModel: DrugDataModel) {
        userRef.child("category").child(oldDrugDataModel.category).removeValue()
        userRef.child("category").child(newDrugDataModel.category)
            .child("creationTime").setValue(newDrugDataModel.creationTime)
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
}
