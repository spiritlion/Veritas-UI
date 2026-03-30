package ru.veritas.veritas_ui.domain.use_cases.local.home;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import ru.veritas.veritas_ui.data.repositories.HomeRepository;

public class AddShortcutUseCase {
    private final HomeRepository homeRepository;
    private final PackageManager packageManager;

    public AddShortcutUseCase(HomeRepository homeRepository, PackageManager packageManager) {
        this.homeRepository = homeRepository;
        this.packageManager = packageManager;
    }

    public void invoke(String packageName) {
        try {
            Drawable icon = packageManager.getApplicationIcon(packageName);
            String appName = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(packageName, 0)).toString();
            homeRepository.addShortcut(packageName, appName, icon);
        } catch (PackageManager.NameNotFoundException e) {
            // ignore
        }
    }
}