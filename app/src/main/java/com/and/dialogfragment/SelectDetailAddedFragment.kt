package com.and.dialogfragment

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.and.R
import com.and.adpater.SelectDetailListAdapter
import com.and.databinding.FragmentDeleteDetailBinding
import com.and.databinding.FragmentSelectDetailAddedBinding
import com.and.datamodel.DrugDataModel
import com.and.setting.NetworkManager
import com.and.viewModel.UserDataViewModel

class SelectDetailAddedFragment : DialogFragment() {
    private var _binding: FragmentSelectDetailAddedBinding? = null
    private val binding get() = _binding!!
    private val selectList = mutableListOf<String>()
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectDetailAddedBinding.inflate(inflater, container, false)
        val selectedCategory = arguments?.getParcelable("selectedCategory", DrugDataModel::class.java) ?: DrugDataModel()
        val recognizedTexts = arguments?.getStringArrayList("recognizedTexts") ?: arrayListOf()
        binding.apply {
            val adapter = SelectDetailListAdapter(recognizedTexts)
            adapter.onItemClickListener = SelectDetailListAdapter.OnItemClickListener {
                if(selectList.contains(it)) {
                    selectList.remove(it)
                    return@OnItemClickListener
                }
                selectList.add(it)
            }

            TextRecognizedRecyclerView.adapter = adapter

            nextBtn.setOnClickListener {
                if (!NetworkManager.checkNetworkState(requireContext())) {
                    return@setOnClickListener
                }
                userDataViewModel.addDetail(selectedCategory, selectList)
                dismiss()
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