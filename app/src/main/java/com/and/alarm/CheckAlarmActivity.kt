package com.and.alarm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.and.LoginActivity
import com.and.MainActivity
import com.and.adpater.SelectDetailListAdapter
import com.and.databinding.ActivityCheckAlarmBinding
import com.and.datamodel.DrugDataModel
import com.and.datamodel.TimeLineDataModel
import com.and.setting.Setting
import com.and.viewModel.UserDataViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

class CheckAlarmActivity : AppCompatActivity() {
    lateinit var binding: ActivityCheckAlarmBinding
    private lateinit var dataViewModel: UserDataViewModel
    private lateinit var loginInfo: android.content.SharedPreferences
    private val selectedList = mutableListOf<String>()

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            Toast.makeText(this@CheckAlarmActivity, "뒤로 가기가 불가능 합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, callback)

        loginInfo = getSharedPreferences("setting", MODE_PRIVATE)
        val loginId = loginInfo.getString("email", null)
        if (loginId != null) {
            Setting.email = loginId
        } else {
            Toast.makeText(this, "알약 정보를 가져오지 못 하였습니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        dataViewModel = ViewModelProvider(this).get(UserDataViewModel::class.java)
        val alarmCode = intent?.getIntExtra("alarmCode", 0)
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
                    if(drugDataModel.details.size == 0) {
                        Toast.makeText(this@CheckAlarmActivity, "알약이 없어요!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@CheckAlarmActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        return@setOnClickListener
                    }

                    if (drugDataModel.details.size == selectedList.size) {
                        val day = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                        val timeLineDataModel = TimeLineDataModel(time, System.currentTimeMillis(),"${drugDataModel.category}의 알약을 복용했습니다.")

                        dataViewModel.addTimeLine(day, timeLineDataModel)

                        val intent = Intent(this@CheckAlarmActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        this.onBackPressedDispatcher.addCallback(this, callback)
    }
}