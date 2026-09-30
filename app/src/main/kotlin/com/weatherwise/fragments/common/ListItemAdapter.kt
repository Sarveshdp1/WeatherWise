package com.weatherwise.fragments.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.weatherwise.R
import com.weatherwise.databinding.ItemContainerListBinding

// One row in a list. Alerts, contacts, guides and helplines are all shown with this same row.
data class ListItem(
    val id: Int,                 // lets the screen know which row was tapped
    val title: String,
    val subtitle: String,
    @ColorRes val accentColorRes: Int = R.color.sonic_silver   // color of the strip on the left
)

class ListItemAdapter(
    private val onItemClicked: (ListItem) -> Unit
) : RecyclerView.Adapter<ListItemAdapter.ListItemViewHolder>() {

    private val items = mutableListOf<ListItem>()

    fun setData(data: List<ListItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        return ListItemViewHolder(
            ItemContainerListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ListItemViewHolder(
        private val binding: ItemContainerListBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListItem) {
            with(binding) {
                textItemTitle.text = item.title
                textItemSubtitle.text = item.subtitle
                textItemSubtitle.visibility =
                    if (item.subtitle.isBlank()) View.GONE else View.VISIBLE
                viewAccent.setBackgroundColor(
                    ContextCompat.getColor(root.context, item.accentColorRes)
                )
                root.setOnClickListener { onItemClicked(item) }
            }
        }
    }
}
