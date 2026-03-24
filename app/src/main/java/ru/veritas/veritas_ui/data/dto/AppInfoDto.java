package ru.veritas.veritas_ui.data.dto;

import android.graphics.drawable.Drawable;

/**
 * Dto-класс приложения
 */
public class AppInfoDto {
    private String packageName;
    private String appName;
    private Drawable icon;

    public AppInfoDto(String packageName, String appName, Drawable icon) {
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