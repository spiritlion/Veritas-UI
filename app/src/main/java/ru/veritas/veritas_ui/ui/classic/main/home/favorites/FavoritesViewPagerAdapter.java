package ru.veritas.veritas_ui.ui.classic.main.home.favorites;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.List;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

public class FavoritesViewPagerAdapter extends FragmentStateAdapter {
    private List<List<AppShortcutDTO>> pages;
    private int columnCount = 5;

    public FavoritesViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public FavoritesViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public void setPages(List<List<AppShortcutDTO>> pages) {
        this.pages = pages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return FavoritesPageFragment.newInstance(position, columnCount);
    }

    @Override
    public int getItemCount() {
        return pages == null ? 0 : pages.size();
    }
}