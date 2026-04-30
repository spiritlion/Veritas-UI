package ru.veritas.veritas_ui.ui.classic.main.home;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetImageUseCase;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private List<AppShortcutDTO> appsList;
    private ViewPagerPagesAdapter.OnItemClickListener listener;
    private final GetImageUseCase getImageUseCase;
    private final int pageIndex;
    private final int columnCount;
    private int itemHeightPx = ViewGroup.LayoutParams.WRAP_CONTENT;
    public AppAdapter(List<AppShortcutDTO> appsList, Context context,
                      ViewPagerPagesAdapter.OnItemClickListener listener,
                      int pageIndex, int columnCount) {
        this.appsList = appsList;
        this.listener = listener;
        this.pageIndex = pageIndex;
        this.columnCount = columnCount;
        this.getImageUseCase = GetImageUseCase.create(context.getPackageManager());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        // Задаём высоту из расчёта
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
        if (lp == null) {
            lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    itemHeightPx
            );
        } else {
            lp.height = itemHeightPx;
        }
        view.setLayoutParams(lp);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppShortcutDTO app = appsList.get(position);
        if (app == null) {
            holder.inside_app.setVisibility(INVISIBLE);
            return;
        }
        holder.inside_app.setVisibility(VISIBLE);
        holder.app.setOnClickListener(v -> listener.onItemClick(app));
//        Log.d("AppAdapter", position + " " + app.getAppName() + " " + row + " " + col);
        holder.app.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return false;
            int row = pos / columnCount;
            int col = pos % columnCount;
            listener.onItemLongClick(pageIndex, row, col, v);
            return true;
        });
        holder.appName.setText(app.getAppName());
        holder.appIcon.setImageDrawable(getImageUseCase.invoke(app));
    }

    @Override
    public int getItemCount() {
        return appsList == null ? -1 : appsList.size();
    }

    public void updateData(List<AppShortcutDTO> newList) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new AppDiffCallback(this.appsList, newList));
        this.appsList = newList;
        result.dispatchUpdatesTo(this);
    }

    public void setListener(ViewPagerPagesAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItemHeightPx(int heightPx) {
        if (heightPx != itemHeightPx && heightPx > 0) {
            itemHeightPx = heightPx;
            notifyDataSetChanged(); // обновить все элементы с новой высотой
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        LinearLayout app;
        LinearLayout inside_app;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            app = itemView.findViewById(R.id.app);
            appIcon = itemView.findViewById(R.id.app_icon);
            appName = itemView.findViewById(R.id.app_name);
            inside_app = itemView.findViewById(R.id.inside_app);
        }
    }

    private static class AppDiffCallback extends DiffUtil.Callback {
        private final List<AppShortcutDTO> oldList;
        private final List<AppShortcutDTO> newList;

        AppDiffCallback(List<AppShortcutDTO> oldList, List<AppShortcutDTO> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList == null ? 0 : oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList == null ? 0 : newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            AppShortcutDTO oldItem = oldList.get(oldItemPosition);
            AppShortcutDTO newItem = newList.get(newItemPosition);
            if (oldItem == null && newItem == null) return true;
            if (oldItem == null || newItem == null) return false;
            // Use package name as the unique identifier
            return oldItem.getPackageName().equals(newItem.getPackageName());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            AppShortcutDTO oldItem = oldList.get(oldItemPosition);
            AppShortcutDTO newItem = newList.get(newItemPosition);
            if (oldItem == null && newItem == null) return true;
            if (oldItem == null || newItem == null) return false;
            return oldItem.getAppName().equals(newItem.getAppName());
        }
    }
}