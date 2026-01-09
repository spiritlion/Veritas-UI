// Обновим DesktopItem.java, добавив новые поля:
package ru.veritas.veritas_ui.managers.main.desktop;

public class DesktopItem {
    public enum Type {
        APP,
        WIDGET
    }

    private Type type;
    private String packageName;
    private int widgetId;
    private int positionX;
    private int positionY;
    private int width = 1;
    private int height = 1;
    private String appName; // Добавляем имя приложения
    private boolean isDraggable = true; // Можно ли перетаскивать

    // Конструкторы
    public DesktopItem(Type type, String packageName, int positionX, int positionY) {
        this.type = type;
        this.packageName = packageName;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    // Добавляем новый конструктор с именем
    public DesktopItem(Type type, String packageName, String appName, int positionX, int positionY) {
        this.type = type;
        this.packageName = packageName;
        this.appName = appName;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    // Геттеры и сеттеры
    public Type getType() { return type; }
    public String getPackageName() { return packageName; }
    public int getPositionX() { return positionX; }
    public int getPositionY() { return positionY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getAppName() { return appName; }
    public boolean isDraggable() { return isDraggable; }

    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setPositionX(int x) { this.positionX = x; }
    public void setPositionY(int y) { this.positionY = y; }
    public void setAppName(String appName) { this.appName = appName; }
    public void setDraggable(boolean draggable) { isDraggable = draggable; }
}