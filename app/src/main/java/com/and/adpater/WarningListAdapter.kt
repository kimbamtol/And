package com.and.adpater

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.and.databinding.WarninglistItemBinding
import com.and.datamodel.DrugDataModel

class WarningListAdapter : ListAdapter<String, WarningListAdapter.WarningListViewHodlder>(diffUtil) {

    fun interface OnItemLongClickListener {
        fun onItemLongClick(warningContext: String)
    }

    var onItemClickListener: OnItemLongClickListener? = null

    inner class WarningListViewHodlder(private val binding: WarninglistItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(warningContext: String) {
            binding.apply {
                warning = warningContext

                warningText.setOnLongClickListener {
                    onItemClickListener?.onItemLongClick(warningContext)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarningListViewHodlder {
        val binding = WarninglistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WarningListViewHodlder(binding)
    }

    override fun getItemCount(): Int = currentList.size

    override fun onBindViewHolder(holder: WarningListViewHodlder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }
}