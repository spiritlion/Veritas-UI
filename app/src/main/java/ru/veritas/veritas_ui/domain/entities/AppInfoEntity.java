package ru.veritas.veritas_ui.domain.entities;

import android.graphics.drawable.Drawable;

/**
 * Data-класс приложения
 */
public class AppInfoEntity {
    private String packageName;
    private String appName;
    private Drawable icon;

    public AppInfoEntity(String packageName, String appName, Drawable icon) {
        this.packageName = packageName;
        this.appName = appName;
        this.icon = icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public Drawable getIcon() {
        return icon;
    }
}