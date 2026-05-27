package ru.veritas.veritas_ui.core.repositories;

public interface SettingsRepository {
    void saveRows(int rows);
    int getRows();
    void saveColumns(int columns);
    int getColumns();
    void savePages(int pages);
    int getPages();
    void savePadding(int padding);
    int getPadding();
}
