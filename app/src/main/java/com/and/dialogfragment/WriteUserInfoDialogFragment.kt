package com.and.dialogfragment

import android.app.DatePickerDialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.and.MainActivity
import com.and.databinding.FragmentWriteUserInfoDialogBinding
import com.and.setting.NetworkManager
import com.and.viewModel.UserDataViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WriteUserInfoDialogFragment : DialogFragment() {
    private var _binding: FragmentWriteUserInfoDialogBinding? = null
    private val binding get() = _binding!!
    private val userDataViewModel: UserDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWriteUserInfoDialogBinding.inflate(inflater, container, false)
        binding.apply {
            viewmodel = userDataViewModel

            writeBirth.setOnClickListener {
                val cal = Calendar.getInstance()
                val dateCallback = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                    val birth = "${year}/${month + 1}/${day}"
                    if (getAge(birth) == -1) {
                        Toast.makeText(requireContext(), "올바른 생일을 입력 해주세요!", Toast.LENGTH_SHORT).show()
                        return@OnDateSetListener
                    }

                    binding.writeBirth.text = birth
                }

                val datePicker = DatePickerDialog(
                    requireContext(),
                    dateCallback,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.datePicker.maxDate = cal.timeInMillis
                datePicker.show()
            }

            cancelButton.setOnClickListener {
                dismiss()
            }

            finishButton.setOnClickListener {
                if(!NetworkManager.checkNetworkState(requireContext()) || !userDataViewModel.successGetData.value!!) {
                    return@setOnClickListener
                }
                if (writeName.text.toString() == "" || writeBirth.text == "") {
                    return@setOnClickListener
                }
                userDataViewModel.setUserInfo(writeName.text.toString(), writeBirth.text.toString())
                dismiss()
            }
        }
        isCancelable = false
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

    private fun getAge(date: String): Int {
        val currentDate = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val birthDate = dateFormat.parse(date)
        val calBirthDate = Calendar.getInstance().apply { time = birthDate }

        var age = currentDate.get(Calendar.YEAR) - calBirthDate.get(Calendar.YEAR)
        if (currentDate.get(Calendar.DAY_OF_YEAR) < calBirthDate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }
}