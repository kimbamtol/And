package com.and.repository

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
    private val userRef =  FBRef.userRef.child(Setting.email)

    fun observeUser(
        userInfo: MutableLiveData<UserDataModel>,
        drugInfos: MutableLiveData<MutableList<DrugDataModel>>,
        successGetData: MutableLiveData<Boolean>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val userInfoJob = async {
                userRef.child("userInfo").get().await()
                    .getValue(UserDataModel::class.java)
            }

            val drugInfoJob = suspendCoroutine { continuation ->
                userRef.child("category").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var categories = mutableListOf<DrugDataModel>()
                        if (snapshot.exists()) {
                            for (categoryData in snapshot.children) {
                                val categoryName = categoryData.key.toString()
                                categories.add(DrugDataModel(categoryName, (categoryData.child("details")
                                    .getValue<MutableList<String>>() ?: mutableListOf<String>()),
                                    categoryData.child("creationTime").getValue(Long::class.java) ?: 0L
                                ))
                            }
                        }
                        categories = categories.sortedBy { it.creationTime }.toMutableList()
                        continuation.resume(categories)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(mutableListOf<DrugDataModel>())
                    }
                })
            }

            userInfo.postValue(userInfoJob.await())
            drugInfos.postValue(drugInfoJob)

            successGetData.postValue(true)
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