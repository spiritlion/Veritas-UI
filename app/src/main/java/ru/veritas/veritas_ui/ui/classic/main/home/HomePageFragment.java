package ru.veritas.veritas_ui.ui.classic.main.home;

import android.content.ClipData;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.home.ToDoubleListUseCase;

import java.util.List;

public class HomePageFragment extends Fragment {
    private static final String ARG_PAGE_INDEX = "page_index";
    private static final String ARG_COLUMN_COUNT = "column_count";

    // Порог края в dp — должен совпадать с AppAdapter
    private static final int EDGE_THRESHOLD_DP = 64;

    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private ViewPagerPagesAdapter.OnItemClickListener listener;
    private View highlightedView = null;

    public static HomePageFragment newInstance(int pageIndex, int columnCount) {
        HomePageFragment fragment = new HomePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_INDEX, pageIndex);
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnItemClickListener(ViewPagerPagesAdapter.OnItemClickListener listener) {
        this.listener = listener;
        if (adapter != null) {
            adapter.setListener(listener);
        }
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

        adapter = new AppAdapter(null, requireContext(), listener, pageIndex, columnCount);

        // Перемещение иконок между ячейками
        adapter.setDragDropListener((fromPage, fromRow, fromCol, targetPage, targetRow, targetCol) ->
                viewModel.moveShortcut(fromPage, fromRow, fromCol, targetPage, targetRow, targetCol));

        // Передаём drag-позицию от ViewHolder вверх во ViewModel.
        // ViewHolder получает DRAG_LOCATION, когда drag находится над конкретной карточкой.
        adapter.setDragEdgeListener(direction -> viewModel.setDragEdge(direction));

        recyclerView.setAdapter(adapter);

        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof HomeScreenState.Content) {
                List<List<List<AppShortcutDTO>>> allPages = ((HomeScreenState.Content) state).getApps();
                List<List<AppShortcutDTO>> page = ToDoubleListUseCase.invoke(allPages);
                if (pageIndex < page.size()) {
                    adapter.updateData(page.get(pageIndex));
                }
            }
        });

        setupDragListener(columnCount, pageIndex, viewModel);
    }

    /**
     * RecyclerView получает DRAG_LOCATION, когда drag находится над пустым местом
     * (там, где нет карточки). Карточки обрабатывают DRAG_LOCATION сами через ViewHolder.
     */
    private void setupDragListener(int columnCount, int pageIndex, HomeViewModel viewModel) {
        recyclerView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d("page", "started");
                    viewModel.setDragging(true);
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION: {
                    float x = event.getX();
                    float y = event.getY();
                    View child = recyclerView.findChildViewUnder(x, y);
                    if (child != null) {
                        if (highlightedView != child) {
                            clearHighlight();
                            Log.d("page", "location");
                            child.setBackgroundResource(R.drawable.highlight_border);
                            highlightedView = child;
                        }
                    } else {
                        clearHighlight();
                    }

                    int[] recyclerLocation = new int[2];
                    recyclerView.getLocationOnScreen(recyclerLocation);

                    float absX = recyclerLocation[0] + x;
                    float screenWidth = getResources().getDisplayMetrics().widthPixels;
                    float edgePx = EDGE_THRESHOLD_DP * getResources().getDisplayMetrics().density;

                    int direction;
                    if (absX < edgePx) {
                        direction = -1;   // левый край
                    } else if (absX > screenWidth - edgePx) {
                        direction = 1;    // правый край
                    } else {
                        direction = 0;
                    }
                    viewModel.setDragEdge(direction);
                    return true;
                }


                case DragEvent.ACTION_DROP: {
                    Log.d("page", "drop");
                    viewModel.setDragEdge(0);
                    clearHighlight();
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
                        int targetRow = targetPos / columnCount;
                        int targetCol = targetPos % columnCount;

                        if (parts.length == 3) {
                            // Перемещение внутри рабочего стола
                            int fromPage = Integer.parseInt(parts[0]);
                            int fromRow = Integer.parseInt(parts[1]);
                            int fromCol = Integer.parseInt(parts[2]);
                            viewModel.moveShortcut(fromPage, fromRow, fromCol,
                                    pageIndex, targetRow, targetCol);
                        } else if (parts.length == 2) {
                            // Перемещение из избранного на рабочий стол
                            int fromFavPage = Integer.parseInt(parts[0]);
                            int fromFavPos = Integer.parseInt(parts[1]);
                            viewModel.swapDesktopWithFavorites(
                                    pageIndex, targetRow, targetCol,
                                    fromFavPage, fromFavPos);
                        }
                    }
                    return false;
                }

                case DragEvent.ACTION_DRAG_ENDED:
                    Log.d("page","ended");
                    viewModel.setDragEdge(0);
                    viewModel.setDragging(false);
                    clearHighlight();
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d("page","exited");
                    viewModel.setDragEdge(0);
                    // viewModel.setDragging(false);
                    clearHighlight();
                    return true;

                default:
                    return false;
            }
        });
    }
    private void clearHighlight() {
        if (highlightedView != null) {
            highlightedView.setBackground(null);
            highlightedView = null;
        }
    }

    public RecyclerView getRecyclerView() {
        return this.recyclerView;
    }
}