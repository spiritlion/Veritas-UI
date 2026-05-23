package ru.veritas.veritas_ui.ui.classic.home.favorites;

import android.content.ClipData;
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
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;
import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.data.loaders.AndroidIconLoader;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.ui.classic.activity.MainActivity;
import ru.veritas.veritas_ui.ui.classic.home.AppAdapter;
import ru.veritas.veritas_ui.ui.classic.home.HomeScreenFragment;
import ru.veritas.veritas_ui.ui.classic.home.HomeViewModel;

public class FavoritesPageFragment extends Fragment {
    private static final String ARG_PAGE_INDEX = "page_index";
    private static final String ARG_COLUMN_COUNT = "column_count";
    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private View highlightedView;

    public static FavoritesPageFragment newInstance(int pageIndex, int columnCount) {
        FavoritesPageFragment fragment = new FavoritesPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_INDEX, pageIndex);
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.favoritesRecycler);
        int columnCount = getArguments().getInt(ARG_COLUMN_COUNT, 5);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), columnCount));
        adapter = new FavoritesAdapter(requireActivity(),
                new GetAppIconUseCase(new AndroidIconLoader(getContext().getPackageManager())),
                getArguments().getInt(ARG_PAGE_INDEX, 0),
                columnCount);
        recyclerView.setAdapter(adapter);

        // Подписываемся на данные избранного
        HomeViewModel viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        viewModel.getFavoritesPages().observe(getViewLifecycleOwner(), pages -> {
            if (pages != null) {
                int pageIndex = getArguments().getInt(ARG_PAGE_INDEX, 0);
                if (pageIndex < pages.size()) {
                    adapter.updateData(pages.get(pageIndex));
                } else {
                    adapter.updateData(null); // или пустой список
                }
            }
        });

        recyclerView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION: {
                    float x = event.getX();
                    float y = event.getY();
                    View child = recyclerView.findChildViewUnder(x, y);
                    highlightChild(child);

                    // Вызываем метод родительского фрагмента для автоскролла
                    Fragment parent = getParentFragment();
                    if (parent instanceof HomeScreenFragment) {
                        ((HomeScreenFragment) parent).onFavoritesDragLocation(x, y);
                    }
                    return true;
                }
                case DragEvent.ACTION_DROP:
                    handleDrop(event);
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    clearHighlight();
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    clearHighlight();
                    Fragment parent = getParentFragment();
                    if (parent instanceof HomeScreenFragment) {
                        ((HomeScreenFragment) parent).onFavoritesDragEnd();
                    }
                    return true;
            }
            return false;
        });

        adapter.setSpecialIconClickListener(() -> {
            if (getActivity() instanceof MainActivity) {
                ViewPager2 viewPager = ((MainActivity) getActivity()).getViewPager();
                if (viewPager != null) {
                    viewPager.setCurrentItem(1, true);
                }
            }
        });
    }

    private void handleDrop(DragEvent event) {
        ClipData clipData = event.getClipData();
        if (clipData == null || clipData.getItemCount() == 0) return;
        String data = clipData.getItemAt(0).getText().toString();
        String[] parts = data.split(":");

        int pageIndex = getArguments().getInt(ARG_PAGE_INDEX, 0);
        HomeViewModel viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        float x = event.getX();
        float y = event.getY();
        View child = recyclerView.findChildViewUnder(x, y);
        if (child == null) return;
        int targetPos = recyclerView.getChildAdapterPosition(child);
        if (targetPos == RecyclerView.NO_POSITION) return;
        switch (parts[0]) {
            case "app": {
                String packageName = parts[1];
                String appTitle = parts[2];
                AppShortcut shortcut = new AppShortcut(packageName, appTitle, null);
                viewModel.addToFavoritesAtPosition(shortcut, pageIndex, targetPos);
                return;
            }

            case "home": {
                int fromPage = Integer.parseInt(parts[1]);
                int fromRow = Integer.parseInt(parts[2]);
                int fromCol = Integer.parseInt(parts[3]);
                viewModel.swapDesktopWithFavorites(fromPage, fromRow, fromCol, pageIndex, targetPos);
                return;
            }

            case "fav": {
                int srcPage = Integer.parseInt(parts[1]);
                int srcPos = Integer.parseInt(parts[2]);
                viewModel.swapFavorites(srcPage, srcPos, pageIndex, targetPos);
                return;
            }
        }
    }

    private void highlightChild(View child) {
        if (highlightedView == child) return;
        clearHighlight();
        if (child != null) {
            child.setBackgroundResource(R.drawable.highlight_border);
            highlightedView = child;
        }
    }

    private void clearHighlight() {
        if (highlightedView != null) {
            highlightedView.setBackground(null);
            highlightedView = null;
        }
    }

    public void updateData(List<AppShortcut> pageData) {
        if (adapter != null) adapter.updateData(pageData);
    }

    public void setDragDropListener(FavoritesAdapter.DragDropListener listener) {
        if (adapter != null) adapter.setDragDropListener(listener);
    }

    public void setDragEdgeListener(AppAdapter.DragEdgeListener listener) {
        if (adapter != null) adapter.setDragEdgeListener(listener);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }
}