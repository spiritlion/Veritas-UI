// [file name]: HomeDesktopFragment.java (обновленная версия)
package ru.veritas.veritas_ui.ui.classic.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Objects;

import ru.veritas.veritas_ui.LauncherActivity;
import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.managers.main.app.AppData;
import ru.veritas.veritas_ui.managers.main.desktop.DesktopItem;
import ru.veritas.veritas_ui.managers.ui.desktop.DesktopManager;
import ru.veritas.veritas_ui.managers.ui.GestureRecordingManager;
import ru.veritas.veritas_ui.ui.ViewType;
import ru.veritas.veritas_ui.ui.classic.dialogs.AddToDesktopDialog;

public class HomeDesktopFragment extends Fragment {

    private DesktopManager desktopManager;
    private GridLayout desktopGrid;
    private static final int GRID_COLUMNS = 4;
    private static final int GRID_ROWS = 6;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_desktop, container, false);

        desktopManager = new DesktopManager(getContext());
        desktopGrid = view.findViewById(R.id.desktopGrid);

        setupDesktopGrid();
        loadDesktopItems();

        // Кнопка добавления приложений
        Button btnAddApps = view.findViewById(R.id.btnAddApps);
        btnAddApps.setOnClickListener(v -> {
            AddToDesktopDialog.show(getContext(), packageName -> {
                // Добавляем приложение на рабочий стол
                if (getActivity() instanceof LauncherActivity) {
                    ((LauncherActivity) getActivity()).addAppToDesktop(packageName);
                    refreshDesktop();
                }
            });
        });

        // Кнопка очистки рабочего стола
        Button btnClearDesktop = view.findViewById(R.id.btnClearDesktop);
        btnClearDesktop.setOnClickListener(v -> {
            // TODO: Реализовать очистку рабочего стола
        });

        // Настраиваем жесты для всего фрагмента
        GestureRecordingManager.setupVerticalSwipe(view, new GestureRecordingManager.OnSwipeListener() {
            @Override
            public void onSwipeDown() {
                if (getActivity() instanceof LauncherActivity) {
                    ((LauncherActivity) getActivity()).switchToPage(ViewType.AppList);
                }
            }

            @Override
            public void onSwipeUp() {
                // Свайп вверх на главном экране - ничего не делаем
            }
        });

        return view;
    }

    private void setupDesktopGrid() {
        // Настраиваем сетку
        desktopGrid.setColumnCount(GRID_COLUMNS);
        desktopGrid.setRowCount(GRID_ROWS);

        // Рассчитываем размер ячеек
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cellSize = screenWidth / GRID_COLUMNS;

        // Очищаем сетку
        desktopGrid.removeAllViews();

        // Создаем пустые ячейки
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLUMNS; col++) {
                View cell = new View(getContext());
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                cell.setLayoutParams(params);
                desktopGrid.addView(cell);
            }
        }
    }

    private void loadDesktopItems() {
        // Получаем элементы рабочего стола
        List<DesktopItem> desktopItems = desktopManager.getDesktopItems();
        List<AppData> desktopApps = desktopManager.getDesktopApps(desktopItems);

        // Рассчитываем размер ячеек
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cellSize = screenWidth / GRID_COLUMNS;

        // Размещаем приложения на рабочем столе
        for (int i = 0; i < desktopItems.size(); i++) {
            DesktopItem item = desktopItems.get(i);

            if (i < desktopApps.size()) {
                AppData app = desktopApps.get(i);
                View appView = createAppView(app, cellSize);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize * item.getWidth();
                params.height = cellSize * item.getHeight();
                params.rowSpec = GridLayout.spec(item.getPositionY(), item.getHeight());
                params.columnSpec = GridLayout.spec(item.getPositionX(), item.getWidth());
                appView.setLayoutParams(params);

                desktopGrid.addView(appView);
            }
        }
    }

    private View createAppView(AppData app, int cellSize) {
        View appView = LayoutInflater.from(getContext())
                .inflate(R.layout.desktop_app_item, desktopGrid, false);

        ImageView icon = appView.findViewById(R.id.desktopAppIcon);
        TextView name = appView.findViewById(R.id.desktopAppName);

        icon.setImageDrawable(app.getIcon());
        name.setText(app.getAppName());

        // Устанавливаем размеры
        appView.setMinimumWidth(cellSize);
        appView.setMinimumHeight(cellSize);

        // Обработчик клика для запуска приложения
        appView.setOnClickListener(v -> {
            if (getActivity() instanceof LauncherActivity) {
                ((LauncherActivity) getActivity()).onAppClick(app.getPackageName());
            }
        });

        return appView;
    }

    public void refreshDesktop() {
        setupDesktopGrid();
        loadDesktopItems();
    }
}