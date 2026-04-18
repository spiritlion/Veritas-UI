package ru.veritas.veritas_ui.ui.classic.main.home;// ViewPagerPagesAdapter.java

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.home.ToDoubleListUseCase;

public class ViewPagerPagesAdapter extends FragmentStateAdapter {
    private int columnCount = 4; // или передавайте извне

    public ViewPagerPagesAdapter(OnItemClickListener listener, FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(AppShortcutDTO shortcut);
        void onItemLongClick(int i, int j, int k, View v);
    }

    private final OnItemClickListener onItemClickListener;
    private List<List<AppShortcutDTO>> pagesData; // список страниц, каждая страница — список приложений

    public ViewPagerPagesAdapter(OnItemClickListener onItemClickListener,
                                 @NonNull FragmentActivity fragmentActivity,
                                 List<List<List<AppShortcutDTO>>> pagesData,
                                 int columnCount) {
        super(fragmentActivity);
        this.onItemClickListener = onItemClickListener;
        this.columnCount = columnCount;
        this.pagesData = ToDoubleListUseCase.invoke(pagesData);
    }

    public ViewPagerPagesAdapter(OnItemClickListener onItemClickListener, @NonNull FragmentActivity fragmentActivity,
                                 List<List<List<AppShortcutDTO>>> pagesData) {
        super(fragmentActivity);
        this.onItemClickListener = onItemClickListener;
        this.pagesData = ToDoubleListUseCase.invoke(pagesData);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        List<AppShortcutDTO> appsList = pagesData.get(position);
        HomePageFragment fragment = HomePageFragment.newInstance(appsList, position, columnCount);
        fragment.setOnItemClickListener(onItemClickListener);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return pagesData != null ? pagesData.size() : 0;
    }
}