package ru.veritas.veritas_ui.data.source.local;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import ru.veritas.veritas_ui.data.dto.AppInfoDto;

import java.util.ArrayList;
import java.util.List;

public class PackageManagerDataSource {
    private final Context context;
    private final PackageManager packageManager;

    public PackageManagerDataSource(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
    }

    /**
     * Возращает все установленные приложения, имеющие лаунчер
     */
    public List<AppInfoDto> getInstalledApps() {
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        List<AppInfoDto> appInfoList = new ArrayList<>();

        for (ApplicationInfo app : apps) {
            // Фильтруем только те приложения, которые имеют лаунчер (android.intent.action.MAIN и категорию LAUNCHER)
            if (packageManager.getLaunchIntentForPackage(app.packageName) != null) {
                String appName = packageManager.getApplicationLabel(app).toString();
                Drawable icon = app.loadIcon(packageManager);
                appInfoList.add(new AppInfoDto(app.packageName, appName, icon));
            }
        }
        Log.d("AppDataSource", "Found " + appInfoList.size() + " apps with launcher");
        return appInfoList;
    }
}