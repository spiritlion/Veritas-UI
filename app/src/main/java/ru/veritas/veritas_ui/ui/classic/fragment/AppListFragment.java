package ru.veritas.veritas_ui.ui.classic.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

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

public class AppListFragment extends Fragment {

    private VerticalScrollRecyclerView appRecyclerView;
    private AppListAdapter appListAdapter;
    private List<AppData> arrayOfApp = new ArrayList<>();
    private AppsManager appsManager;
    private OnAppClickListener onAppClickListener;
    private ProgressBar progressBar;
    private Button btnBack;

    // Флаг для отслеживания загрузки
    private boolean isLoading = false;

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
        View view = inflater.inflate(R.layout.app_list, container, false);

        appRecyclerView = view.findViewById(R.id.appRecyclerView);
        btnBack = view.findViewById(R.id.btnBack);
        progressBar = view.findViewById(R.id.progressBar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Сразу показываем ProgressBar
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Устанавливаем GridLayoutManager с 4 столбцами
        int spanCount = 4;
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        appRecyclerView.setLayoutManager(layoutManager);

        // Создаем пустой адаптер
        appListAdapter = new AppListAdapter(getContext(), new ArrayList<>(),
                packageName -> {
                    if (onAppClickListener != null) {
                        onAppClickListener.onAppClick(packageName);
                    }
                });

        appRecyclerView.setAdapter(appListAdapter);

        // Загружаем приложения
        loadAppsAsync();

        // Кнопка назад
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof LauncherActivity) {
                ((LauncherActivity) getActivity()).switchToPage(ViewType.Main);
            }
        });
    }

    private void loadAppsAsync() {
        if (isLoading) return;

        isLoading = true;
        appsManager = new AppsManager(getContext());

        appsManager.loadUserAppsAsync(new AppsManager.AppLoadCallback() {
            @Override
            public void onAppsLoaded(List<AppData> apps) {
                arrayOfApp = apps;
                isLoading = false;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        if (appListAdapter != null) {
                            appListAdapter.updateApps(arrayOfApp);
                        }
                    });
                }
            }
        });
    }

    public void refreshAppList() {
        if (appsManager != null) {
            appsManager.invalidateCache();
        }
        loadAppsAsync();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Опционально: обновлять список при возвращении
        // refreshAppList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isLoading = false;
    }
}