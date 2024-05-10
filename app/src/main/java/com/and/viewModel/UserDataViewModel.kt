package com.and.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.and.repository.UserRepository
import com.and.datamodel.DrugDataModel
import com.and.datamodel.UserDataModel

class UserDataViewModel: ViewModel() {
    private val repository = UserRepository(viewModelScope)
    private val _userInfo = MutableLiveData<UserDataModel>()
    private val _drugInfos = MutableLiveData<MutableList<DrugDataModel>>()
    private val _successGetData = MutableLiveData<Boolean>()

    init {
        repository.observeUser(_userInfo, _drugInfos, _successGetData)
    }

    val userInfo : LiveData<UserDataModel>
        get() = _userInfo

    val drugInfos : LiveData<MutableList<DrugDataModel>>
        get() = _drugInfos

    val successGetData : LiveData<Boolean>
        get() = _successGetData

    fun addCategory(drugDataModel: DrugDataModel) {
        val newList = _drugInfos.value!!.also {
            it.add(drugDataModel)
        }
        _drugInfos.postValue(newList)
        repository.addCategory(drugDataModel)
    }

    fun changeCategoryName(oldDrugDataModel: DrugDataModel, newDrugDataModel: DrugDataModel) {
        val newList = _drugInfos.value!!.also {
            it[it.indexOf(oldDrugDataModel)] = newDrugDataModel
        }
        _drugInfos.postValue(newList)
        repository.changeCategoryName(oldDrugDataModel, newDrugDataModel)
    }

    fun removeCategory(drugDataModel: DrugDataModel) {
        val newList = _drugInfos.value!!.also {
            it.remove(drugDataModel)
        }
        _drugInfos.postValue(newList)
        repository.removeCategory(drugDataModel)
    }

    fun addDetail(selectedCategory: DrugDataModel, newDetails: List<String>) {
        val newList = _drugInfos.value!!.also {
            it[it.indexOf(selectedCategory)].details.addAll(newDetails)
        }
        _drugInfos.postValue(newList)
        repository.addDetail(newList[newList.indexOf(selectedCategory)])
    }

    fun removeDetail(selectedDrugDataModel: DrugDataModel, selectedDetails: List<String>) {
        val newList = _drugInfos.value!!.also {
            it[it.indexOf(selectedDrugDataModel)].details.removeAll(selectedDetails)
        }
        _drugInfos.postValue(newList)
        repository.removeDetail(newList[newList.indexOf(selectedDrugDataModel)])
    }
}