package com.orgzly.android.ui.note.links

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.orgzly.databinding.ItemLinkTargetBinding

class LinkTargetAdapter(
    private val onTargetClick: (LinkTarget) -> Unit
) : ListAdapter<LinkTarget, LinkTargetAdapter.ViewHolder>(DIFF_CALLBACK) {

    class ViewHolder(val binding: ItemLinkTargetBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLinkTargetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val holder = ViewHolder(binding)
        binding.root.setOnClickListener {
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onTargetClick(getItem(position))
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).let { target ->
            holder.binding.itemLinkTargetTitle.text = target.title
            holder.binding.itemLinkTargetContext.text = target.context
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LinkTarget>() {
            override fun areItemsTheSame(oldItem: LinkTarget, newItem: LinkTarget): Boolean =
                oldItem.noteId == newItem.noteId

            override fun areContentsTheSame(oldItem: LinkTarget, newItem: LinkTarget): Boolean =
                oldItem == newItem
        }
    }
}
