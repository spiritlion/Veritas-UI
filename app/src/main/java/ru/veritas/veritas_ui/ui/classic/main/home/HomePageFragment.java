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
                    return true;
                case DragEvent.ACTION_DROP:
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
                            View child = recyclerView.findChildViewUnder(x, y);
                            if (child != null) {
                                int targetPos = recyclerView.getChildAdapterPosition(child);
                                if (targetPos != RecyclerView.NO_POSITION) {
                                    int targetRow = targetPos / columnCount;
                                    int targetCol = targetPos % columnCount;
                                    viewModel.moveShortcut(fromPage, fromRow, fromCol,
                                            pageIndex, targetRow, targetCol);
                                }
                            }
                        }
                    }
                    return true;
                default:
                    return false;
            }
        });
    }
}