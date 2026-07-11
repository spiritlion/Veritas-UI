package ru.veritas.veritas_ui.data.loaders.icon;

import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import ru.veritas.veritas_ui.data.loaders.utils.icon.IconConverter;
import ru.veritas.veritas_ui.core.entities.AppIcon;
import ru.veritas.veritas_ui.core.loaders.IconLoader;

public class FileIconLoader implements IconLoader {

    @Nullable
    @Override
    public AppIcon loadIcon(String packageName, @Nullable String iconSpec) {
        if (iconSpec == null || !iconSpec.startsWith("image:")) return null;
        String path = iconSpec.substring(6);
        var bitmap = BitmapFactory.decodeFile(path);
        if (bitmap == null) return null;
        return IconConverter.convertBitmapToAppIcon(bitmap);
    }
}