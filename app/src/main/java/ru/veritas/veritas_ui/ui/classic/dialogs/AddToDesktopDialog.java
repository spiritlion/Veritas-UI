// [file name]: AddToDesktopDialog.java
package ru.veritas.veritas_ui.ui.classic.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.managers.main.app.AppData;
import ru.veritas.veritas_ui.managers.main.app.AppsManager;
import ru.veritas.veritas_ui.ui.classic.adapters.views.AppListAdapter;

public class AddToDesktopDialog {
    public interface OnAppSelectedListener {
        void onAppSelected(String packageName);
    }

    public static void show(Context context, OnAppSelectedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Добавить на рабочий стол");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_to_desktop, null);
        builder.setView(dialogView);

        RecyclerView appsRecyclerView = dialogView.findViewById(R.id.appsRecyclerView);
        AppsManager appsManager = new AppsManager(context);

        // Настраиваем RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
        appsRecyclerView.setLayoutManager(layoutManager);

        // Загружаем приложения
        List<AppData> apps = appsManager.loadUserAppsSync();

        AppListAdapter adapter = new AppListAdapter(context, apps,
                packageName -> {
                    if (listener != null) {
                        listener.onAppSelected(packageName);
                    }
                });

        appsRecyclerView.setAdapter(adapter);

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }
}