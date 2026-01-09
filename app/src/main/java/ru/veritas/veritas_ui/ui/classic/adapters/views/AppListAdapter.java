package ru.veritas.veritas_ui.ui.classic.adapters.views;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.veritas.veritas_ui.managers.main.app.AppData;
import ru.veritas.veritas_ui.R;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    private Context context;
    private List<AppData> apps;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(String packageName);
    }

    public AppListAdapter(Context context, List<AppData> apps, OnItemClickListener listener) {
        this.context = context;
        this.apps = apps;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppData app = apps.get(position);

        holder.appName.setText(app.getAppName());
        holder.packageName.setText(app.getPackageName());
        holder.appIcon.setImageDrawable(app.getIcon());

        // Делаем иконку серой, если приложение отключено
        if (!app.isEnabled()) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0); // Убираем насыщенность
            holder.appIcon.setColorFilter(new ColorMatrixColorFilter(matrix));
        } else {
            holder.appIcon.clearColorFilter();
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null && app.isEnabled()) {
                onItemClickListener.onItemClick(app.getPackageName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public void updateApps(List<AppData> newApps) {
        apps = newApps;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView appName;
        ImageView appIcon;
        TextView packageName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.app_name_text);
            appIcon = itemView.findViewById(R.id.img);
            packageName = itemView.findViewById(R.id.package_of_it);
        }
    }
}