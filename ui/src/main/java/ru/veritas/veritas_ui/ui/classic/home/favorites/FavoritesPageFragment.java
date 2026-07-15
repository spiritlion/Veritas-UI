package ru.veritas.veritas_ui.ui.classic.home.favorites;

import android.content.ClipData;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.veritas.veritas_ui.ui.R;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.command.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.ui.classic.home.HomeScreenState;
import ru.veritas.veritas_ui.ui.classic.home.HomeViewModel;
import ru.veritas.veritas_ui.ui.common.utils.DragHighlightHelper;
import ru.veritas.veritas_ui.ui.common.utils.EdgeAutoScrollController;

/**
 * Фрагмент страницы избранного, аналогичный HomePageFragment, но с FavoritesAdapter.
 */
public class FavoritesPageFragment extends Fragment {

    private static final String ARG_PAGE_INDEX = "page_index";
    private static final String ARG_COLUMN_COUNT = "column_count";

    private static final int EDGE_THRESHOLD_DP = EdgeAutoScrollController.DEFAULT_EDGE_THRESHOLD_DP;

    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private FavoritesAdapter.OnItemClickListener listener;
    private FavoritesAdapter.OnItemMenuClickListener menuListener;
    private FavoritesAdapter.OnSpecificItemClickListener specificClickListener;
    private final DragHighlightHelper highlightHelper = new DragHighlightHelper(R.drawable.highlight_border);

    private int computedItemHeight = 0;
    private boolean needsHeightRecalculation = true;
    private ViewTreeObserver.OnGlobalLayoutListener layoutListener;

    private final GetAppIconUseCase getAppIconUseCase;

    public FavoritesPageFragment(GetAppIconUseCase getAppIconUseCase) {
        this.getAppIconUseCase = getAppIconUseCase;
    }

    public static FavoritesPageFragment newInstance(int pageIndex, int columnCount,
                                                    GetAppIconUseCase getAppIconUseCase) {
        FavoritesPageFragment fragment = new FavoritesPageFragment(getAppIconUseCase);
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_INDEX, pageIndex);
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Установка слушателей для кликов
     */
    public void setOnItemClickListener(FavoritesAdapter.OnItemClickListener listener) {
        this.listener = listener;
        if (adapter != null) {
            adapter.setListener(listener);
        }
    }

    /**
     * Установка слушателя для меню.
     */
    public void setOnItemMenuClickListener(FavoritesAdapter.OnItemMenuClickListener menuListener) {
        this.menuListener = menuListener;
        if (adapter != null)
            adapter.setMenuListener(menuListener);
    }

    public void setSpecificClickListener(FavoritesAdapter.OnSpecificItemClickListener specificClickListener) {
        this.specificClickListener = specificClickListener;
        if (adapter != null)
            adapter.setSpecificListener(specificClickListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.item_home_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerPage);

        Bundle args = getArguments();
        int pageIndex = args != null ? args.getInt(ARG_PAGE_INDEX, 0) : 0;
        int columnCount = args != null ? args.getInt(ARG_COLUMN_COUNT, 4) : 4;

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), columnCount));

        HomeViewModel viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        adapter = new FavoritesAdapter(null, pageIndex, columnCount, getAppIconUseCase);
        adapter.setListener(listener);
        adapter.setMenuListener(menuListener);
        adapter.setSpecificListener(specificClickListener);
        recyclerView.setAdapter(adapter);

        // Подписка на состояние HomeViewModel
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof HomeScreenState.Content) {
                // Предполагаем, что в Content есть метод getFavoritesPages() -> List<List<AppShortcut>>
                List<List<AppShortcut>> favoritesPages = ((HomeScreenState.Content) state).getFavApps();
                if (favoritesPages != null && pageIndex < favoritesPages.size()) {
                    List<AppShortcut> pageApps = favoritesPages.get(pageIndex);
                    adapter.updateData(pageApps);
                }
            }
        });

        setupDragListener(columnCount, pageIndex, viewModel);

        // Слушатель изменения размера для пересчета высоты ячеек
        layoutListener = () -> {
            if (recyclerView.getHeight() > 0 && needsHeightRecalculation) {
                adjustItemHeights();
            }
        };
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        // Пересчет при обновлении данных
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof HomeScreenState.Content) {
                needsHeightRecalculation = true;
                recyclerView.post(this::adjustItemHeights);
            }
        });
    }

    private void adjustItemHeights() {
        if (adapter == null || recyclerView.getHeight() == 0) return;

        int itemCount = adapter.getItemCount();
        if (itemCount == 0) return;

        int spanCount = ((GridLayoutManager) recyclerView.getLayoutManager()).getSpanCount();
        int rowCount = (int) Math.ceil((double) itemCount / spanCount);
        int availableHeight = recyclerView.getHeight() - recyclerView.getPaddingTop() - recyclerView.getPaddingBottom();

        if (rowCount <= 0) return;
        int newItemHeight = availableHeight / rowCount;

        if (newItemHeight == computedItemHeight) return;
        computedItemHeight = newItemHeight;

        adapter.setItemHeight(computedItemHeight);

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            ViewGroup.LayoutParams params = child.getLayoutParams();
            if (params != null) {
                params.height = computedItemHeight;
                child.setLayoutParams(params);
            }
        }

        needsHeightRecalculation = false;
    }

    /**
     * Обработка drag & drop.
     * Аналогична HomePageFragment, но с учетом работы с избранным.
     */
    private void setupDragListener(int columnCount, int pageIndex, HomeViewModel viewModel) {
        recyclerView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d("FavoritesPage", "drag started");
                    viewModel.setDragging(true);
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION: {
                    float x = event.getX();
                    float y = event.getY();
                    View child = recyclerView.findChildViewUnder(x, y);
                    highlightHelper.highlight(child);

                    int[] recyclerLocation = new int[2];
                    recyclerView.getLocationOnScreen(recyclerLocation);
                    float absX = recyclerLocation[0] + x;
                    float screenWidth = getResources().getDisplayMetrics().widthPixels;
                    float edgePx = EDGE_THRESHOLD_DP * getResources().getDisplayMetrics().density;

                    int direction = EdgeAutoScrollController.computeDirection(absX, screenWidth, edgePx);
                    viewModel.setDragEdge(direction); // TODO сделать такую же логику как и с рабочим столом, только для favorite бара. А то я просто не успел перед отъездом в лагерь
                    return true;
                }

                case DragEvent.ACTION_DROP: {
                    Log.d("FavoritesPage", "drop");
                    viewModel.setDragEdge(0);
                    highlightHelper.clear();
                    ClipData clipData = event.getClipData();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        String data = clipData.getItemAt(0).getText().toString();
                        String[] parts = data.split(":");
                        float x = event.getX();
                        float y = event.getY();
                        View child = recyclerView.findChildViewUnder(x, y);
                        if (child == null) return false;
                        int targetPos = recyclerView.getChildAdapterPosition(child);
                        if (targetPos == RecyclerView.NO_POSITION) return false;
                        // В избранном мы используем позицию (индекс) напрямую
                        int targetIndex = targetPos;

                        switch (parts[0]) {
                            case "app": {
                                // Добавление приложения в избранное
                                String packageName = parts[1];
                                String appTitle = parts[2];
                                AppShortcut shortcut = new AppShortcut(packageName, appTitle, null);
                                viewModel.addToFavorites(shortcut, pageIndex, targetIndex);
                                return true;
                            }

                            case "home": {
                                // Перемещение с рабочего стола в избранное
                                int fromPage = Integer.parseInt(parts[1]);
                                int fromRow = Integer.parseInt(parts[2]);
                                int fromCol = Integer.parseInt(parts[3]);
                                viewModel.swapShortcutWithFavoriteAndHome(fromPage, fromRow, fromCol, pageIndex, targetIndex);
                                return true;
                            }

                            case "fav": {
                                // Перемещение внутри избранного
                                int fromPage = Integer.parseInt(parts[1]);
                                int fromPos = Integer.parseInt(parts[2]);
                                viewModel.swapFavorites(fromPage, fromPos, pageIndex, targetIndex);
                                return true;
                            }
                        }
                    }
                    return false;
                }

                case DragEvent.ACTION_DRAG_ENDED:
                    Log.d("FavoritesPage", "drag ended");
                    viewModel.setDragEdge(0);
                    viewModel.setDragging(false);
                    highlightHelper.clear();
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d("FavoritesPage", "drag exited");
                    viewModel.setDragEdge(0);
                    highlightHelper.clear();
                    return true;

                default:
                    return false;
            }
        });
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (recyclerView != null && layoutListener != null) {
            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
        }
    }
}