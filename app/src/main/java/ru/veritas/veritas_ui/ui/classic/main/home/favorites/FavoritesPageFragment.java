package ru.veritas.veritas_ui.ui.classic.main.home.favorites;
import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
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

import java.util.ArrayList;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.ui.classic.main.home.HomeScreenMode;
import ru.veritas.veritas_ui.ui.classic.main.home.HomeViewModel;

public class FavoritesPageFragment extends Fragment {
    private static final String ARG_PAGE_INDEX = "page_index";
    private static final int FAVORITE_COLUMNS = 5;
    private int pageIndex;
    private RecyclerView recyclerView;
    private FavoritesGridAdapter adapter;
    private FavoritesGridAdapter.OnItemClickListener listener; // сохранённый слушатель
    private View highlightedView = null;

    public static FavoritesPageFragment newInstance(int pageIndex) {
        FavoritesPageFragment fragment = new FavoritesPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_INDEX, pageIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.favoritesRecycler);
        pageIndex = getArguments() != null ? getArguments().getInt(ARG_PAGE_INDEX) : 0;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), FAVORITE_COLUMNS));
        adapter = new FavoritesGridAdapter(getContext(), pageIndex);
        recyclerView.setAdapter(adapter);

        // Если слушатель уже был установлен до вызова onViewCreated, применим его сейчас
        if (listener != null) {
            adapter.setListener(listener);
        }



        // Подписка на данные избранного
        HomeViewModel viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        viewModel.getFavoritesPages().observe(getViewLifecycleOwner(), pages -> {
            if (pageIndex < pages.size()) {
                adapter.updateData(pages.get(pageIndex));
            } else {
                adapter.updateData(new ArrayList<>());
            }
        });

        // FavoritesPageFragment.java – в onViewCreated после создания адаптера

        recyclerView.setOnDragListener((v, event) -> {
            // Работаем только в режиме Edit
            if (viewModel.getMode().getValue() != HomeScreenMode.Edit) return false;

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);

                case DragEvent.ACTION_DRAG_LOCATION: {
                    View child = recyclerView.findChildViewUnder(event.getX(), event.getY());
                    if (highlightedView != child) {
                        clearHighlight();
                        if (child != null) {
                            child.setBackgroundResource(R.drawable.highlight_border);
                            highlightedView = child;
                        }
                    }
                    return true;
                }

                case DragEvent.ACTION_DROP: {
                    clearHighlight();
                    viewModel.clearDragSource();
                    ClipData clipData = event.getClipData();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        String data = clipData.getItemAt(0).getText().toString();
                        String[] parts = data.split(":");
                        if (parts.length == 3) {
                            // Определяем целевую позицию под пальцем
                            View targetView = recyclerView.findChildViewUnder(event.getX(), event.getY());
                            int targetPos = (targetView != null) ? recyclerView.getChildAdapterPosition(targetView) : -1;

                            if ("favorites".equals(parts[0])) {
                                // Перемещение внутри избранного
                                if (targetPos != -1) {
                                    int srcPage = Integer.parseInt(parts[1]);
                                    int srcPos = Integer.parseInt(parts[2]);
                                    viewModel.swapFavorites(srcPage, srcPos, pageIndex, targetPos);
                                }
                            } else {
                                // Пришло с рабочего стола
                                int desktopPage = Integer.parseInt(parts[0]);
                                int desktopRow = Integer.parseInt(parts[1]);
                                int desktopCol = Integer.parseInt(parts[2]);
                                if (targetPos != -1) {
                                    viewModel.swapDesktopWithFavorites(
                                            desktopPage, desktopRow, desktopCol,
                                            pageIndex, targetPos
                                    );
                                }
                            }
                        }
                    }
                    viewModel.clearDragSource(); // убираем источник после обработки
                    return true;
                }

                case DragEvent.ACTION_DRAG_ENDED:
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

    /**
     * Устанавливает слушатель кликов на элементы избранного.
     * Может быть вызван как до, так и после создания адаптера.
     */
    public void setOnItemClickListener(FavoritesGridAdapter.OnItemClickListener listener) {
        this.listener = listener;
        if (adapter != null) {
            adapter.setListener(listener);
        }
    }
}