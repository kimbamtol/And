package com.and.adpater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.and.databinding.RemovedetaillistItemBinding

class RemoveDetailListAdapter(private val detailList: List<String>): RecyclerView.Adapter<RemoveDetailListAdapter.RemoveDetailListViewHolder>() {

    private val selectedItems = mutableListOf<String>()

    fun interface OnItemClickListener {
        fun onItemClick(detail: String)
    }

    var onItemClickListener: OnItemClickListener? = null
    inner class RemoveDetailListViewHolder(private val binding: RemovedetaillistItemBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClickListener?.onItemClick(detailList[bindingAdapterPosition])
                toggleSelection(detailList[bindingAdapterPosition])
            }
        }
        fun bind(name: String) {
            binding.apply {
                detailCheckbox.background = null
                detailName.text = name
                detailCheckbox.setOnClickListener {
                    onItemClickListener?.onItemClick(detailList[bindingAdapterPosition])
                    toggleSelection(detailList[bindingAdapterPosition])
                }
                detailCheckbox.isChecked = selectedItems.contains(detailList[bindingAdapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemoveDetailListViewHolder {
        val binding = RemovedetaillistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RemoveDetailListViewHolder(binding)
    }

    override fun getItemCount(): Int = detailList.size

    override fun onBindViewHolder(holder: RemoveDetailListViewHolder, position: Int) {
        holder.bind(detailList[position])
    }

    private fun toggleSelection(detail: String) {
        if (selectedItems.contains(detail)) {
            selectedItems.remove(detail)
        } else {
            selectedItems.add(detail)
        }
        notifyItemRangeChanged(0, itemCount, null)
    }
}