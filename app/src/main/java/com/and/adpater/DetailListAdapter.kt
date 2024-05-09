package com.and.adpater

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.and.databinding.DetaillistItemBinding

class DetailListAdapter(private val detailList: List<String>): RecyclerView.Adapter<DetailListAdapter.DetailListViewHolder>() {
    inner class DetailListViewHolder(private val binding: DetaillistItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(name: String) {
            binding.apply {
                detailName.text = name
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailListViewHolder {
        val binding = DetaillistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailListViewHolder(binding)
    }

    override fun getItemCount(): Int = detailList.size

    override fun onBindViewHolder(holder: DetailListViewHolder, position: Int) {
        holder.bind(detailList[position])
    }
}