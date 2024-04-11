package com.and.adpater

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.and.databinding.CategorylistItemBinding

class CategoryListAdapter(private val categoryList: List<String>) : RecyclerView.Adapter<CategoryListAdapter.CategoryListViewHodlder>(),
    Filterable {
    interface OnClickListener {
        fun onSettingClick()
    }

    private var categorys: List<String> = categoryList
    var onClickListener: OnClickListener? = null

    inner class CategoryListViewHodlder(private val binding: CategorylistItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                root.setOnClickListener {
                    if (detailRecyclerView.visibility == View.GONE) {
                        detailRecyclerView.visibility = View.VISIBLE
                    } else {
                        detailRecyclerView.visibility = View.GONE
                    }
                }

                setting.setOnClickListener {
                    onClickListener?.onSettingClick()
                }
            }
        }
        fun bind(name: String) {
            binding.apply {
                categoryName.text = name
                val adapter = DetailListAdapter(mutableListOf("aaaaaa", "bcs", "ccc"))
                detailRecyclerView.adapter = adapter
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryListViewHodlder {
        val binding = CategorylistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryListViewHodlder(binding)
    }

    override fun getItemCount(): Int = categorys.size

    override fun onBindViewHolder(holder: CategoryListViewHodlder, position: Int) {
        holder.bind(categorys[position])
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint.toString()
                categorys = if(charString.isEmpty()) {
                    categoryList
                } else {
                    val filteredList = mutableListOf<String>()
                    for(category in categoryList) {
                        if(category.lowercase().contains(charString.lowercase())) {
                            filteredList.add(category)
                        }
                    }
                    filteredList
                }
                val filterResult = FilterResults()
                filterResult.values = categorys
                return filterResult
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                categorys = results?.values as List<String>
                notifyDataSetChanged()
            }
        }
    }
}