package com.and.adpater

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.and.datamodel.DrugDataModel
import com.and.databinding.CategorylistItemBinding

class CategoryListAdapter(private val drugDataModelList: List<DrugDataModel>): RecyclerView.Adapter<CategoryListAdapter.CategoryListViewHolder>(),
    Filterable {
    fun interface OnClickListener {
        fun onSettingClick(drugDataModel: DrugDataModel)
    }

    private var drugDataModels: List<DrugDataModel> = drugDataModelList
    var onClickListener: OnClickListener? = null

    inner class CategoryListViewHolder(private val binding: CategorylistItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(drugDataModel: DrugDataModel) {
            binding.apply {
                binding.category = drugDataModel

                val adapter = DetailListAdapter(drugDataModel.details)
                detailRecyclerView.adapter = adapter

                setting.setOnClickListener {
                    onClickListener?.onSettingClick(drugDataModel)
                }

                root.setOnClickListener {
                    if (detailRecyclerView.visibility == View.GONE) {
                        detailRecyclerView.visibility = View.VISIBLE
                    } else {
                        detailRecyclerView.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryListViewHolder {
        val binding = CategorylistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryListViewHolder(binding)
    }

    override fun getItemCount(): Int = drugDataModels.size

    override fun onBindViewHolder(holder: CategoryListViewHolder, position: Int) {
        holder.bind(drugDataModels[position])
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint.toString()
                drugDataModels = if(charString.isEmpty()) {
                    drugDataModelList
                } else {
                    val filteredList = mutableListOf<DrugDataModel>()
                    for(drugDataModel in drugDataModelList) {
                        if(drugDataModel.category.lowercase().contains(charString.lowercase())) {
                            filteredList.add(drugDataModel)
                        }
                    }
                    filteredList
                }
                val filterResult = FilterResults()
                filterResult.values = drugDataModels
                return filterResult
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                drugDataModels = results?.values as List<DrugDataModel>
                notifyDataSetChanged()
            }
        }
    }
}