package com.and.dialogfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.and.DrugDataModel
import com.and.adpater.SelectCategoryListAdapter
import com.and.databinding.FragmentSelectCategoryDialogBinding

class SelectCategoryDialogFragment : DialogFragment() {
    private var _binding: FragmentSelectCategoryDialogBinding? = null
    private val binding get() = _binding!!
    private val selectList = mutableListOf<DrugDataModel>()

    fun interface OnSuccessAddDetailsListener {
        fun onSuccessAddDetails(selectedCategorys: List<DrugDataModel>, newDetails: List<String>)
    }

    var onSuccessAddDetailsListener: OnSuccessAddDetailsListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectCategoryDialogBinding.inflate(inflater, container, false)
        val categoryList = arguments?.getParcelableArrayList("categoryList", DrugDataModel::class.java)?: arrayListOf()
        binding.apply {
            val adapter = SelectCategoryListAdapter(categoryList)
            adapter.onItemClickListener = SelectCategoryListAdapter.OnItemClickListener {
                if(selectList.contains(it)) {
                    selectList.remove(it)
                    return@OnItemClickListener
                }
                selectList.add(it)
            }

            selectCategoryRecyclerView.adapter = adapter

            selectCategoryBtn.setOnClickListener {
                if (selectList.isEmpty()) {
                    Toast.makeText(requireContext(), "카테고리를 선택 해주세요!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val selectAddDetailWayFragment = SelectAddDetailWayFragment()
                selectAddDetailWayFragment.onAddDetailListener = SelectAddDetailWayFragment.OnAddDetailListener {
                    onSuccessAddDetailsListener?.onSuccessAddDetails(selectList, it)
                }
                selectAddDetailWayFragment.show(requireActivity().supportFragmentManager, "selectWays")
                dismiss()
            }

            exitBtn.setOnClickListener {
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