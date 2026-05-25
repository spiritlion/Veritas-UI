package ru.veritas.veritas_ui.ui.classic;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;

import ru.veritas.veritas_ui.di.DependencyContainer;
import ru.veritas.veritas_ui.ui.classic.apps.AppsScreenFragment;
import ru.veritas.veritas_ui.ui.classic.home.HomeScreenFragment;

public class VeritasFragmentFactory extends FragmentFactory {
    private final DependencyContainer container;

    public VeritasFragmentFactory(DependencyContainer container) {
        this.container = container;
    }

    @NonNull
    @Override
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
        // Обрабатываем только нужные фрагменты, остальные пусть создаются стандартно
        switch (className) {
            case "ru.veritas.veritas_ui.ui.classic.home.HomeScreenFragment":
                return new HomeScreenFragment(container);
            case "ru.veritas.veritas_ui.ui.classic.apps.AppsScreenFragment":
                return new AppsScreenFragment(container);
            default:
                return super.instantiate(classLoader, className);
        }
    }
}
