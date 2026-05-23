package ru.veritas.veritas_ui.domain.entities;

import java.io.Serializable;

public class AppShortcut implements Serializable {
    private final String packageName;
    private final String appName;
    private final String icon;

    public AppShortcut(String packageName, String appName, String icon) {
        this.packageName = packageName;
        this.appName = appName;
        this.icon = icon;
    }

    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public String getIcon() { return icon; }
}
