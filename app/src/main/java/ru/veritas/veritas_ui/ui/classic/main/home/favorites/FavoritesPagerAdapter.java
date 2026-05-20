package ru.veritas.veritas_ui.ui.classic.main.home.favorites;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

public class FavoritesPagerAdapter extends FragmentStateAdapter {
    private int pageCount = 0;
    private final OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(AppShortcutDTO shortcut);
        void onFavoriteLongClick(AppShortcutDTO shortcut, int pageIndex, int position, View v);
    }

    public FavoritesPagerAdapter(@NonNull FragmentActivity fa, OnFavoriteClickListener listener) {
        super(fa);
        this.listener = listener;
    }

    public void setPageCount(int count) {
        pageCount = count;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        FavoritesPageFragment fragment = FavoritesPageFragment.newInstance(position);
        fragment.setOnItemClickListener(new FavoritesGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AppShortcutDTO shortcut) {
                listener.onFavoriteClick(shortcut);
            }

            @Override
            public void onItemLongClick(AppShortcutDTO shortcut, int pageIndex, int position, View v) {
                listener.onFavoriteLongClick(shortcut, pageIndex, position, v);
            }
        });
        return fragment;
    }

    @Override
    public int getItemCount() {
        return pageCount;
    }

    /**
     * Возвращает фрагмент по его позиции (использует стандартный тег FragmentStateAdapter).
     */
    public Fragment getFragment(int position, FragmentManager fm) {
        return fm.findFragmentByTag("f" + position);
    }
}