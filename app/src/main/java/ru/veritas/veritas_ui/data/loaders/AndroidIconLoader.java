package ru.veritas.veritas_ui.data.loaders;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;

import ru.veritas.veritas_ui.domain.entities.AppIcon;
import ru.veritas.veritas_ui.domain.loaders.IconLoader;

public class AndroidIconLoader implements IconLoader {
    private final PackageManager pm;

    public AndroidIconLoader(PackageManager pm) {
        this.pm = pm;
    }

    @Override
    public AppIcon loadIcon(String packageName) {
        Drawable d;
        try {
            d = pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        byte[] bytes = convertDrawableToPng(d); // конвертация
        return new AppIcon(bytes);
    }

    private byte[] convertDrawableToPng(Drawable d) {
        Bitmap bitmap = Bitmap.createBitmap(
                d.getIntrinsicWidth(),
                d.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}