package com.and.dialogfragment

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.and.databinding.FragmentWriteDialogBinding

class WriteDialogFragment : DialogFragment() {
    private var _binding: FragmentWriteDialogBinding? = null
    private val binding get() = _binding!!

    fun interface OnClickYesListener {
        fun onClick(text: String)
    }

    var clickYesListener: OnClickYesListener? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWriteDialogBinding.inflate(inflater, container, false)
        binding.apply {
            finishButton.setOnClickListener {
                clickYesListener?.onClick(write.text.toString())
                dismiss()
            }

            cancelButton.setOnClickListener {
                dismiss()
            }
        }
        isCancelable = false
        resizeDialog()
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    private fun resizeDialog() {
        val params: ViewGroup.LayoutParams? = this.dialog?.window?.attributes
        val deviceWidth = Resources.getSystem().displayMetrics.widthPixels
        params?.width = (deviceWidth * 0.8).toInt()
        this.dialog?.window?.attributes = params as WindowManager.LayoutParams
    }
}