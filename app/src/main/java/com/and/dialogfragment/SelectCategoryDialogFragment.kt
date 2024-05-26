package com.and.dialogfragment

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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.and.datamodel.DrugDataModel
import com.and.adpater.SelectCategoryListAdapter
import com.and.databinding.FragmentSelectCategoryDialogBinding
import com.and.viewModel.UserDataViewModel

class SelectCategoryDialogFragment : DialogFragment() {
    private var _binding: FragmentSelectCategoryDialogBinding? = null
    private val binding get() = _binding!!
    private var selectedCategory = DrugDataModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectCategoryDialogBinding.inflate(inflater, container, false)
        val categoryList = arguments?.getParcelableArrayList("categoryList", DrugDataModel::class.java)?: arrayListOf()
        binding.apply {
            val adapter = SelectCategoryListAdapter(categoryList)
            adapter.onItemClickListener = SelectCategoryListAdapter.OnItemClickListener {
                if(selectedCategory == it) {
                    selectedCategory = DrugDataModel()
                    return@OnItemClickListener
                }
                selectedCategory = it
            }

            selectCategoryRecyclerView.adapter = adapter

            selectCategoryBtn.setOnClickListener {
                if (selectedCategory.category == "") {
                    Toast.makeText(requireContext(), "카테고리를 선택 해주세요!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val selectAddDetailWayFragment = SelectAddDetailWayFragment().apply {
                    val bundle = Bundle()
                    bundle.putParcelable("selectedList", selectedCategory)
                    arguments = bundle
                }
                selectAddDetailWayFragment.show(requireActivity().supportFragmentManager, "selectWays")
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