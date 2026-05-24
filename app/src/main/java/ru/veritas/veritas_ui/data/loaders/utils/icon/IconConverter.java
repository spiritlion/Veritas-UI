package ru.veritas.veritas_ui.data.loaders.utils.icon;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;

import ru.veritas.veritas_ui.domain.entities.AppIcon;

public class IconConverter {

    public static AppIcon convertDrawableToAppIcon(Drawable drawable) {
        if (drawable == null) return null;

        // Получаем размеры Drawable
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0) width = 96; // fallback
        if (height <= 0) height = 96;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return convertBitmapToAppIcon(bitmap);
    }

    public static AppIcon convertBitmapToAppIcon(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] pngData = stream.toByteArray();
        return new AppIcon(pngData);
    }
}