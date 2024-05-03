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