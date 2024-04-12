package com.and

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import androidx.fragment.app.DialogFragment
import com.and.databinding.FragmentAddDrugDialogBinding

class AddDrugDialogFragment : DialogFragment() {
    interface OnButtonClickListener {
        fun onAddCategoryBtnClick(text: String)
        fun onAddDetailBtnClick()
    }

    var onButtonClickListener: OnButtonClickListener? = null

    private var _binding: FragmentAddDrugDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDrugDialogBinding.inflate(inflater, container, false)
        binding.apply {
            addCategoryBtn.setOnClickListener {
                val writeDialogFragment = WriteDialogFragment()
                writeDialogFragment.clickYesListener = WriteDialogFragment.OnClickYesListener {
                    onButtonClickListener?.onAddCategoryBtnClick(it)
                }
                writeDialogFragment.show(requireActivity().supportFragmentManager, "writeCategory")
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