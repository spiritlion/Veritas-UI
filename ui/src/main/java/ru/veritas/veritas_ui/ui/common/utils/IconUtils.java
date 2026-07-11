package ru.veritas.veritas_ui.ui.common.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import ru.veritas.veritas_ui.core.entities.AppIcon;

public class IconUtils {
    public static Drawable toDrawable(AppIcon icon, Resources resources) {
        if (icon == null || icon.getPngData() == null) return null;
        Bitmap bitmap = BitmapFactory.decodeByteArray(icon.getPngData(), 0, icon.getPngData().length);
        return new BitmapDrawable(resources, bitmap);
    }
}