package ru.veritas.veritas_ui.ui.custom.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.LauncherActivity;
import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.managers.main.app.AppData;
import ru.veritas.veritas_ui.managers.main.app.AppsManager;
import ru.veritas.veritas_ui.ui.ViewType;
import ru.veritas.veritas_ui.ui.classic.adapters.views.AppListAdapter;
import ru.veritas.veritas_ui.ui.classic.adapters.VerticalScrollRecyclerView;

public class CustomAppListFragment extends Fragment { // TODO переделать

    private VerticalScrollRecyclerView appRecyclerView;
    private AppListAdapter appListAdapter;
    private List<AppData> cachedApps = new ArrayList<>();
    private AppsManager appsManager;
    private OnAppClickListener onAppClickListener;
    private Button btnBack;

    private static final String ARG_APPS = "cached_apps";

    public interface OnAppClickListener {
        void onAppClick(String packageName);
    }

    public static CustomAppListFragment newInstance(List<AppData> apps) {
        CustomAppListFragment fragment = new CustomAppListFragment();
        Bundle args = new Bundle();
        // Можно передать кэшированные данные через аргументы
        return fragment;
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
        return inflater.inflate(R.layout.app_list_custom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appRecyclerView = view.findViewById(R.id.appRecyclerView);
        btnBack = view.findViewById(R.id.btnBack);

        // Быстрая инициализация RecyclerView с пустым адаптером
        int spanCount = 4;
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        appRecyclerView.setLayoutManager(layoutManager);

        // Создаем пустой адаптер сразу
        appListAdapter = new AppListAdapter(getContext(), cachedApps,
                packageName -> {
                    if (onAppClickListener != null) {
                        onAppClickListener.onAppClick(packageName);
                    }
                });

        appRecyclerView.setAdapter(appListAdapter);

        // Кнопка назад
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof LauncherActivity) {
                ((LauncherActivity) getActivity()).switchToPage(ViewType.Main);
            }
        });

        // Загружаем приложения (если есть кэш, показываем сразу)
        loadAppsWithCache();
    }

    private void loadAppsWithCache() {
        if (appsManager == null) {
            appsManager = new AppsManager(getContext());
        }

        // Пытаемся получить из кэша
        appsManager.loadUserAppsAsync(new AppsManager.AppLoadCallback() {
            @Override
            public void onAppsLoaded(List<AppData> apps) {
                cachedApps = apps;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (appListAdapter != null) {
                            appListAdapter.updateApps(cachedApps);
                        }
                    });
                }
            }
        });
    }

    public void refreshAppList() {
        if (appsManager != null) {
            appsManager.invalidateCache();
            loadAppsWithCache();
        }
    }
}