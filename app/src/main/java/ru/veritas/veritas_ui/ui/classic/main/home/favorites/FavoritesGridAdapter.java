package ru.veritas.veritas_ui.ui.classic.main.home.favorites;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetImageUseCase;

public class FavoritesGridAdapter extends RecyclerView.Adapter<FavoritesGridAdapter.ViewHolder> {
    private List<AppShortcutDTO> items = new ArrayList<>();
    private final GetImageUseCase getImageUseCase;
    private final int pageIndex;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AppShortcutDTO shortcut);
        void onItemLongClick(AppShortcutDTO shortcut, int pageIndex, int position, View v);
    }

    public FavoritesGridAdapter(Context context, int pageIndex) {
        this.getImageUseCase = GetImageUseCase.create(context.getPackageManager());
        this.pageIndex = pageIndex;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<AppShortcutDTO> newData) {
        this.items = newData != null ? newData : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_cell, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppShortcutDTO shortcut = items.get(position);
        if (shortcut == null) {
            holder.itemView.setVisibility(View.INVISIBLE);
            return;
        }
        holder.itemView.setVisibility(View.VISIBLE);
        holder.appName.setText(shortcut.getAppName());
        holder.appIcon.setImageDrawable(getImageUseCase.invoke(shortcut));



        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(shortcut);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(shortcut, pageIndex, position, holder.itemView);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.fav_icon);
            appName = itemView.findViewById(R.id.fav_name);
        }
    }
}