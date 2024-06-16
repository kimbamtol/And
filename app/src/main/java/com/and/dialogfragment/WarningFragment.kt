package com.and.dialogfragment

import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.databinding.BindingAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.and.ManageDrugFragment
import com.and.adpater.CategoryListAdapter
import com.and.adpater.WarningListAdapter
import com.and.databinding.FragmentWarningBinding
import com.and.datamodel.DrugDataModel
import com.and.viewModel.UserDataViewModel

class WarningFragment : DialogFragment() {

    private var _binding: FragmentWarningBinding? = null
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWarningBinding.inflate(inflater, container, false)
        binding.apply {
            fragment = this@WarningFragment
            viewModel = userDataViewModel
            lifecycleOwner = requireActivity()
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

    object WarningRecyclerViewBindingAdapter {
        @BindingAdapter("listData", "viewmodel", "fragment")
        @JvmStatic
        fun bindData(
            recyclerView: RecyclerView,
            warningList: List<String>?,
            viewModel: UserDataViewModel,
            fragment: WarningFragment
        ) {
            recyclerView.itemAnimator = null
            if (recyclerView.adapter == null) {
                val adapter = WarningListAdapter()
                adapter.onItemClickListener = WarningListAdapter.OnItemLongClickListener {
                    val builder = AlertDialog.Builder(fragment.requireContext())
                    builder.setTitle("삭제하시겠습니까?")
                    val listener = DialogInterface.OnClickListener { _, ans ->
                        when (ans) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                viewModel.removeWarningInfo(it)
                            }
                        }
                    }
                    builder.setPositiveButton("네", listener)
                    builder.setNegativeButton("아니오", null)
                    builder.show()

                }

                recyclerView.adapter = adapter
            }
            warningList?.let {
                val myAdapter = recyclerView.adapter as WarningListAdapter
                myAdapter.submitList(it.toMutableList())
            }
        }
    }
}