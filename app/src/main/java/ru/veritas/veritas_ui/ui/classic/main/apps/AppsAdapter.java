package ru.veritas.veritas_ui.ui.classic.main.apps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppInfoEntity;

/**
 * Этот класс связывает данные (список {@link AppInfoEntity}) с {@link RecyclerView}.
 * Он создаёт {@link RecyclerView.ViewHolder}'ы, когда это необходимо, и
 * наполняет их данными, соответствующими позиции в списке.
 * Также он управляет размером списка и уведомляет {@link RecyclerView} об изменениях данных.
 */
public class AppsAdapter extends RecyclerView.Adapter<AppViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(AppInfoEntity app);
        void onItemLongClick(AppInfoEntity app);
    }

    private List<AppInfoEntity> apps = new ArrayList<>();
    private final OnItemClickListener listener;

    public AppsAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfoEntity app = apps.get(position);
        holder.bind(app, listener);
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public void setApps(List<AppInfoEntity> apps) {
        this.apps = apps;
        notifyDataSetChanged();
    }
}