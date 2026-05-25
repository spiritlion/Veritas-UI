package ru.veritas.veritas_ui.ui.classic.activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ru.veritas.veritas_ui.di.DependencyContainer;
import ru.veritas.veritas_ui.ui.classic.apps.AppsScreenFragment;
import ru.veritas.veritas_ui.ui.classic.home.HomeScreenFragment;

public class MainPagerAdapter extends FragmentStateAdapter {
    private final DependencyContainer container;

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity, DependencyContainer container) {
        super(fragmentActivity);
        this.container = container;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new HomeScreenFragment(container);
        } else {
            return new AppsScreenFragment(container);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}