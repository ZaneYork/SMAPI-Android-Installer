package com.zane.smapiinstaller.ui.help;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.databinding.HelpListItemBinding;
import com.zane.smapiinstaller.entity.HelpItem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Zane
 */
public class HelpItemAdapter extends RecyclerView.Adapter<HelpItemAdapter.ViewHolder>  {

    public void setHelpItems(List<HelpItem> helpItems) {
        this.helpItems = helpItems;
        notifyDataSetChanged();
    }

    private List<HelpItem> helpItems;

    public HelpItemAdapter(List<HelpItem> helpItems) {
        this.helpItems = helpItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.help_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setHelpItem(helpItems.get(position));
    }

    @Override
    public int getItemCount() {
        return helpItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final HelpListItemBinding binding;

        public ViewHolder(View view) {
            super(view);
            binding = HelpListItemBinding.bind(view);
        }
        void setHelpItem(HelpItem item) {
            binding.textItemTitle.setText(item.getTitle());
            binding.textItemAuthor.setText(item.getAuthor());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                binding.textItemContent.setText(Html.fromHtml(item.getContent(), Html.FROM_HTML_MODE_COMPACT));
            } else {
                binding.textItemContent.setText(Html.fromHtml(item.getContent()));
            }
            binding.textItemContent.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
