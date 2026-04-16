package ru.veritas.veritas_ui.ui.classic.main.home;

import static android.view.View.INVISIBLE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetImageUseCase;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private List<AppShortcutDTO> appsList;
    private ViewPagerPagesAdapter.OnItemClickListener listener;
    private GetImageUseCase getImageUseCase;
    private int pageIndex;
    private int columnCount;

    public AppAdapter(List<AppShortcutDTO> appsList, Context context,
                      ViewPagerPagesAdapter.OnItemClickListener listener,
                      int pageIndex, int columnCount) {
        this.appsList = appsList;
        this.listener = listener;
        this.pageIndex = pageIndex;
        this.columnCount = columnCount;
        this.getImageUseCase = new GetImageUseCase(context.getPackageManager());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppShortcutDTO app = appsList.get(position);
        if (app == null) {
            holder.setVisibility(INVISIBLE);
            return;
        }
        holder.app.setOnClickListener(v -> listener.onItemClick(app));
        holder.app.setOnLongClickListener(v -> {
            int row = position / columnCount;
            int col = position % columnCount;
            listener.onItemLongClick(pageIndex, row, col);
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
        this.appsList = newList;
        notifyDataSetChanged();
    }

    public void setListener(ViewPagerPagesAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        MaterialCardView app;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            app = itemView.findViewById(R.id.app);
            appIcon = itemView.findViewById(R.id.app_icon);
            appName = itemView.findViewById(R.id.app_name);
        }

        void setVisibility(int visibility) {
            app.setVisibility(visibility);
        }
    }
}