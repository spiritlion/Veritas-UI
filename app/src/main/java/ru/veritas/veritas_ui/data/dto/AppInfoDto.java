package ru.veritas.veritas_ui.data.dto;

/**
 * Dto-класс приложения
 */
public class AppInfoDto {
    private String packageName;
    private String appName;

    public AppInfoDto(String packageName, String appName) {
        this.packageName = packageName;
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }
    public String getAppName() {
        return appName;
    }
}