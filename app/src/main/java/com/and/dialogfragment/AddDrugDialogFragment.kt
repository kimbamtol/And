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
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.and.MainActivity
import com.and.R
import com.and.alarm.AlarmSettingFragment
import com.and.datamodel.DrugDataModel
import com.and.databinding.FragmentAddDrugDialogBinding
import com.and.viewModel.UserDataViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddDrugDialogFragment : DialogFragment() {
    private lateinit var mainactivity: MainActivity

    private var _binding: FragmentAddDrugDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainactivity = requireActivity() as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDrugDialogBinding.inflate(inflater, container, false)
        val categoryList = arguments?.getParcelableArrayList("categoryList", DrugDataModel::class.java) ?: arrayListOf()
        binding.apply {
            addCategoryBtn.setOnClickListener {
                val writeDialogFragment = WriteDialogFragment()
                writeDialogFragment.clickYesListener = WriteDialogFragment.OnClickYesListener {
                    for (drugData in categoryList) {
                        if (drugData.category == it) {
                            mainactivity.toastMessage("같은 이름이 있어요!")
                            return@OnClickYesListener
                        }
                    }

                    val alarmSettingFragment = AlarmSettingFragment().apply {
                        val bundle = Bundle()
                        bundle.putString("newCategory", it)
                        arguments = bundle
                    }
                    mainactivity.changeFragment(alarmSettingFragment)
                }
                writeDialogFragment.show(requireActivity().supportFragmentManager, "writeCategory")
                dismiss()
            }


            addDetailBtn.setOnClickListener {
                val selectCategoryDialogFragment = SelectCategoryDialogFragment().apply {
                    val bundle = Bundle()
                    bundle.putParcelableArrayList("categoryList", categoryList)
                    arguments = bundle
                }

                selectCategoryDialogFragment.show(
                    requireActivity().supportFragmentManager,
                    "selectCategory"
                )
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
}