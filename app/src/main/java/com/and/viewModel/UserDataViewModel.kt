package com.and.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.and.repository.UserRepository
import com.and.datamodel.DrugDataModel
import com.and.datamodel.FirebaseDbAlarmDataModel
import com.and.datamodel.RoomDbAlarmDataModel
import com.and.datamodel.TimeLineDataModel
import com.and.datamodel.UserDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserDataViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository(application)
    private val _userInfo = MutableLiveData<UserDataModel>()
    private val _drugInfos = MutableLiveData<MutableList<DrugDataModel>>()
    private val _timeLineInfos = MutableLiveData<HashMap<String, MutableList<TimeLineDataModel>>>()
    private val _successGetData = MutableLiveData<Boolean>()
    private val TAG = "UserDataViewModel"  // 로그 태그 추가

    init {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "Initializing UserDataViewModel")
            repository.observeUser(_userInfo, _drugInfos, _timeLineInfos, _successGetData)
        }
    }

    val userInfo: LiveData<UserDataModel>
        get() = _userInfo

    val drugInfos: LiveData<MutableList<DrugDataModel>>
        get() = _drugInfos

    val timeLineInfos: LiveData<HashMap<String, MutableList<TimeLineDataModel>>>
        get() = _timeLineInfos

    val successGetData: LiveData<Boolean>
        get() = _successGetData

    suspend fun observeUser() {
        repository.observeUser(_userInfo, _drugInfos, _timeLineInfos, _successGetData)
    }

    fun setUserInfo(name: String, birth: String) {
        try {
            val newUserInfo = UserDataModel(name, birth, _userInfo.value!!.myEmail)
            _userInfo.postValue(newUserInfo)
            repository.setUserInfo(name, birth)
        } catch (e: Exception) {
            return
        }
    }

    fun addCategory(drugDataModel: DrugDataModel) {
        Log.d(TAG, "Adding category: $drugDataModel")
        val newList = _drugInfos.value!!.also {
            it.add(drugDataModel)
        }
        _drugInfos.postValue(newList)
        repository.addCategory(drugDataModel)
    }

    fun changeCategoryName(oldDrugDataModel: DrugDataModel, newDrugDataModel: DrugDataModel) {
        Log.d(TAG, "Changing category name from ${oldDrugDataModel.category} to ${newDrugDataModel.category}")
        val newList = _drugInfos.value!!.also {
            it[it.indexOf(oldDrugDataModel)] = newDrugDataModel
        }
        _drugInfos.postValue(newList)
        repository.changeCategoryName(oldDrugDataModel, newDrugDataModel)
    }

    fun removeCategory(drugDataModel: DrugDataModel) {
        Log.d(TAG, "Removing category: $drugDataModel")
        val newList = _drugInfos.value!!.also {
            it.remove(drugDataModel)
        }
        _drugInfos.postValue(newList)
        repository.removeCategory(drugDataModel)
    }

    fun addDetail(selectedCategory: DrugDataModel, newDetails: List<String>) {
        Log.d(TAG, "Adding details: $newDetails to category: ${selectedCategory.category}")
        val newList = mutableListOf<DrugDataModel>()
        _drugInfos.value!!.forEach {
            newList.add(it.copy())
        }

        val categoryNum = newList.indexOf(selectedCategory)
        newList.also {
            it[categoryNum].details.addAll(newDetails)
            it[categoryNum].details = it[categoryNum].details.distinct().toMutableList()
        }

        _drugInfos.postValue(newList)
        repository.addDetail(newList[categoryNum])
    }

    fun removeDetail(selectedDrugDataModel: DrugDataModel, selectedDetails: List<String>) {
        Log.d(TAG, "Removing details: $selectedDetails from category: ${selectedDrugDataModel.category}")
        val newList = mutableListOf<DrugDataModel>()
        _drugInfos.value!!.forEach {
            newList.add(it.copy())
        }

        val categoryNum = newList.indexOf(selectedDrugDataModel)
        newList.also {
            it[categoryNum].details.removeAll(selectedDetails)
        }

        _drugInfos.postValue(newList)
        repository.removeDetail(newList[categoryNum])
    }

    fun updateDrugInfo(updatedDrugDataModel: DrugDataModel) {
        Log.d(TAG, "Updating drug info: $updatedDrugDataModel")
        val newList = _drugInfos.value!!.also {
            val index = it.indexOfFirst { drugDataModel -> drugDataModel.category == updatedDrugDataModel.category }
            if (index != -1) {
                it[index] = updatedDrugDataModel
            }
        }
        _drugInfos.postValue(newList)
        repository.addDetail(updatedDrugDataModel)
    }

    fun addTimeLine(day: String, timeLineDataModel: TimeLineDataModel) {
        val newMap = _timeLineInfos.value!!.also {
            if(!it.containsKey(day)) {
                it[day] = mutableListOf()
            }
            it[day]?.add(timeLineDataModel)
        }
        _timeLineInfos.postValue(newMap)
        repository.addTimeLine(newMap)
    }

    fun removeTimeLine(day: String, timeLineDataModel: TimeLineDataModel) {
        val newMap = _timeLineInfos.value!!.also {
            it[day]!!.remove(timeLineDataModel)
            if(it[day]!!.isEmpty()) {
                it.remove(day)
            }
        }
        _timeLineInfos.postValue(newMap)
        repository.addTimeLine(newMap)
    }

    fun getTimeLine(day: String): List<TimeLineDataModel> {
        return (_timeLineInfos.value?.get(day)?.sortedBy { it.creationTime }) ?: mutableListOf()
    }

    fun getAlarmList(): List<RoomDbAlarmDataModel> {
        return repository.getAlarmList()
    }

    fun addAlarm(alarmModels: RoomDbAlarmDataModel) {
        repository.addAlarm(alarmModels)
    }

    fun deleteAlarm(alarmCode: Int) {
        repository.deleteAlarm(alarmCode)
    }

    fun updateFirebaseAlarmTime(drugDataModel: DrugDataModel, alarmModels: List<FirebaseDbAlarmDataModel>) {
        val newList = _drugInfos.value!!.also {
            it[it.indexOf(drugDataModel)].firstAlarm = alarmModels[0]
            it[it.indexOf(drugDataModel)].secondAlarm = alarmModels[1]
            it[it.indexOf(drugDataModel)].thirdAlarm = alarmModels[2]
        }
        _drugInfos.postValue(newList)
        repository.updateFirebaseAlarmTime(drugDataModel, alarmModels)
    }

    fun deleteInfo() {
        repository.deleteInfo()
    }
}

