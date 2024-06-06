package com.and.alarm

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.and.MainActivity
import com.and.ManageDrugFragment
import com.and.R
import com.and.databinding.FragmentAlarmSettingBinding
import com.and.datamodel.DrugDataModel
import com.and.datamodel.FirebaseDbAlarmDataModel
import com.and.datamodel.RoomDbAlarmDataModel
import com.and.dialogfragment.TimePickerBottomSheetFragment
import com.and.setting.NetworkManager
import com.and.viewModel.UserDataViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.system.exitProcess

class AlarmSettingFragment : Fragment() {
    private var _binding: FragmentAlarmSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainactivity: MainActivity
    private val userDataViewModel: UserDataViewModel by activityViewModels()

    private var firstAlarmHour = 7
    private var firstAlarmMinutes = 0
    private var secondAlarmHour = 13
    private var secondAlarmMinutes = 0
    private var thirdAlarmHour = 19
    private var thirdAlarmMinutes = 0

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("나가시겠습니까?")
            val listener = DialogInterface.OnClickListener { _, ans ->
                when (ans) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        val manageDrugFragment = ManageDrugFragment()
                        mainactivity.changeFragment(manageDrugFragment)
                    }
                }
            }

            builder.setPositiveButton("네", listener)
            builder.setNegativeButton("아니오", null)
            builder.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainactivity = requireActivity() as MainActivity
        mainactivity.binding.menuBn.visibility = View.GONE
        if (!NetworkManager.checkNetworkState(requireContext())) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("인터넷을 연결해주세요!")
            builder.setPositiveButton("네", null)
            builder.setCancelable(false)
            builder.show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmSettingBinding.inflate(inflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

        val existingDrugDataModel = arguments?.getParcelable("selectedCategory", DrugDataModel::class.java) ?: DrugDataModel()
        val newCategory = arguments?.getString("newCategory") ?: ""
        val currentTimeLong = System.currentTimeMillis()
        val currentTimeInt = (System.currentTimeMillis() / 1000).toInt()
        val tempString = currentTimeInt.toString().substring(1)
        binding.apply {
            btnGoManage.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("나가시겠습니까?")
                val listener = DialogInterface.OnClickListener { _, ans ->
                    when (ans) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val manageDrugFragment = ManageDrugFragment()
                            mainactivity.changeFragment(manageDrugFragment)
                        }
                    }
                }

                builder.setPositiveButton("네", listener)
                builder.setNegativeButton("아니오", null)
                builder.show()
            }

            firstAlarm.setOnClickListener {
                showTimePicker(firstAlarmHour, firstAlarmMinutes) { hour, minutes ->
                    firstAlarmHour = hour
                    firstAlarmMinutes = minutes
                    firstAlarm.text = getAlarmText(hour, minutes)
                }
            }

            secondAlarm.setOnClickListener {
                showTimePicker(secondAlarmHour, secondAlarmMinutes) { hour, minutes ->
                    secondAlarmHour = hour
                    secondAlarmMinutes = minutes
                    secondAlarm.text = getAlarmText(hour, minutes)
                }
            }

            thirdAlarm.setOnClickListener {
                showTimePicker(thirdAlarmHour, thirdAlarmMinutes) { hour, minutes ->
                    thirdAlarmHour = hour
                    thirdAlarmMinutes = minutes
                    thirdAlarm.text = getAlarmText(hour, minutes)
                }
            }

            if(existingDrugDataModel.category != "") {
                setWeeks(existingDrugDataModel.firstAlarm)
                getAlarmHourAndMinutes(existingDrugDataModel.firstAlarm.time).also {
                    firstAlarmHour = it[0]
                    firstAlarmMinutes = it[1]
                }
                getAlarmHourAndMinutes(existingDrugDataModel.secondAlarm.time).also {
                    secondAlarmHour = it[0]
                    secondAlarmMinutes = it[1]
                }
                getAlarmHourAndMinutes(existingDrugDataModel.thirdAlarm.time).also {
                    thirdAlarmHour = it[0]
                    thirdAlarmMinutes = it[1]
                }

                val firstAlarmInfo = getAlarmTextFromDataModel(firstAlarmHour, firstAlarmMinutes)
                val secondAlarmInfo = getAlarmTextFromDataModel(secondAlarmHour, secondAlarmMinutes)
                val thirdAlarmInfo = getAlarmTextFromDataModel(thirdAlarmHour, thirdAlarmMinutes)

                firstAlarm.text = firstAlarmInfo
                secondAlarm.text = secondAlarmInfo
                thirdAlarm.text = thirdAlarmInfo
            }

            saveAlarmBtn.setOnClickListener {
                if(!NetworkManager.checkNetworkState(requireContext())) {
                    return@setOnClickListener
                }

                val weeks = arrayListOf(
                    sunday.isChecked,
                    monday.isChecked,
                    tuesday.isChecked,
                    wednesday.isChecked,
                    thursday.isChecked,
                    friday.isChecked,
                    saturday.isChecked
                )

                val firstAlarmTime = getAlarmTime(firstAlarmHour, firstAlarmMinutes)
                val secondAlarmTime = getAlarmTime(secondAlarmHour, secondAlarmMinutes)
                val thirdAlarmTime = getAlarmTime(thirdAlarmHour, thirdAlarmMinutes)


                val firstAlarmCode = getAlarmCode(firstAlarmHour, firstAlarmMinutes, weeks)
                val secondAlarmCode = getAlarmCode(secondAlarmHour, secondAlarmMinutes, weeks)
                val thirdAlarmCode = getAlarmCode(thirdAlarmHour, thirdAlarmMinutes, weeks)

                if (firstAlarmCode == "") {
                    return@setOnClickListener
                }

                if (firstAlarmCode == secondAlarmCode || secondAlarmCode == thirdAlarmCode || thirdAlarmCode == firstAlarmCode) {
                    Toast.makeText(requireContext(), "같은 시간의 알람이 존재 해요!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val firstFirebaseDbAlarmDataModel = FirebaseDbAlarmDataModel(tempString.toInt(), firstAlarmCode.toInt(), firstAlarmTime, weeks)
                val secondFirebaseDbAlarmDataModel = FirebaseDbAlarmDataModel(currentTimeInt, secondAlarmCode.toInt(), secondAlarmTime, weeks)
                val thirdFirebaseDbAlarmDataModel = FirebaseDbAlarmDataModel("-$tempString".toInt(), thirdAlarmCode.toInt(), thirdAlarmTime, weeks)

                val firstRoomDbAlarmDataModel = RoomDbAlarmDataModel(tempString.toInt(), firstAlarmCode.toInt(), firstAlarmTime, weeks)
                val secondRoomDbAlarmDataModel = RoomDbAlarmDataModel(currentTimeInt, secondAlarmCode.toInt(), secondAlarmTime, weeks)
                val thirdRoomDbAlarmDataModel = RoomDbAlarmDataModel("-$tempString".toInt(), thirdAlarmCode.toInt(), thirdAlarmTime, weeks)

                try {
                    if (newCategory != "") {
                        userDataViewModel.addCategory(
                            DrugDataModel(
                                category = newCategory,
                                creationTime = currentTimeLong,
                                firstAlarm = firstFirebaseDbAlarmDataModel,
                                secondAlarm = secondFirebaseDbAlarmDataModel,
                                thirdAlarm = thirdFirebaseDbAlarmDataModel
                            )
                        )

                        val addAlarmJob =  CoroutineScope(Dispatchers.IO).launch {
                            settingAlarm(firstRoomDbAlarmDataModel)
                            settingAlarm(secondRoomDbAlarmDataModel)
                            settingAlarm(thirdRoomDbAlarmDataModel)
                        }

                        addAlarmJob.invokeOnCompletion {
                            val manageDrugFragment = ManageDrugFragment()
                            mainactivity.changeFragment(manageDrugFragment)
                        }

                    } else if (existingDrugDataModel.category != "") {
                        val changeAlarmDrugDataModel = existingDrugDataModel.copy()
                        userDataViewModel.updateFirebaseAlarmTime(
                            existingDrugDataModel,
                            listOf(
                                firstFirebaseDbAlarmDataModel,
                                secondFirebaseDbAlarmDataModel,
                                thirdFirebaseDbAlarmDataModel
                            )
                        )

                        val changeAlarmJob = CoroutineScope(Dispatchers.IO).launch {
                            removeAlarm(changeAlarmDrugDataModel.firstAlarm)
                            removeAlarm(changeAlarmDrugDataModel.secondAlarm)
                            removeAlarm(changeAlarmDrugDataModel.thirdAlarm)
                            settingAlarm(firstRoomDbAlarmDataModel)
                            settingAlarm(secondRoomDbAlarmDataModel)
                            settingAlarm(thirdRoomDbAlarmDataModel)
                        }

                        changeAlarmJob.invokeOnCompletion {
                            val manageDrugFragment = ManageDrugFragment()
                            mainactivity.changeFragment(manageDrugFragment)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun showTimePicker(hour: Int, minutes: Int, callback: TimePickerBottomSheetFragment.OnClickSaveBtnListener) {
        val timePickerBottomSheetFragment = TimePickerBottomSheetFragment().apply {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
            val bundle = Bundle()
            bundle.putInt("hour", hour)
            bundle.putInt("minutes", minutes)
            arguments = bundle
        }
        timePickerBottomSheetFragment.onClickSaveBtnListener = callback
        timePickerBottomSheetFragment.show(requireActivity().supportFragmentManager, "bottomSheet")
    }

    private fun getAlarmTime(hour: Int, minutes: Int): Long {
        val alarmCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
        }

        checkAlarmTime(alarmCalendar)
        return alarmCalendar.timeInMillis
    }
    private fun checkAlarmTime(calendar: Calendar) {
        if (System.currentTimeMillis() > calendar.timeInMillis) {
            calendar.add(Calendar.DATE, 1)
        }
    }

    private fun getAlarmCode(hour: Int, minutes: Int, weeks: ArrayList<Boolean>): String {
        var code = if (hour < 10) {
            "-" + String.format("%2d", hour).replace(" ", "1") + String.format(
                "%02d",
                minutes
            )
        } else {
            hour.toString() + String.format("%02d", minutes)
        }

        code += if (weeksToCode(weeks) == "") {
            Toast.makeText(requireContext(), "요일을 선택 해주세요!", Toast.LENGTH_SHORT).show()
            return ""
        } else {
            weeksToCode(weeks)
        }

        return code
    }

    private fun weeksToCode(weeks: ArrayList<Boolean>): String {
        var code = ""
        for (i: Int in weeks.indices) {
            if (weeks[i]) {
                code += i.toString()
            }
        }

        if (code.length == 6) {
            code = when (code) {
                "123456" -> "00"
                "023456" -> "11"
                "013456" -> "22"
                "012456" -> "33"
                "012356" -> "44"
                "012346" -> "55"
                else -> "66"
            }
        } else if (code.length == 7) {
            code = "7"
        }

        return code
    }

    private fun setWeeks(alarm: FirebaseDbAlarmDataModel) {
        val weeks = alarm.week
        binding.apply {
            val weeksButton = listOf(sunday, monday, tuesday, wednesday, thursday, friday, saturday)
            for (i: Int in weeks.indices) {
                weeksButton[i].isChecked = weeks[i]
            }
        }
    }

    private fun getAlarmHourAndMinutes(alarmTime: Long): List<Int> {
        val setCalendar = Calendar.getInstance().apply {
            timeInMillis = alarmTime
        }
        val hour = setCalendar.get(Calendar.HOUR_OF_DAY)
        val minutes = setCalendar.get(Calendar.MINUTE)

        return listOf(hour, minutes)
    }

    private fun getAlarmTextFromDataModel(hour: Int, minutes: Int): String {
        var timeToText = ""
        timeToText += if (hour > 12) {
            "오후 " + (hour - 12).toString()
        } else if (hour == 12) {
            "오후 $hour"
        } else if (hour == 0) {
            "오전 12"
        } else {
            "오전 $hour"
        }

        timeToText += ":${String.format("%02d", minutes)}"

        return timeToText
    }

    private fun getAlarmText(hour: Int, minutes: Int): String {
        var timeToText = ""
        timeToText += if (hour > 12) {
            "오후 " + (hour - 12).toString()
        } else if (hour == 12) {
            "오후 $hour"
        } else if (hour == 0) {
            "오전 12"
        } else {
            "오전 $hour"
        }

        timeToText += ":${String.format("%02d", minutes)}"

        return timeToText
    }

    private fun settingAlarm(roomDbAlarmDataModel: RoomDbAlarmDataModel) {
        val alarmFunctions = AlarmFunctions(requireContext())
        alarmFunctions.callAlarm(roomDbAlarmDataModel.time, roomDbAlarmDataModel.alarmCode, roomDbAlarmDataModel.week.toTypedArray())
        userDataViewModel.addAlarm(roomDbAlarmDataModel)
    }

    private fun removeAlarm(firebaseDbAlarmDataModel: FirebaseDbAlarmDataModel) {
        val alarmFunctions = AlarmFunctions(requireContext())
        alarmFunctions.cancelAlarm(firebaseDbAlarmDataModel.alarmCode)
        userDataViewModel.deleteAlarm(firebaseDbAlarmDataModel.alarmCode)
    }
}