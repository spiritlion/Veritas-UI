package ru.veritas.veritas_ui.domain.use_cases.local.home;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import ru.veritas.veritas_ui.domain.entities.AppIcon;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.loaders.IconLoader;

public class GetAppIconUseCase {
    private final IconLoader iconLoader;
    public GetAppIconUseCase(IconLoader iconLoader) {
        this.iconLoader = iconLoader;
    }
    public AppIcon invoke(AppShortcut shortcut) {
        return iconLoader.loadIcon(shortcut.getPackageName());
    }
}