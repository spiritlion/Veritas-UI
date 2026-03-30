package ru.veritas.veritas_ui.domain.use_cases.local.home;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.data.repositories.HomeRepository;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;

public class GetShortcutsUseCase {
    private final HomeRepository homeRepository;
    private final PackageManager packageManager;

    public GetShortcutsUseCase(HomeRepository homeRepository, PackageManager packageManager) {
        this.homeRepository = homeRepository;
        this.packageManager = packageManager;
    }

    public List<AppShortcut> invoke() {
        List<AppShortcut> shortcuts = homeRepository.getShortcuts();
        List<AppShortcut> enriched = new ArrayList<>();
        for (AppShortcut shortcut : shortcuts) {
            try {
                Drawable icon = packageManager.getApplicationIcon(shortcut.getPackageName());
                String appName = packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(shortcut.getPackageName(), 0)).toString();
                enriched.add(new AppShortcut(shortcut.getPackageName(), appName, icon));
            } catch (PackageManager.NameNotFoundException e) {
                // приложение удалено, пропускаем
            }
        }
        return enriched;
    }
}