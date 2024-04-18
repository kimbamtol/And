package com.and.dialogfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.and.DrugDataModel
import com.and.databinding.FragmentAddDrugDialogBinding
import java.util.ArrayList

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
        val categoryList = arguments?.getParcelableArrayList("categoryList", DrugDataModel::class.java)?: arrayListOf()
        binding.apply {
            addCategoryBtn.setOnClickListener {
                val writeDialogFragment = WriteDialogFragment()
                writeDialogFragment.clickYesListener = WriteDialogFragment.OnClickYesListener {
                    onButtonClickListener?.onAddCategoryBtnClick(DrugDataModel(it))
                }
                writeDialogFragment.show(requireActivity().supportFragmentManager, "writeCategory")
            }

            addDetailBtn.setOnClickListener {
                val selectCategoryDialogFragment = SelectCategoryDialogFragment().apply {
                    val bundle = Bundle()
                    bundle.putParcelableArrayList("categoryList", categoryList)
                    arguments = bundle
                }
                selectCategoryDialogFragment.onSuccessAddDetailsListener = SelectCategoryDialogFragment.OnSuccessAddDetailsListener { selectedCategorys, newDetails ->
                    onButtonClickListener?.onAddDetailBtnClick(selectedCategorys, newDetails)
                }
                selectCategoryDialogFragment.show(requireActivity().supportFragmentManager, "selectCategory")
                dismiss()
            }
        }
        isCancelable = true
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}