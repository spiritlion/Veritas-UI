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

import java.util.List;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.home.ToDoubleListUseCase;

public class HomePageFragment extends Fragment {
    private static final String ARG_PAGE_INDEX = "page_index";
    private static final String ARG_COLUMN_COUNT = "column_count";
    private static final int ROWS_PER_PAGE = 6;   // <-- восстановлена

    private View highlightedView = null; //       цель
    private View draggedView = null;          // источник
    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private ViewPagerPagesAdapter.OnItemClickListener listener;
    private HomeViewModel viewModel;
    private int pageIndex;
    private int columnCount;


    // Новый фабричный метод – только индекс и количество колонок
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
        if (args != null) {
            pageIndex = args.getInt(ARG_PAGE_INDEX, 0);
            columnCount = args.getInt(ARG_COLUMN_COUNT, 4);
        } else {
            pageIndex = 0;
            columnCount = 4;
        }

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), columnCount));

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int totalHeight = recyclerView.getHeight()
                    - recyclerView.getPaddingTop()
                    - recyclerView.getPaddingBottom();
            if (totalHeight > 0) {
                int itemHeight = totalHeight / ROWS_PER_PAGE; // 6 строк
                adapter.setItemHeightPx(itemHeight);
            }
        });

        // Адаптер изначально с пустыми данными
        adapter = new AppAdapter(null, requireContext(), listener, pageIndex, columnCount);
        recyclerView.setAdapter(adapter);

        // Подписываемся на ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof HomeScreenState.Content) {
                List<List<List<AppShortcutDTO>>> allPages = ((HomeScreenState.Content) state).getApps();
                List<List<AppShortcutDTO>> pages = ToDoubleListUseCase.invoke(allPages);
                if (pageIndex < pages.size()) {
                    adapter.updateData(pages.get(pageIndex));
                } else {
                    adapter.updateData(null); // страница удалена (редко)
                }
            }
        });

        // Drag & Drop
        recyclerView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    clearDragged(); // сбросим предыдущий, если был
                    int[] src = viewModel.getDragSource();
                    if (src[0] == pageIndex) {
                        int pos = src[1] * columnCount + src[2];
                        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(pos);
                        if (holder != null && holder.itemView != null) {
                            draggedView = holder.itemView;
                            draggedView.setBackgroundResource(R.drawable.highred_border); // красный полупрозрачный
                        }
                    }
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION: {
                    float x = event.getX();
                    float y = event.getY();
                    View child = recyclerView.findChildViewUnder(x, y);
                    if (child != null) {
                        if (highlightedView != child) {
                            clearHighlight();
                            child.setBackgroundResource(R.drawable.highlight_border);
                            highlightedView = child;
                        }
                    } else {
                        clearHighlight();
                    }
                    return true;
                }

                case DragEvent.ACTION_DROP:
                    clearHighlight();
                    clearDragged();
                    viewModel.clearDragSource();
                    ClipData clipData = event.getClipData();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        String data = clipData.getItemAt(0).getText().toString();
                        String[] parts = data.split(":");
                        if (parts.length == 3) {
                            int fromPage = Integer.parseInt(parts[0]);
                            int fromRow = Integer.parseInt(parts[1]);
                            int fromCol = Integer.parseInt(parts[2]);

                            float x = event.getX();
                            float y = event.getY();

                            // 1. Попытаться найти виджет под пальцем (самый точный метод)
                            View child = recyclerView.findChildViewUnder(x, y);
                            int targetPos = (child != null) ? recyclerView.getChildAdapterPosition(child) : RecyclerView.NO_POSITION;
                            int targetRow, targetCol;

                            if (targetPos != RecyclerView.NO_POSITION) {
                                targetRow = targetPos / columnCount;
                                targetCol = targetPos % columnCount;
                            } else {
                                // 2. Запасной геометрический расчёт, если recyclerView ещё не разложил элементы
                                int rvWidth = recyclerView.getWidth();
                                int rvHeight = recyclerView.getHeight();
                                if (rvWidth == 0 || rvHeight == 0) return true;
                                int cellWidth = rvWidth / columnCount;
                                int cellHeight = rvHeight / ROWS_PER_PAGE;
                                targetCol = Math.min((int) (x / cellWidth), columnCount - 1);
                                targetRow = Math.min((int) (y / cellHeight), ROWS_PER_PAGE - 1);
                            }

                            Log.d("DragDrop", String.format("%d %d %d -> %d %d %d",
                                    fromPage, fromRow, fromCol, pageIndex, targetRow, targetCol));
                            viewModel.moveShortcut(fromPage, fromRow, fromCol, pageIndex, targetRow, targetCol);
                        }
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    clearHighlight();
                    clearDragged();
                    viewModel.clearDragSource();
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

    private void clearDragged() {
        if (draggedView != null) {
            draggedView.setBackground(null);
            draggedView = null;
        }
    }
}