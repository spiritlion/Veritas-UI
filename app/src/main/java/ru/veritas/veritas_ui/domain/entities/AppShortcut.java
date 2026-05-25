package ru.veritas.veritas_ui.domain.entities;

public class AppShortcut {
    private final String packageName;
    private final String appName;
    private final String iconSpec;

    public AppShortcut(String packageName, String appName, String iconSpec) {
        this.packageName = packageName;
        this.appName = appName;
        this.iconSpec = iconSpec;
    }

    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public String getIconSpec() { return iconSpec; }
}
