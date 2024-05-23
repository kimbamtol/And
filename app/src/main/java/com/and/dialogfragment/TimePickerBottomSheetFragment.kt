package com.and.dialogfragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.and.R
import com.and.databinding.FragmentTimePickerBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
class TimePickerBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentTimePickerBottomSheetBinding? = null
    private val binding get() = _binding!!

    fun interface OnClickSaveBtnListener {
      fun onClickSaveBtn(hour: Int, minutes: Int)
    }

    var onClickSaveBtnListener: OnClickSaveBtnListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimePickerBottomSheetBinding.inflate(inflater, container, false)
        val hour = arguments?.getInt("hour") ?: 0
        val minutes = arguments?.getInt("minutes") ?: 0
        binding.apply {
            saveAlarm.setOnClickListener {
                onClickSaveBtnListener?.onClickSaveBtn(timepicker.hour, timepicker.minute)
                dismiss()
            }

            exist.setOnClickListener {
                dismiss()
            }

            timepicker.hour = hour
            timepicker.minute = minutes
        }
        this.dialog?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        this.dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}