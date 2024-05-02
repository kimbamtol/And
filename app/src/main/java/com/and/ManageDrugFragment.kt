package com.and

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import com.and.adpater.CategoryListAdapter
import com.and.databinding.FragmentManageDrugBinding
import com.and.datamodel.DrugDataModel
import com.and.dialogfragment.AddDrugDialogFragment
import com.and.dialogfragment.SettingDrugDialogFragment

class ManageDrugFragment : Fragment() {
    private var _binding: FragmentManageDrugBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageDrugBinding.inflate(inflater, container, false)
        binding.apply {
            val categoryList = mutableListOf(DrugDataModel("Mon", mutableListOf("a", "b", "c")), DrugDataModel("Tues", mutableListOf("a", "b", "c")))
            val adapter = CategoryListAdapter(categoryList)
            adapter.onClickListener = CategoryListAdapter.OnClickListener { drugDataModel ->
                val settingDrugDialogFragment = SettingDrugDialogFragment().apply {
                    val bundle = Bundle()
                    bundle.putSerializable("categoryInfo", drugDataModel)
                    arguments = bundle
                }

                settingDrugDialogFragment.onButtonClickListener = object: SettingDrugDialogFragment.OnButtonClickListener {
                    override fun onRemoveCategoryBtnClick(removeDrugDataModel: DrugDataModel) {
                        val categoryIndex = categoryList.indexOf(removeDrugDataModel)
                        Log.d("savepoint", categoryIndex.toString())
                        if (categoryIndex != -1) {
                            categoryList.remove(removeDrugDataModel)
                            adapter.notifyItemRemoved(categoryIndex)
                        }
                    }

                    override fun onChangeNameBtnClick(
                        oldDrugDataModel: DrugDataModel,
                        newDrugDataModel: DrugDataModel
                    ) {
                        for (drugData in categoryList) {
                            if (drugData.category == newDrugDataModel.category) {
                                Toast.makeText(requireContext(), "같은 이름이 있어요!", Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                        val oldCategoryIndex = categoryList.indexOf(oldDrugDataModel)
                        if (oldCategoryIndex != -1) {
                            categoryList[oldCategoryIndex] = newDrugDataModel
                            adapter.notifyItemChanged(oldCategoryIndex)
                        }
                        settingDrugDialogFragment.dismiss()
                    }

                    override fun onRemoveDetailBtnClick(
                        selectedDrugDataModel: DrugDataModel,
                        selectedDetails: List<String>
                    ) {
                        categoryList[categoryList.indexOf(selectedDrugDataModel)].details.removeAll(selectedDetails)
                        adapter.notifyItemChanged(categoryList.indexOf(selectedDrugDataModel))
                    }
                }

                settingDrugDialogFragment.show(requireActivity().supportFragmentManager, "setting")

            }
            val itemAnimator = DefaultItemAnimator() // 리사이클러 뷰 아이템 변경시 애니메이션 설정
            itemAnimator.supportsChangeAnimations = false
            categoryRecyclerView.itemAnimator = itemAnimator
            categoryRecyclerView.adapter = adapter

            searchDrugBtn.setOnClickListener {
                if (searchview.visibility == View.GONE) {
                    searchview.visibility = View.VISIBLE
                    interval1.visibility = View.VISIBLE
                } else {
                    searchview.visibility = View.GONE
                    interval1.visibility = View.GONE
                }
            }

            addDrugBtn.setOnClickListener {
                val addDrugDialogFragment = AddDrugDialogFragment().apply {
                    val bundle = Bundle()
                    bundle.putParcelableArrayList("categoryList", ArrayList(categoryList))
                    arguments = bundle
                }
                addDrugDialogFragment.onButtonClickListener =
                    object : AddDrugDialogFragment.OnButtonClickListener {
                        override fun onAddCategoryBtnClick(addDrugDataModel: DrugDataModel) {
                            for (drugData in categoryList) {
                                if (drugData.category == addDrugDataModel.category) {
                                    Toast.makeText(requireContext(), "같은 이름이 있어요!", Toast.LENGTH_SHORT).show()
                                    return
                                }
                            }
                            categoryList.add(addDrugDataModel)
                            adapter.notifyItemInserted(categoryList.indexOf(addDrugDataModel))
                            addDrugDialogFragment.dismiss()
                        }

                        override fun onAddDetailBtnClick(
                            selectedCategorys: List<DrugDataModel>,
                            newDetails: List<String>
                        ) {
                            for(category in selectedCategorys) {
                                categoryList[categoryList.indexOf(category)].details.addAll(newDetails)
                                adapter.notifyItemChanged(categoryList.indexOf(category))
                            }
                        }
                    }
                addDrugDialogFragment.show(
                    requireActivity().supportFragmentManager,
                    "addDrug"
                )
            }

            searchview.setOnQueryTextListener(object :
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText) // 입력 값에 따라 도감 필터
                    return false
                }
            })

            buttonToNext.setOnClickListener {
                val intent = Intent(requireActivity(), ImageRecognitionActivity::class.java)
                startActivity(intent)

            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}