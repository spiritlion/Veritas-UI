package ru.veritas.veritas_ui.domain.use_cases.local.home;

import android.content.pm.PackageManager;

import ru.veritas.veritas_ui.data.repositories.HomeRepository;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

public class AddShortcutUseCase {
    private final HomeRepository homeRepository;
    private final PackageManager packageManager;

    public AddShortcutUseCase(HomeRepository homeRepository, PackageManager packageManager) {
        this.homeRepository = homeRepository;
        this.packageManager = packageManager;
    }

    public void invoke(int i, int j, int k, AppShortcutDTO shortcut) {
        homeRepository.addShortcut(i, j, k, shortcut);
    }

    public void invoke(AppShortcutDTO shortcut) {
        homeRepository.addShortcut(shortcut);
    }
}