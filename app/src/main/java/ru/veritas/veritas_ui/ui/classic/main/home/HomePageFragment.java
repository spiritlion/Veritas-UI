package ru.veritas.veritas_ui.ui.classic.main.home;// HomePageFragment.java

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

import java.io.Serializable;
import java.util.List;

public class HomePageFragment extends Fragment {
    private static final String ARG_APPS_LIST = "apps_list";
    private static final String ARG_PAGE_INDEX = "page_index";
    private static final String ARG_COLUMN_COUNT = "column_count";

    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private ViewPagerPagesAdapter.OnItemClickListener listener;

    public static HomePageFragment newInstance(List<AppShortcutDTO> appsList, int pageIndex, int columnCount) {
        HomePageFragment fragment = new HomePageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_APPS_LIST, (Serializable) appsList);
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
        int columnCount = args != null ? args.getInt(ARG_COLUMN_COUNT, 4) : 4;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), columnCount));

        List<AppShortcutDTO> appsList = null;
        int pageIndex = 0;
        if (args != null) {
            appsList = (List<AppShortcutDTO>) args.getSerializable(ARG_APPS_LIST);
            pageIndex = args.getInt(ARG_PAGE_INDEX, 0);
        }

        adapter = new AppAdapter(appsList, requireContext(), listener, pageIndex, columnCount);
        recyclerView.setAdapter(adapter);
    }
}