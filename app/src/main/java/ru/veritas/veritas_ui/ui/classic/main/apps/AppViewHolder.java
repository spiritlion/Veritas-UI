package ru.veritas.veritas_ui.ui.classic.main.apps;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppInfoEntity;

/**
 * Этот класс хранит ссылки на все View внутри одного элемента списка
 */
public class AppViewHolder extends RecyclerView.ViewHolder {

    private final MaterialCardView cardView;
    private final ImageView icon;
    private final TextView name;

    public AppViewHolder(@NonNull View itemView) {
        super(itemView);
        cardView = (MaterialCardView) itemView;
        icon = itemView.findViewById(R.id.app_icon);
        name = itemView.findViewById(R.id.app_name);
    }

    public void bind(final AppInfoEntity app, final AppsAdapter.OnItemClickListener listener) {
        icon.setImageDrawable(app.getIcon());
        name.setText(app.getAppName());
        cardView.setOnClickListener(v -> listener.onItemClick(app));
        cardView.setOnLongClickListener(v -> {
            listener.onItemLongClick(app);
            return true;
        });
    }
}