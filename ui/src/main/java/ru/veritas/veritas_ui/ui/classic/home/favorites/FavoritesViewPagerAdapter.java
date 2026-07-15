package ru.veritas.veritas_ui.ui.classic.home.favorites;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import ru.veritas.veritas_ui.core.command.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.core.entities.AppShortcut;

public class FavoritesViewPagerAdapter extends FragmentStateAdapter {
    private List<List<AppShortcut>> pages;
    private int columnCount = 5;
    private GetAppIconUseCase getAppIconUseCase;

    private final FavoritesAdapter.OnItemClickListener listener;
    private final FavoritesAdapter.OnItemMenuClickListener menuListener;
    private final FavoritesAdapter.OnSpecificItemClickListener specificClickListener;

    public FavoritesViewPagerAdapter(@NonNull FragmentActivity fragment,
                                     GetAppIconUseCase getAppIconUseCase,
                                     FavoritesAdapter.OnItemClickListener listener,
                                     FavoritesAdapter.OnItemMenuClickListener menuListener,
                                     FavoritesAdapter.OnSpecificItemClickListener specificClickListener) {
        super(fragment);
        this.getAppIconUseCase = getAppIconUseCase;
        this.listener = listener;
        this.menuListener = menuListener;
        this.specificClickListener = specificClickListener;
    }

    public void setPages(List<List<AppShortcut>> pages) {
        this.pages = pages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        FavoritesPageFragment fragment = FavoritesPageFragment.newInstance(position, columnCount, getAppIconUseCase);
        fragment.setOnItemClickListener(listener);
        fragment.setOnItemMenuClickListener(menuListener);
        fragment.setSpecificClickListener(specificClickListener);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return pages == null ? 0 : pages.size();
    }
}