package com.assaf.android.minibrowser.adapters;


import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.assaf.android.minibrowser.Constants;
import com.assaf.android.minibrowser.History.DatabaseManager;
import com.assaf.android.minibrowser.History.Site;
import com.assaf.android.minibrowser.R;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {
    private List<Site> myDataLists;
    private Context context;

    public HistoryRecyclerViewAdapter(List<Site> myDataLists, Context context) {
        this.myDataLists = myDataLists;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.history_list_data, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Site siteItem = myDataLists.get(i);
        viewHolder.tvTitle.setText(siteItem.siteName);
        viewHolder.tvSubtitle.setText(siteItem.siteUrl);
        if (siteItem.siteIcon != null && siteItem.siteIcon.length > 0)
            viewHolder.ivIcon.setImageBitmap(BitmapFactory.decodeByteArray(siteItem.siteIcon, 0, siteItem.siteIcon.length));
        else
            viewHolder.ivIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.internet));
        viewHolder.ivDeleteSite.setOnClickListener(v -> {
            try {
                DatabaseManager.getInstance(context).getDb().siteDao().delete(myDataLists.get(i));
                myDataLists = DatabaseManager.getInstance(context).getDb().siteDao().loadFirstN(Constants.NO_OF_SITES);
                notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return myDataLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final ImageView ivIcon;
        private final ImageView ivDeleteSite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.title);
            tvSubtitle = itemView.findViewById(R.id.subtitle);
            ivIcon = itemView.findViewById(R.id.icon);
            ivDeleteSite = itemView.findViewById(R.id.delete);
        }
    }
}

