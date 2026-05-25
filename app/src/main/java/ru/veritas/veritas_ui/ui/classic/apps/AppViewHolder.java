package ru.veritas.veritas_ui.ui.classic.apps;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.command.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.ui.common.utils.IconUtils;

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

    public void bind(final AppShortcut app, final AppsAdapter.OnItemClickListener listener, GetAppIconUseCase getAppIconUseCase) {
        if (app == null) return;
        icon.setImageDrawable(IconUtils.toDrawable(
                getAppIconUseCase.invoke(app),
                icon.getResources())
        );
        name.setText(app.getAppName());
        cardView.setOnClickListener(v -> listener.onItemClick(app));
    }
}