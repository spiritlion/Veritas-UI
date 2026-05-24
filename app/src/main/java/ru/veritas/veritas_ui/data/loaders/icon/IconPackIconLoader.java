package ru.veritas.veritas_ui.data.loaders.icon;

import android.content.pm.PackageManager;

import androidx.annotation.Nullable;

import ru.veritas.veritas_ui.domain.entities.AppIcon;
import ru.veritas.veritas_ui.domain.loaders.IconLoader;

public class IconPackIconLoader implements IconLoader {
    private final PackageManager pm;

    public IconPackIconLoader(PackageManager pm) {
        this.pm = pm;
    }

    @Nullable
    @Override
    public AppIcon loadIcon(String packageName, @Nullable String iconSpec) {
        if (iconSpec == null || !iconSpec.startsWith("pack:")) return null;
        String[] parts = iconSpec.split(":");
        if (parts.length < 3) return null;
        String packPackage = parts[1];
        String iconId = parts[2];

        // TODO: реализовать загрузку ресурса из другого пакета
        // Например, через PackageManager.getResourcesForApplication(packPackage)
        // и получение Drawable по имени ресурса iconId
        // Пока возвращаем null, что приведёт к использованию следующей стратегии (например, DefaultIconLoader)
        return null;
    }
}