package ru.veritas.veritas_ui.domain.entities;

import android.graphics.drawable.Drawable;

public class AppShortcut {
    private final String packageName;
    private final String appName;
    private final Drawable icon;

    public AppShortcut(String packageName, String appName, Drawable icon) {
        this.packageName = packageName;
        this.appName = appName;
        this.icon = icon;
    }

    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public Drawable getIcon() { return icon; }
}