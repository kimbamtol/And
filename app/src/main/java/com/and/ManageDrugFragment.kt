package com.and

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.and.adpater.CategoryListAdapter
import com.and.databinding.FragmentManageDrugBinding
import com.and.datamodel.DrugDataModel
import com.and.dialogfragment.AddDrugDialogFragment
import com.and.dialogfragment.SettingDrugDialogFragment
import com.and.dialogfragment.ShowAlarmFragment
import com.and.viewModel.UserDataViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageDrugFragment : Fragment() {
    private var _binding: FragmentManageDrugBinding? = null
    private val binding get() = _binding!!
    private val userDataViewModel: UserDataViewModel by activityViewModels()

    private lateinit var mainactivity: MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainactivity = requireActivity() as MainActivity
        mainactivity.binding.menuBn.visibility = View.VISIBLE
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.POST_NOTIFICATIONS), 999)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageDrugBinding.inflate(inflater, container, false)
        binding.apply {
            fragment = this@ManageDrugFragment
            viewModel = userDataViewModel
            lifecycleOwner = requireActivity()
            categoryRecyclerView.itemAnimator = null

            addDrugBtn.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    if (userDataViewModel.getAlarmList().isEmpty() && (userDataViewModel.drugInfos.value?: mutableListOf()).isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "마이페이지의 알람 불러오기를 클릭 해주세요!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                       withContext(Dispatchers.IO) {
                           val addDrugDialogFragment = AddDrugDialogFragment().apply {
                               val bundle = Bundle()
                               bundle.putParcelableArrayList("categoryList", ArrayList((categoryRecyclerView.adapter as CategoryListAdapter).currentList))
                               arguments = bundle
                           }

                           addDrugDialogFragment.show(
                               requireActivity().supportFragmentManager,
                               "addDrug"
                           )
                       }
                    }
                }
            }

            searchview.setOnQueryTextListener(object :
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    (categoryRecyclerView.adapter as CategoryListAdapter).filter.filter(newText)
                    return false
                }
            })

            searchDrugBtn.setOnClickListener {
                if (searchview.visibility == View.GONE) {
                    searchview.visibility = View.VISIBLE
                    interval1.visibility = View.VISIBLE
                } else {
                    searchview.visibility = View.GONE
                    interval1.visibility = View.GONE
                }
            }

            buttonToNext.setOnClickListener {
                val intent = Intent(requireActivity(), ImageRecognitionActivity::class.java)
                startActivity(intent)
            }
            return binding.root
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    object RecyclerViewBindingAdapter {
        @BindingAdapter("listData", "fragment")
        @JvmStatic
        fun bindData(recyclerView: RecyclerView, categoryList: List<DrugDataModel>?, fragment: ManageDrugFragment){
            recyclerView.itemAnimator = null
            if(recyclerView.adapter == null){
                val adapter = CategoryListAdapter()
                adapter.onClickListener = object: CategoryListAdapter.OnClickListener {
                    override fun onSettingClick(drugDataModel: DrugDataModel) {
                        val settingDrugDialogFragment = SettingDrugDialogFragment().apply {
                            val bundle = Bundle()
                            bundle.putParcelable("categoryInfo", drugDataModel)
                            arguments = bundle
                        }
                        settingDrugDialogFragment.show(fragment.requireActivity().supportFragmentManager, "setting")
                    }

                    override fun onAlarmClick(drugDataModel: DrugDataModel) {
                        val alarmSettingFragment = ShowAlarmFragment().apply {
                            val bundle = Bundle()
                            bundle.putParcelable("selectedCategory", drugDataModel)
                            arguments = bundle
                        }
                        alarmSettingFragment.show(fragment.requireActivity().supportFragmentManager, "alarm")
                    }
                }
                recyclerView.adapter = adapter
            }
            categoryList?.let{
                val myAdapter = recyclerView.adapter as CategoryListAdapter
                myAdapter.setData(it.toMutableList())
            }
        }
    }
}
