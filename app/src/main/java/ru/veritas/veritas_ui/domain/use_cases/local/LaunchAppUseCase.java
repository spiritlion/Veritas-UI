package ru.veritas.veritas_ui.domain.use_cases.local;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class LaunchAppUseCase {
    private final Context context;

    public LaunchAppUseCase(Context context) {
        this.context = context;
    }

    /**
     * Запускает приложение находящееся в packageName
     * @param packageName
     */
    public void invoke(String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            context.startActivity(launchIntent);
        }
    }
}