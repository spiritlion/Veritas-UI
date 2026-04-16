package ru.veritas.veritas_ui.domain.use_cases.local.home;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.data.repositories.HomeRepository;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

public class GetShortcutsUseCase {
    private final HomeRepository homeRepository;
    private final PackageManager packageManager;

    public GetShortcutsUseCase(HomeRepository homeRepository, PackageManager packageManager) {
        this.homeRepository = homeRepository;
        this.packageManager = packageManager;
    }

    public List<List<List<AppShortcutDTO>>> invoke() {
        return homeRepository.getShortcuts();
    }
}