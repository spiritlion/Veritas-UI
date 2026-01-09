// [file name]: DesktopItem.java
package ru.veritas.veritas_ui.managers.main.desktop;

public class DesktopItem {
    public enum Type {
        APP,
        WIDGET
    }

    private Type type;
    private String packageName; // для приложений
    private int widgetId; // для виджетов
    private int positionX;
    private int positionY;
    private int width = 1; // в ячейках
    private int height = 1; // в ячейках

    // Конструкторы, геттеры и сеттеры
    public DesktopItem(Type type, String packageName, int positionX, int positionY) {
        this.type = type;
        this.packageName = packageName;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public Type getType() { return type; }
    public String getPackageName() { return packageName; }
    public int getPositionX() { return positionX; }
    public int getPositionY() { return positionY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
}