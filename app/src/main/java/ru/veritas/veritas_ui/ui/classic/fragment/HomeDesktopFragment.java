package ru.veritas.veritas_ui.ui.classic.fragment;

import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.LauncherActivity;
import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.managers.main.app.AppData;
import ru.veritas.veritas_ui.managers.main.desktop.DesktopItem;
import ru.veritas.veritas_ui.managers.ui.desktop.DesktopManager;
import ru.veritas.veritas_ui.managers.ui.GestureRecordingManager;
import ru.veritas.veritas_ui.ui.ViewType;

public class HomeDesktopFragment extends Fragment {

    private static final String TAG = "DesktopFragment";
    private static final int GRID_COLUMNS = 4;
    private static final int GRID_ROWS = 6;

    private DesktopManager desktopManager;
    private GridLayout desktopGrid;
    private List<DesktopItem> desktopItems = new ArrayList<>();
    private List<AppData> desktopApps = new ArrayList<>();

    // Для Drag & Drop
    private View draggedView;
    private int dragStartX = -1;
    private int dragStartY = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_desktop, container, false);

        desktopManager = new DesktopManager(getContext());
        desktopGrid = view.findViewById(R.id.desktopGrid);

        // Сначала настраиваем Grid
        setupDesktopGrid();

        // Загружаем элементы рабочего стола
        loadDesktopItems();

        // Настраиваем Drag & Drop
        setupDragAndDrop();

        // Кнопка добавления приложений
        Button btnAddApps = view.findViewById(R.id.btnAddApps);
        btnAddApps.setOnClickListener(v -> {
            showAddAppsDialog();
        });

        // Кнопка очистки рабочего стола
        Button btnClearDesktop = view.findViewById(R.id.btnClearDesktop);
        btnClearDesktop.setOnClickListener(v -> {
            clearDesktop();
        });

        // Настраиваем жесты
        setupGestures(view);

        return view;
    }

    private void setupDesktopGrid() {
        desktopGrid.setColumnCount(GRID_COLUMNS);
        desktopGrid.setRowCount(GRID_ROWS);

        // Рассчитываем размер ячейки
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cellSize = screenWidth / GRID_COLUMNS;

        // Очищаем сетку
        desktopGrid.removeAllViews();

        Log.d(TAG, "Grid setup: " + GRID_COLUMNS + "x" + GRID_ROWS + ", cellSize: " + cellSize);
    }

    private void loadDesktopItems() {
        // Получаем элементы из менеджера
        desktopItems = desktopManager.getDesktopItems();
        desktopApps = desktopManager.getDesktopApps(desktopItems);

        Log.d(TAG, "Loaded " + desktopItems.size() + " items, " + desktopApps.size() + " apps");

        // Очищаем Grid перед добавлением
        desktopGrid.removeAllViews();

        // Создаем массив для отслеживания занятых ячеек
        boolean[][] occupied = new boolean[GRID_COLUMNS][GRID_ROWS];

        // Сначала размещаем все элементы
        for (int i = 0; i < desktopItems.size(); i++) {
            DesktopItem item = desktopItems.get(i);

            // Проверяем, не выходит ли элемент за границы
            if (item.getPositionX() < 0 || item.getPositionX() >= GRID_COLUMNS ||
                    item.getPositionY() < 0 || item.getPositionY() >= GRID_ROWS) {
                Log.w(TAG, "Item out of bounds: " + item.getPositionX() + "," + item.getPositionY());
                continue;
            }

            // Проверяем, не занята ли позиция
            if (occupied[item.getPositionX()][item.getPositionY()]) {
                Log.w(TAG, "Position occupied: " + item.getPositionX() + "," + item.getPositionY());
                continue;
            }

            // Находим соответствующее приложение
            AppData app = findAppForItem(item);
            if (app != null) {
                View appView = createAppView(app, item);
                desktopGrid.addView(appView);
                occupied[item.getPositionX()][item.getPositionY()] = true;
            }
        }
    }

    private AppData findAppForItem(DesktopItem item) {
        for (AppData app : desktopApps) {
            if (app.getPackageName().equals(item.getPackageName())) {
                return app;
            }
        }
        return null;
    }

    private View createAppView(AppData app, DesktopItem item) {
        View appView = LayoutInflater.from(getContext())
                .inflate(R.layout.desktop_app_item, desktopGrid, false);

        ImageView icon = appView.findViewById(R.id.desktopAppIcon);
        TextView name = appView.findViewById(R.id.desktopAppName);

        icon.setImageDrawable(app.getIcon());
        name.setText(app.getAppName());

        // Рассчитываем размер ячейки
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cellSize = screenWidth / GRID_COLUMNS;

        // Устанавливаем LayoutParams для GridLayout
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = cellSize;
        params.height = cellSize;
        params.rowSpec = GridLayout.spec(item.getPositionY(), 1);
        params.columnSpec = GridLayout.spec(item.getPositionX(), 1);
        appView.setLayoutParams(params);

        // Сохраняем позицию в тегах
        appView.setTag(R.id.item_grid_x, item.getPositionX());
        appView.setTag(R.id.item_grid_y, item.getPositionY());
        appView.setTag(R.id.item_package, item.getPackageName());

        // Настраиваем обработчики
        setupAppViewListeners(appView, app, item);

        return appView;
    }

    private void setupAppViewListeners(View appView, AppData app, DesktopItem item) {
        // Обработчик клика для запуска приложения
        appView.setOnClickListener(v -> {
            if (getActivity() instanceof LauncherActivity) {
                ((LauncherActivity) getActivity()).onAppClick(app.getPackageName());
            }
        });

        // Обработчик долгого нажатия для начала перетаскивания
        appView.setOnLongClickListener(v -> {
            Log.d(TAG, "Long press on item at " + item.getPositionX() + "," + item.getPositionY());
            startDrag(appView, item);
            return true;
        });

        // Также добавляем обработчик касания для отладки
        appView.setOnTouchListener(new View.OnTouchListener() {
            private long pressStartTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pressStartTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        long pressDuration = System.currentTimeMillis() - pressStartTime;
                        if (pressDuration > 500) {
                            Log.d(TAG, "Long touch detected: " + pressDuration + "ms");
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void startDrag(View view, DesktopItem item) {
        // Сохраняем стартовую позицию
        dragStartX = item.getPositionX();
        dragStartY = item.getPositionY();
        draggedView = view;

        // Делаем view полупрозрачным
        view.setAlpha(0.5f);

        // Создаем ClipData
        ClipData.Item clipItem = new ClipData.Item(item.getPackageName());
        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData dragData = new ClipData("DesktopItem", mimeTypes, clipItem);

        // Создаем тень
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

        // Начинаем перетаскивание
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            view.startDragAndDrop(dragData, shadowBuilder, view, 0);
        } else {
            view.startDrag(dragData, shadowBuilder, view, 0);
        }

        Log.d(TAG, "Drag started from position: " + dragStartX + "," + dragStartY);
    }

    private void setupDragAndDrop() {
        // Настраиваем слушатель Drag & Drop для GridLayout
        desktopGrid.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        Log.d(TAG, "Drag started in grid");
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.d(TAG, "Drag entered grid");
                        return true;

                    case DragEvent.ACTION_DRAG_LOCATION:
                        // Показываем, где сейчас находится перетаскиваемый элемент
                        float x = event.getX();
                        float y = event.getY();
                        highlightDropPosition(x, y);
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.d(TAG, "Drag exited grid");
                        clearHighlights();
                        return true;

                    case DragEvent.ACTION_DROP:
                        Log.d(TAG, "Drop at: " + event.getX() + "," + event.getY());

                        // Восстанавливаем прозрачность
                        if (draggedView != null) {
                            draggedView.setAlpha(1.0f);
                        }

                        // Определяем позицию дропа
                        float dropX = event.getX();
                        float dropY = event.getY();

                        int cellSize = desktopGrid.getWidth() / GRID_COLUMNS;
                        int dropGridX = (int) (dropX / cellSize);
                        int dropGridY = (int) (dropY / cellSize);

                        // Ограничиваем границами
                        dropGridX = Math.max(0, Math.min(dropGridX, GRID_COLUMNS - 1));
                        dropGridY = Math.max(0, Math.min(dropGridY, GRID_ROWS - 1));

                        Log.d(TAG, "Drop grid position: " + dropGridX + "," + dropGridY);

                        // Обрабатываем перемещение
                        handleDrop(dropGridX, dropGridY);

                        clearHighlights();
                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        Log.d(TAG, "Drag ended");

                        // Восстанавливаем прозрачность, даже если не было дропа
                        if (draggedView != null) {
                            draggedView.setAlpha(1.0f);
                            draggedView = null;
                        }

                        clearHighlights();
                        return true;
                }
                return false;
            }
        });
    }

    private void highlightDropPosition(float x, float y) {
        // Очищаем предыдущие подсветки
        clearHighlights();

        // Рассчитываем ячейку
        int cellSize = desktopGrid.getWidth() / GRID_COLUMNS;
        int gridX = (int) (x / cellSize);
        int gridY = (int) (y / cellSize);

        // Ограничиваем границами
        gridX = Math.max(0, Math.min(gridX, GRID_COLUMNS - 1));
        gridY = Math.max(0, Math.min(gridY, GRID_ROWS - 1));

        // Подсвечиваем ячейку
        for (int i = 0; i < desktopGrid.getChildCount(); i++) {
            View child = desktopGrid.getChildAt(i);
            Integer childX = (Integer) child.getTag(R.id.item_grid_x);
            Integer childY = (Integer) child.getTag(R.id.item_grid_y);

            if (childX != null && childY != null && childX == gridX && childY == gridY) {
                child.setBackgroundColor(0x40FF0000); // Красный с прозрачностью
                break;
            }
        }
    }

    private void clearHighlights() {
        for (int i = 0; i < desktopGrid.getChildCount(); i++) {
            View child = desktopGrid.getChildAt(i);
            child.setBackgroundColor(0x00000000); // Прозрачный
        }
    }

    private void handleDrop(int dropGridX, int dropGridY) {
        if (draggedView == null) {
            Log.e(TAG, "No dragged view!");
            return;
        }

        // Получаем позицию перетаскиваемого элемента из тегов
        Integer startX = (Integer) draggedView.getTag(R.id.item_grid_x);
        Integer startY = (Integer) draggedView.getTag(R.id.item_grid_y);
        String packageName = (String) draggedView.getTag(R.id.item_package);

        if (startX == null || startY == null || packageName == null) {
            Log.e(TAG, "Missing tag data!");
            return;
        }

        Log.d(TAG, "Moving item from " + startX + "," + startY + " to " + dropGridX + "," + dropGridY);

        // Проверяем, не тащим ли мы на ту же позицию
        if (startX == dropGridX && startY == dropGridY) {
            Log.d(TAG, "Same position, no move needed");
            return;
        }

        // Проверяем, не занята ли целевая позиция
        boolean targetOccupied = false;
        View occupiedView = null;
        Integer occupiedX = null;
        Integer occupiedY = null;

        for (int i = 0; i < desktopGrid.getChildCount(); i++) {
            View child = desktopGrid.getChildAt(i);
            if (child == draggedView) continue;

            Integer childX = (Integer) child.getTag(R.id.item_grid_x);
            Integer childY = (Integer) child.getTag(R.id.item_grid_y);

            if (childX != null && childY != null && childX == dropGridX && childY == dropGridY) {
                targetOccupied = true;
                occupiedView = child;
                occupiedX = childX;
                occupiedY = childY;
                break;
            }
        }

        if (targetOccupied && occupiedView != null && occupiedX != null && occupiedY != null) {
            // Меняем элементы местами
            Log.d(TAG, "Swapping items");

            // Обновляем параметры GridLayout для draggedView
            GridLayout.LayoutParams draggedParams = (GridLayout.LayoutParams) draggedView.getLayoutParams();
            draggedParams.columnSpec = GridLayout.spec(dropGridX, 1);
            draggedParams.rowSpec = GridLayout.spec(dropGridY, 1);
            draggedView.setLayoutParams(draggedParams);

            // Обновляем параметры GridLayout для occupiedView
            GridLayout.LayoutParams occupiedParams = (GridLayout.LayoutParams) occupiedView.getLayoutParams();
            occupiedParams.columnSpec = GridLayout.spec(startX, 1);
            occupiedParams.rowSpec = GridLayout.spec(startY, 1);
            occupiedView.setLayoutParams(occupiedParams);

            // Обновляем теги
            draggedView.setTag(R.id.item_grid_x, dropGridX);
            draggedView.setTag(R.id.item_grid_y, dropGridY);

            occupiedView.setTag(R.id.item_grid_x, startX);
            occupiedView.setTag(R.id.item_grid_y, startY);

            // Обновляем данные в менеджере
            desktopManager.moveItem(startX, startY, dropGridX, dropGridY);
            desktopManager.moveItem(occupiedX, occupiedY, startX, startY);

        } else {
            // Просто перемещаем элемент
            Log.d(TAG, "Moving to empty position");

            // Обновляем параметры GridLayout
            GridLayout.LayoutParams params = (GridLayout.LayoutParams) draggedView.getLayoutParams();
            params.columnSpec = GridLayout.spec(dropGridX, 1);
            params.rowSpec = GridLayout.spec(dropGridY, 1);
            draggedView.setLayoutParams(params);

            // Обновляем теги
            draggedView.setTag(R.id.item_grid_x, dropGridX);
            draggedView.setTag(R.id.item_grid_y, dropGridY);

            // Обновляем данные в менеджере
            desktopManager.moveItem(startX, startY, dropGridX, dropGridY);
        }

        // Принудительно перерисовываем Grid
        desktopGrid.requestLayout();
        desktopGrid.invalidate();

        Log.d(TAG, "Move completed successfully");
    }

    private void showAddAppsDialog() {
        // Временно просто добавим тестовое приложение
        addTestApp();
    }

    private void addTestApp() {
        // Добавляем тестовое приложение на первую свободную позицию
        int[] freePos = desktopManager.findFreePosition();
        if (freePos[0] != -1 && freePos[1] != -1) {
            // Здесь нужно получить реальное приложение, временно используем заглушку
            desktopManager.addAppToDesktop("com.android.settings", freePos[0], freePos[1]);
            refreshDesktop();
            Log.d(TAG, "Added test app at position: " + freePos[0] + "," + freePos[1]);
        } else {
            Log.d(TAG, "No free positions on desktop");
        }
    }

    private void clearDesktop() {
        // Удаляем все элементы с рабочего стола
        desktopManager.clearDesktop();
        refreshDesktop();
        Log.d(TAG, "Desktop cleared");
    }

    private void setupGestures(View view) {
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
    }

    public void refreshDesktop() {
        loadDesktopItems();
    }
}