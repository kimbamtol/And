package com.and.adpater

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.and.R
import com.and.databinding.CategorylistItemBinding
import com.and.datamodel.DrugDataModel

class CategoryListAdapter: ListAdapter<DrugDataModel, CategoryListAdapter.CategoryListViewHolder>(diffUtil), Filterable {
    private var originList = listOf<DrugDataModel>()
    interface OnClickListener {
        fun onSettingClick(drugDataModel: DrugDataModel)
        fun onAlarmClick(drugDataModel: DrugDataModel)
    }

    var onClickListener: OnClickListener? = null

    inner class CategoryListViewHolder(private val binding: CategorylistItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(drugDataModel: DrugDataModel) {
            binding.apply {
                binding.category = drugDataModel

                val adapter = DetailListAdapter(drugDataModel.details)
                detailRecyclerView.adapter = adapter

                setting.setOnClickListener {
                    onClickListener?.onSettingClick(drugDataModel)
                }

                alarm.setOnClickListener {
                    onClickListener?.onAlarmClick(drugDataModel)
                }

                openRecyclerView.setOnClickListener {
                    if (detailRecyclerView.visibility == View.GONE) {
                        detailRecyclerView.visibility = View.VISIBLE
                        arrow.setImageResource(R.drawable.arrowdown)
                    } else {
                        detailRecyclerView.visibility = View.GONE
                        arrow.setImageResource(R.drawable.arrow)
                    }
                }
            }
        }
    }

    private val searchFilter : Filter = object : Filter() {
        override fun performFiltering(input: CharSequence?): FilterResults {
            val word = (input ?: "").toString().lowercase()
            val filteredList = if (word.isEmpty()) {
                originList
            } else {
                val newList = mutableListOf<DrugDataModel>()
                originList.forEach { drugDataModel ->
                    if (drugDataModel.category.lowercase().contains(word)) {
                        newList.add(drugDataModel)
                        return@forEach
                    }
                    drugDataModel.details.forEach { detail ->
                        if (detail.lowercase().contains(word))
                            newList.add(drugDataModel)
                    }
                }
                newList.distinct()
            }
            return FilterResults().apply { values = filteredList.toMutableList() }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null) {
                submitList((results.values as MutableList<DrugDataModel>).sortedBy { it.creationTime})
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryListViewHolder {
        val binding =
            CategorylistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryListViewHolder(binding)
    }

    override fun getItemCount(): Int = currentList.size

    override fun onBindViewHolder(holder: CategoryListViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getFilter(): Filter {
        return searchFilter
    }

    fun setData(list: MutableList<DrugDataModel>?){
        this.originList = list ?: listOf()
        submitList(list)
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<DrugDataModel>() {
            override fun areItemsTheSame(oldItem: DrugDataModel, newItem: DrugDataModel): Boolean {
                return oldItem.creationTime == newItem.creationTime
            }

            override fun areContentsTheSame(
                oldItem: DrugDataModel,
                newItem: DrugDataModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}