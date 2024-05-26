package com.and.alarm

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.and.adpater.SelectDetailListAdapter
import com.and.databinding.ActivityCheckAlarmBinding
import com.and.datamodel.DrugDataModel
import com.and.setting.Setting
import com.and.viewModel.UserDataViewModel

class CheckAlarmActivity : AppCompatActivity() {
    lateinit var binding: ActivityCheckAlarmBinding
    private lateinit var dataViewModel: UserDataViewModel
    private val selectedList = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Setting.email = "ghlwns10@kakao"
        dataViewModel = ViewModelProvider(this).get(UserDataViewModel::class.java)
        val alarmCode = intent?.getIntExtra("alarmCode", 0)
        Log.d("savepoint", alarmCode.toString())
        dataViewModel.successGetData.observe(this) {
            var drugDataModel = DrugDataModel()
            for (category in dataViewModel.drugInfos.value!!) {
                if (alarmCode == category.firstAlarm.alarmCode || alarmCode == category.secondAlarm.alarmCode || alarmCode == category.thirdAlarm.alarmCode) {
                    drugDataModel = category
                    break
                }
            }

            binding.apply {
                drugs = drugDataModel
                lifecycleOwner = this@CheckAlarmActivity
                val adapter = SelectDetailListAdapter(drugDataModel.details)
                adapter.onItemClickListener = SelectDetailListAdapter.OnItemClickListener {
                    if (selectedList.contains(it)) {
                        selectedList.remove(it)
                        return@OnItemClickListener
                    }
                    selectedList.add(it)
                }
                alarmRecyclerView.adapter = adapter
                saveCheckInfoBtn.setOnClickListener {
                }
            }
        }
    }
}