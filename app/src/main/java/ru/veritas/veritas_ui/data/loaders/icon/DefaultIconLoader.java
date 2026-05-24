package ru.veritas.veritas_ui.data.loaders.icon;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import ru.veritas.veritas_ui.data.loaders.utils.icon.IconConverter;
import ru.veritas.veritas_ui.domain.entities.AppIcon;
import ru.veritas.veritas_ui.domain.loaders.IconLoader;

public class DefaultIconLoader implements IconLoader {
    private final PackageManager pm;

    public DefaultIconLoader(PackageManager pm) {
        this.pm = pm;
    }

    @Nullable
    @Override
    public AppIcon loadIcon(String packageName, @Nullable String iconSpec) {
        // Если iconSpec == null, грузим стандартную иконку приложения
        try {
            Drawable d = pm.getApplicationIcon(packageName);
            return IconConverter.convertDrawableToAppIcon(d);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}