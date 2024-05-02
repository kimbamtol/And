package com.and.adpater

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.and.datamodel.DrugDataModel
import com.and.databinding.CategorylistItemBinding

class CategoryListAdapter(private val drugDataModelList: List<DrugDataModel>): RecyclerView.Adapter<CategoryListAdapter.CategoryListViewHodlder>(),
    Filterable {
    fun interface OnClickListener {
        fun onSettingClick(drugDataModel: DrugDataModel)
    }

    private var drugDataModels: List<DrugDataModel> = drugDataModelList
    var onClickListener: OnClickListener? = null

    inner class CategoryListViewHodlder(private val binding: CategorylistItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(drugDataModel: DrugDataModel) {
            binding.apply {
                categoryName.text = drugDataModel.category
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

        fun bind() {
            binding.apply {
                categoryItem.visibility = View.INVISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryListViewHodlder {
        val binding = CategorylistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryListViewHodlder(binding)
    }

    override fun getItemCount(): Int = drugDataModels.size + 2

    override fun onBindViewHolder(holder: CategoryListViewHodlder, position: Int) {
        if (position >= drugDataModels.size) {
            holder.bind()
        } else {
            holder.bind(drugDataModels[position])
        }
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