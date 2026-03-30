package ru.veritas.veritas_ui.ui.classic.main.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(AppShortcut shortcut);
        void onItemLongClick(AppShortcut shortcut);
    }

    private List<AppShortcut> shortcuts = new ArrayList<>();
    private final OnItemClickListener listener;

    public HomeAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new HomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        AppShortcut shortcut = shortcuts.get(position);
        holder.bind(shortcut, listener);
    }

    @Override
    public int getItemCount() {
        return shortcuts.size();
    }

    public void setShortcuts(List<AppShortcut> shortcuts) {
        this.shortcuts = shortcuts;
        notifyDataSetChanged();
    }

    static class HomeViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView icon;
        private final TextView name;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            icon = itemView.findViewById(R.id.app_icon);
            name = itemView.findViewById(R.id.app_name);
        }

        public void bind(final AppShortcut shortcut, final OnItemClickListener listener) {
            icon.setImageDrawable(shortcut.getIcon());
            name.setText(shortcut.getAppName());
            cardView.setOnClickListener(v -> listener.onItemClick(shortcut));
            cardView.setOnLongClickListener(v -> {
                listener.onItemLongClick(shortcut);
                return true;
            });
        }
    }
}