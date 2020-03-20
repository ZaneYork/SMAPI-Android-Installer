package com.zane.smapiinstaller.ui.help;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.entity.HelpItem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

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
        @BindView(R.id.text_item_title)
        TextView textTitle;
        @BindView(R.id.text_item_author)
        TextView textAuthor;
        @BindView(R.id.text_item_content)
        TextView textContent;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
        void setHelpItem(HelpItem item) {
            textTitle.setText(item.getTitle());
            textAuthor.setText(item.getAuthor());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                textContent.setText(Html.fromHtml(item.getContent(), Html.FROM_HTML_MODE_COMPACT));
            } else {
                textContent.setText(Html.fromHtml(item.getContent()));
            }
            textContent.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
