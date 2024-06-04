package com.and.adpater

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.and.databinding.TimelinelistItemBinding
import com.and.datamodel.DrugDataModel
import com.and.datamodel.TimeLineDataModel

class TimeLineListAdapter : ListAdapter<TimeLineDataModel, TimeLineListAdapter.TimeLineListViewHolder>(diffUtil) {

    fun interface SetOnLongClickListener {
        fun setOnLongClick(timeLineDataModel: TimeLineDataModel)
    }

    var setOnLongClickListener: SetOnLongClickListener? = null
    inner class TimeLineListViewHolder(private val binding: TimelinelistItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(timeLineDataModel: TimeLineDataModel) {
            binding.timeLine = timeLineDataModel
            binding.root.setOnLongClickListener {
                setOnLongClickListener?.setOnLongClick(timeLineDataModel)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineListViewHolder {
        val binding = TimelinelistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimeLineListViewHolder(binding)
    }

    override fun getItemCount(): Int = currentList.size

    override fun onBindViewHolder(holder: TimeLineListViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<TimeLineDataModel>() {
            override fun areItemsTheSame(oldItem: TimeLineDataModel, newItem: TimeLineDataModel): Boolean {
                return oldItem.creationTime == newItem.creationTime
            }

            override fun areContentsTheSame(
                oldItem: TimeLineDataModel,
                newItem: TimeLineDataModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}