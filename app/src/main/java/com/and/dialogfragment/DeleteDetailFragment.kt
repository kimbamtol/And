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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.and.datamodel.DrugDataModel
import com.and.adpater.RemoveDetailListAdapter
import com.and.databinding.FragmentDeleteDetailBinding

class DeleteDetailFragment : DialogFragment() {

    fun interface OnRemoveButtonClickListener {
        fun onRemoveBtnClick(details: List<String>)
    }

    private var _binding: FragmentDeleteDetailBinding? = null
    private val binding get() = _binding!!
    private val removeList = mutableListOf<String>()
    var onRemoveButtonClickListener: OnRemoveButtonClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeleteDetailBinding.inflate(inflater, container, false)
        val categoryInfo = arguments?.getSerializable("categoryInfo", DrugDataModel::class.java)?: DrugDataModel()
        binding.apply {
            val adapter = RemoveDetailListAdapter(categoryInfo.details)
            adapter.onItemClickListener = RemoveDetailListAdapter.OnItemClickListener {
                if(removeList.contains(it)) {
                    removeList.remove(it)
                    Log.d("savepoint", removeList.toString())
                    return@OnItemClickListener
                }

                removeList.add(it)
                Log.d("savepoint", removeList.toString())
            }
            removeDetaisRecyclerView.adapter = adapter
            removeDetailsBtn.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("삭제 할까요?")
                val listener = DialogInterface.OnClickListener { _, ans ->
                    when (ans) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            onRemoveButtonClickListener?.onRemoveBtnClick(removeList)
                            dismiss()
                        }
                    }
                }
                builder.setPositiveButton("네", listener)
                builder.setNegativeButton("아니요", null)
                builder.show()
            }

        }
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