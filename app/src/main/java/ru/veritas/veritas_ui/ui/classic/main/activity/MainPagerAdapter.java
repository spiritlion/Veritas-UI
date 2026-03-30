package ru.veritas.veritas_ui.ui.classic.main.activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ru.veritas.veritas_ui.ui.classic.main.apps.AppsScreenFragment;
import ru.veritas.veritas_ui.ui.classic.main.home.HomeScreenFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    private final FragmentActivity activity;
    private Fragment[] fragments = new Fragment[2]; // массив для хранения фрагментов

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.activity = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        if (position == 0) {
            fragment = new HomeScreenFragment();
        } else {
            fragment = new AppsScreenFragment();
        }
        fragments[position] = fragment; // сохраняем ссылку
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}