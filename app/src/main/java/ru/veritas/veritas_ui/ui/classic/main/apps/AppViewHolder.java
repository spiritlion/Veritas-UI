package ru.veritas.veritas_ui.ui.classic.main.apps;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetImageUseCase;

/**
 * Этот класс хранит ссылки на все View внутри одного элемента списка
 */
public class AppViewHolder extends RecyclerView.ViewHolder {

    private final LinearLayout cardView;
    private final ImageView icon;
    private final TextView name;

    public AppViewHolder(@NonNull View itemView) {
        super(itemView);
        cardView = (LinearLayout) itemView;
        icon = itemView.findViewById(R.id.app_icon);
        name = itemView.findViewById(R.id.app_name);
    }

    public void bind(final AppShortcutDTO app, final AppsAdapter.OnItemClickListener listener, GetImageUseCase getImageUseCase) {
        if (app == null) return;
        icon.setImageDrawable(getImageUseCase.invoke(app));
        name.setText(app.getAppName());
        cardView.setOnClickListener(v -> listener.onItemClick(app));
    }
}