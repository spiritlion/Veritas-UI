package ru.veritas.veritas_ui.data.loaders;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import ru.veritas.veritas_ui.core.exceptions.AppLaunchException;
import ru.veritas.veritas_ui.core.loaders.AppLauncher;

public class AndroidAppLauncher implements AppLauncher {
    private final Context context;

    public AndroidAppLauncher(Context context) {
        this.context = context;
    }

    @Override
    public void launch(String packageName) throws AppLaunchException {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent == null) {
                throw new AppLaunchException("Приложение не найдено: " + packageName);
            }
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            throw new AppLaunchException("Не удалось открыть приложение", e);
        }
    }
}
