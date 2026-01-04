package ru.veritas.veritas_ui.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.managers.main.app.AppData;
import ru.veritas.veritas_ui.managers.main.app.AppsManager;
import ru.veritas.veritas_ui.ui.view.AppListView;

public class AppListFragment extends Fragment {

    private RecyclerView appRecyclerView;
    private AppListView appListView;
    private ArrayList<AppData> arrayOfApp = new ArrayList<>();
    private AppsManager appsManager;
    private OnAppClickListener onAppClickListener;

    public interface OnAppClickListener {
        void onAppClick(String packageName);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnAppClickListener) {
            onAppClickListener = (OnAppClickListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.app_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appRecyclerView = view.findViewById(R.id.appRecyclerView);

        // Устанавливаем GridLayoutManager с 4 столбцами
        int spanCount = 4;
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        appRecyclerView.setLayoutManager(layoutManager);

        // Загружаем приложения через AppsManager
        appsManager = new AppsManager(getContext());
        loadApps();

        // Создаем адаптер
        appListView = new AppListView(getContext(), arrayOfApp,
                packageName -> {
                    if (onAppClickListener != null) {
                        onAppClickListener.onAppClick(packageName);
                    }
                });

        appRecyclerView.setAdapter(appListView);
    }

    private void loadApps() {
        arrayOfApp = appsManager.loadUserApps();
        arrayOfApp.sort((a, b) ->
                a.getAppName().compareToIgnoreCase(b.getAppName()));
    }

    public void refreshAppList() {
        loadApps();
        if (appListView != null) {
            appListView.updateApps(arrayOfApp);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAppList();
    }
}