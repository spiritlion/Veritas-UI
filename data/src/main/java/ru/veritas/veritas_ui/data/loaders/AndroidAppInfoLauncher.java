package ru.veritas.veritas_ui.data.loaders;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import ru.veritas.veritas_ui.core.exceptions.AppLaunchException;
import ru.veritas.veritas_ui.core.loaders.AppLauncher;

public class AndroidAppInfoLauncher implements AppLauncher {
    private final Context context;

    public AndroidAppInfoLauncher(Context context) {
        this.context = context;
    }

    @Override
    public void launch(String packageName) throws AppLaunchException {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            Uri uri = Uri.parse("package:" + packageName);
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (PackageManager.NameNotFoundException e) {
            throw new AppLaunchException("Приложение не найдено: " + packageName, e);
        } catch (ActivityNotFoundException e) {
            throw new AppLaunchException("Не удалось открыть настройки приложения", e);
        }
    }
}
