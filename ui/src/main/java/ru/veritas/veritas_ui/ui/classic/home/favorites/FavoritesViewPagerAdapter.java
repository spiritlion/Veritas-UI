package ru.veritas.veritas_ui.ui.classic.home.favorites;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import ru.veritas.veritas_ui.core.command.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.core.command.local.settings.OpenSettingsUseCase;
import ru.veritas.veritas_ui.core.entities.AppShortcut;

public class FavoritesViewPagerAdapter extends FragmentStateAdapter {
    private List<List<AppShortcut>> pages;
    private int columnCount = 5;
    private GetAppIconUseCase getAppIconUseCase;
    private OpenSettingsUseCase openSettingsUseCase;

    public FavoritesViewPagerAdapter(@NonNull Fragment fragment,
                                     GetAppIconUseCase getAppIconUseCase,
                                     OpenSettingsUseCase openSettingsUseCase) {
        super(fragment);
        this.getAppIconUseCase = getAppIconUseCase;
        this.openSettingsUseCase = openSettingsUseCase;
    }

    public FavoritesViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public void setPages(List<List<AppShortcut>> pages) {
        this.pages = pages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return FavoritesPageFragment.newInstance(position, columnCount, getAppIconUseCase, openSettingsUseCase);
    }

    @Override
    public int getItemCount() {
        return pages == null ? 0 : pages.size();
    }
}