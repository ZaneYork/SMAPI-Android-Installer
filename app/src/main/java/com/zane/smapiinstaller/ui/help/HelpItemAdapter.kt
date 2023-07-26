package com.zane.smapiinstaller.ui.help

import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.databinding.HelpListItemBinding
import com.zane.smapiinstaller.entity.HelpItem

/**
 * @author Zane
 */
class HelpItemAdapter(private var helpItems: List<HelpItem>) :
    RecyclerView.Adapter<HelpItemAdapter.ViewHolder>() {
    fun setHelpItems(helpItems: List<HelpItem>) {
        this.helpItems = helpItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.help_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setHelpItem(helpItems[position])
    }

    override fun getItemCount(): Int {
        return helpItems.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding: HelpListItemBinding

        init {
            binding = HelpListItemBinding.bind(view)
        }

        fun setHelpItem(item: HelpItem) {
            binding.textItemTitle.text = item.title
            binding.textItemAuthor.text = item.author
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.textItemContent.text =
                    Html.fromHtml(item.content, Html.FROM_HTML_MODE_COMPACT)
            } else {
                binding.textItemContent.text = Html.fromHtml(item.content)
            }
            binding.textItemContent.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}