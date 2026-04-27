package ru.veritas.veritas_ui.domain.use_cases.local;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

@FunctionalInterface
public interface LaunchAppUseCase {
    void invoke(String packageName);

    static LaunchAppUseCase create(Context context) {
        return packageName -> {
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                context.startActivity(launchIntent);
            }
        };
    }
}