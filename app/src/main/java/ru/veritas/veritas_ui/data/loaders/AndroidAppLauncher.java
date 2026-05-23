package ru.veritas.veritas_ui.data.loaders;

import android.content.Context;
import android.content.Intent;

import ru.veritas.veritas_ui.domain.loaders.AppLauncher;

public class AndroidAppLauncher implements AppLauncher {
    private final Context context;

    public AndroidAppLauncher(Context context) {
        this.context = context;
    }

    @Override
    public void launch(String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) context.startActivity(intent);
    }
}
