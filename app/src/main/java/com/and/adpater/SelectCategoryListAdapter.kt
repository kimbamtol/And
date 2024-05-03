package com.and.adpater

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.and.datamodel.DrugDataModel
import com.and.databinding.SelectcategorylistItemBinding

class SelectCategoryListAdapter(private val drugDataModelList: List<DrugDataModel>): RecyclerView.Adapter<SelectCategoryListAdapter.SelectCategoryViewHodlder>(),
    Filterable {

    fun interface OnItemClickListener {
        fun onItemClick(category: DrugDataModel)
    }

    var onItemClickListener: OnItemClickListener? = null

    private var drugDataModels: List<DrugDataModel> = drugDataModelList
    private val selectedItems = mutableListOf<DrugDataModel>()

    inner class SelectCategoryViewHodlder(private val binding: SelectcategorylistItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                binding.root.setOnClickListener {
                    onItemClickListener?.onItemClick(drugDataModels[bindingAdapterPosition])
                    toggleSelection(drugDataModels[bindingAdapterPosition])
                }
            }
        }

        fun bind(drugDataModel: DrugDataModel) {
            binding.apply {
                selectCategoryName.text = drugDataModel.category
                categoryCheckbox.setOnClickListener {
                    onItemClickListener?.onItemClick(drugDataModels[bindingAdapterPosition])
                    toggleSelection(drugDataModels[bindingAdapterPosition])
                }
                categoryCheckbox.isChecked = selectedItems.contains(drugDataModels[bindingAdapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectCategoryViewHodlder {
        val binding = SelectcategorylistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectCategoryViewHodlder(binding)
    }

    override fun getItemCount(): Int = drugDataModels.size

    override fun onBindViewHolder(holder: SelectCategoryViewHodlder, position: Int) {
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

    private fun toggleSelection(category: DrugDataModel) {
        if (selectedItems.contains(category)) {
            selectedItems.remove(category)
        } else {
            selectedItems.add(category)
        }
        notifyItemRangeChanged(0, itemCount, null)
    }
}