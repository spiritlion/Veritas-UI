package ru.veritas.veritas_ui.domain.entities;

public class DragEventData {
    public enum SourceType { APPS, HOME, FAVORITES}

    private final SourceType sourceType;
    private final String packageName;    // для APPS
    private final String appName;        // для APPS
    private final int page;              // для HOME и FAVORITE
    private final int row;               // для HOME
    private final int col;               // для HOME
    private final int position;          // для FAVORITE

    // Конструктор для APP
    public DragEventData(String packageName, String appName) {
        this.sourceType = SourceType.APPS;
        this.packageName = packageName;
        this.appName = appName;
        this.page = -1; this.row = -1; this.col = -1; this.position = -1;
    }

    // Конструктор для HOME_
    public DragEventData(int page, int row, int col) {
        this.sourceType = SourceType.HOME;
        this.page = page; this.row = row; this.col = col;
        this.packageName = null; this.appName = null; this.position = -1;
    }

    // Конструктор для FAVORITE
    public DragEventData(int page, int position) {
        this.sourceType = SourceType.FAVORITES;
        this.page = page; this.position = position;
        this.row = -1; this.col = -1; this.packageName = null; this.appName = null;
    }


    public SourceType getSourceType() { return sourceType; }
    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public int getPage() { return page; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getPosition() { return position; }
}
