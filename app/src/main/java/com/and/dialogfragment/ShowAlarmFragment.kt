package com.and.dialogfragment

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.and.alarm.AlarmSettingFragment
import com.and.R
import com.and.databinding.FragmentAlarmShowBinding
import com.and.datamodel.DrugDataModel
import com.and.datamodel.FirebaseDbAlarmDataModel
import java.util.Calendar

class ShowAlarmFragment : DialogFragment() {
    private var _binding: FragmentAlarmShowBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmShowBinding.inflate(inflater, container, false)
        val drugDataModel = arguments?.getParcelable("selectedCategory", DrugDataModel::class.java) ?: DrugDataModel()
        binding.apply {
            if (drugDataModel.category != "") {
                val firstAlarmInfo = getAlarmInfo(drugDataModel.firstAlarm)
                val secondAlarmInfo = getAlarmInfo(drugDataModel.secondAlarm)
                val thirdAlarmInfo = getAlarmInfo(drugDataModel.thirdAlarm)

                firstAlarm.text = firstAlarmInfo[0]
                secondAlarm.text = secondAlarmInfo[0]
                thirdAlarm.text = thirdAlarmInfo[0]

                weekAlarm.text = firstAlarmInfo[1] + " 복용중.."
            }

            editAlarmBtn.setOnClickListener {
                val alarmSettingFragment = AlarmSettingFragment().apply {
                    val bundle = Bundle()
                    if (drugDataModel.category == "") {
                        dismiss()
                        return@setOnClickListener
                    }
                    bundle.putParcelable("selectedCategory", drugDataModel)
                    arguments = bundle
                }
                changeFragment(alarmSettingFragment)
                dismiss()
            }
        }
        isCancelable = true
        this.dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.dialog?.window!!.setGravity(Gravity.BOTTOM)
        this.dialog?.window!!.attributes.y = 40
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        resizeDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun resizeDialog() {
        val params: ViewGroup.LayoutParams? = this.dialog?.window?.attributes
        val deviceWidth = Resources.getSystem().displayMetrics.widthPixels
        params?.width = (deviceWidth * 0.95).toInt()
        this.dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    private fun changeFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.screen_fl, fragment)
            .commitAllowingStateLoss()
    }

    private fun getAlarmInfo(alarm: FirebaseDbAlarmDataModel): List<String> {
        val time = alarm.time
        val setCalendar = Calendar.getInstance().apply {
            timeInMillis = time
        }
        val hour = setCalendar.get(Calendar.HOUR_OF_DAY)
        val minutes = setCalendar.get(Calendar.MINUTE)

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

        val weeks = alarm.week
        val weekWords = listOf("일", "월", "화", "수", "목", "금", "토")
        var onWeeks = ""

        for(i: Int in weeks.indices){
            if(weeks[i])
                onWeeks += weekWords[i]
        }

        return listOf(timeToText, onWeeks)
    }
}