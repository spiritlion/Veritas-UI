package ru.veritas.veritas_ui.ui.classic.main.home;// ViewPagerPagesAdapter.java

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

public class ViewPagerPagesAdapter extends FragmentStateAdapter {
    private final int columnCount;
    private final OnItemClickListener onItemClickListener;
    private int pageCount = 0;

    public ViewPagerPagesAdapter(OnItemClickListener listener,
                                 @NonNull FragmentActivity activity,
                                 int columnCount) {
        super(activity);
        this.onItemClickListener = listener;
        this.columnCount = columnCount;
    }

    public void setPageCount(int newCount) {
        if (this.pageCount != newCount) {
            this.pageCount = newCount;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
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