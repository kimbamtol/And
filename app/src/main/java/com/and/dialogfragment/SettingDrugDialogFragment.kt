package com.and.dialogfragment

import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.and.MainActivity
import com.and.alarm.AlarmFunctions
import com.and.datamodel.DrugDataModel
import com.and.databinding.FragmentSettingDrugDialogBinding
import com.and.datamodel.FirebaseDbAlarmDataModel
import com.and.datamodel.RoomDbAlarmDataModel
import com.and.setting.NetworkManager
import com.and.viewModel.UserDataViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingDrugDialogFragment : DialogFragment() {
    private var _binding: FragmentSettingDrugDialogBinding? = null
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private lateinit var mainactivity: MainActivity

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainactivity = requireActivity() as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingDrugDialogBinding.inflate(inflater, container, false)

        val categoryInfo = (arguments?.getParcelable("categoryInfo", DrugDataModel::class.java)?: DrugDataModel()).copy()
        val categoryList = userDataViewModel.drugInfos.value!!

        binding.apply {
            removeCategoryBtn.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("삭제 할까요?")
                val listener = DialogInterface.OnClickListener { _, ans ->
                    when (ans) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            if (!NetworkManager.checkNetworkState(requireContext())) {
                                return@OnClickListener
                            }
                            try {
                                userDataViewModel.removeCategory(categoryInfo)
                                val removeJob = CoroutineScope(Dispatchers.IO).launch {
                                    removeAlarm(categoryInfo.firstAlarm)
                                    removeAlarm(categoryInfo.secondAlarm)
                                    removeAlarm(categoryInfo.thirdAlarm)
                                }

                                removeJob.invokeOnCompletion {
                                    dismiss()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(requireContext(), "오류로 삭제하지 못했습니다.", Toast.LENGTH_SHORT).show()
                                dismiss()
                                return@OnClickListener
                            }
                        }
                    }
                }
                builder.setPositiveButton("네", listener)
                builder.setNegativeButton("아니요", null)
                builder.show()
            }

            changeNameBtn.setOnClickListener {
                if (!NetworkManager.checkNetworkState(requireContext())) {
                    return@setOnClickListener
                }
                val writeDialogFragment = WriteDialogFragment()
                writeDialogFragment.clickYesListener =
                    WriteDialogFragment.OnClickYesListener { categoryName ->
                        try {
                            for (drugData in categoryList) {
                                if (drugData.category == categoryName) {
                                    mainactivity.toastMessage("같은 이름이 있어요!")
                                    return@OnClickYesListener
                                }
                            }

                            userDataViewModel.changeCategoryName(
                                categoryInfo, DrugDataModel(
                                    categoryName,
                                    categoryInfo.details,
                                    categoryInfo.creationTime,
                                    categoryInfo.firstAlarm,
                                    categoryInfo.secondAlarm,
                                    categoryInfo.thirdAlarm
                                )
                            )
                        } catch (e: Exception) {
                            return@OnClickYesListener
                        }
                    }

                writeDialogFragment.show(requireActivity().supportFragmentManager, "changeCategory")
                dismiss()
            }

            removeDetailBtn.setOnClickListener {
                if(categoryInfo.details.isEmpty()) {
                    Toast.makeText(requireContext(), "Detail이 없어요!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val deleteDetailFragment = DeleteDetailFragment().apply {
                    val bundle = Bundle()
                    bundle.putParcelable("categoryInfo", categoryInfo)
                    arguments = bundle
                }
                deleteDetailFragment.show(requireActivity().supportFragmentManager, "deleteDetails")
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

    private fun removeAlarm(firebaseDbAlarmDataModel: FirebaseDbAlarmDataModel) {
        val alarmFunctions = AlarmFunctions(requireContext())
        alarmFunctions.cancelAlarm(firebaseDbAlarmDataModel.alarmCode)
        userDataViewModel.deleteAlarm(firebaseDbAlarmDataModel.alarmCode)
    }
}