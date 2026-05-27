package ru.veritas.veritas_ui.activity.classic;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ru.veritas.veritas_ui.core.command.CommandFactory;
import ru.veritas.veritas_ui.ui.classic.apps.AppsScreenFragment;
import ru.veritas.veritas_ui.ui.classic.home.HomeScreenFragment;

public class MainPagerAdapter extends FragmentStateAdapter {
    private final CommandFactory commandFactory;
    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                            CommandFactory commandFactory) {
        super(fragmentActivity);
        this.commandFactory = commandFactory;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new HomeScreenFragment(
                    commandFactory.getHomeScreenFactory(),
                    commandFactory.getFavoritesFactory(),
                    commandFactory.getUseCaseFactory()
            );
        } else {
            return new AppsScreenFragment(commandFactory.getUseCaseFactory());
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}