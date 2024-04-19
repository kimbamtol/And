package com.and.dialogfragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.and.DrugDataModel
import com.and.R
import com.and.databinding.FragmentSelectAddDetailWayBinding

class SelectAddDetailWayFragment : DialogFragment() {
    private var _binding: FragmentSelectAddDetailWayBinding? = null
    private val binding get() = _binding!!

    fun interface OnAddDetailListener {
        fun onAddDetail(detail: List<String>)
    }

    var onAddDetailListener: OnAddDetailListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectAddDetailWayBinding.inflate(inflater, container, false)
        binding.apply {
            writeBtn.setOnClickListener {
                val writeDialogFragment = WriteDialogFragment()
                writeDialogFragment.clickYesListener = WriteDialogFragment.OnClickYesListener {
                    onAddDetailListener?.onAddDetail(listOf(it))
                }
                writeDialogFragment.show(requireActivity().supportFragmentManager, "writeDetail")
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