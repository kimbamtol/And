package com.and.dialogfragment

import android.content.Context
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
import com.and.datamodel.DrugDataModel
import com.and.databinding.FragmentAddDrugDialogBinding

class AddDrugDialogFragment : DialogFragment() {
    interface OnButtonClickListener {
        fun onAddCategoryBtnClick(addDrugDataModel: DrugDataModel)
        fun onAddDetailBtnClick(selectedCategorys: List<DrugDataModel>, newDetails: List<String>)
    }

    var onButtonClickListener: OnButtonClickListener? = null

    private var _binding: FragmentAddDrugDialogBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDrugDialogBinding.inflate(inflater, container, false)
        val categoryList =
            arguments?.getParcelableArrayList("categoryList", DrugDataModel::class.java)
                ?: arrayListOf()

        binding.apply {
            addCategoryBtn.setOnClickListener {
                val writeDialogFragment = WriteDialogFragment()
                writeDialogFragment.clickYesListener = WriteDialogFragment.OnClickYesListener {
                    onButtonClickListener?.onAddCategoryBtnClick(DrugDataModel(it))
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
                selectCategoryDialogFragment.onSuccessAddDetailsListener =
                    SelectCategoryDialogFragment.OnSuccessAddDetailsListener { selectedCategorys, newDetails ->
                        onButtonClickListener?.onAddDetailBtnClick(selectedCategorys, newDetails)
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