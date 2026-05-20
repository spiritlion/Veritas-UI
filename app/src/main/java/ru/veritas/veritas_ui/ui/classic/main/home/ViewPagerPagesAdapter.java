package ru.veritas.veritas_ui.ui.classic.main.home;// ViewPagerPagesAdapter.java

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

public class ViewPagerPagesAdapter extends FragmentStateAdapter {
    private int columnCount = 4;
    private OnItemClickListener onItemClickListener;
    private List<List<AppShortcutDTO>> pagesData;

    public interface OnItemClickListener {
        void onItemClick(AppShortcutDTO shortcut);
        void onItemLongClick(int page, int row, int col, View v);
    }

    public ViewPagerPagesAdapter(OnItemClickListener listener,
                                 @NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.onItemClickListener = listener;
    }

    public ViewPagerPagesAdapter(OnItemClickListener listener,
                                 @NonNull FragmentActivity fragmentActivity,
                                 List<List<List<AppShortcutDTO>>> pagesData,
                                 int columnCount) {
        super(fragmentActivity);
        this.onItemClickListener = listener;
        this.columnCount = columnCount;
    }

    public void setPagesData(List<List<List<AppShortcutDTO>>> pagesData) {
        this.pagesData = ToDoubleListUseCase.invoke(pagesData);
        notifyDataSetChanged(); // ← это было пропущено
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Только pageIndex и columnCount — данные фрагмент получит сам из ViewModel
        HomePageFragment fragment = HomePageFragment.newInstance(position, columnCount);
        fragment.setOnItemClickListener(onItemClickListener);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return pageCount;
    }

    public interface OnItemClickListener {
        void onItemClick(AppShortcutDTO shortcut);
        void onItemLongClick(int i, int j, int k, View v);
    }
}