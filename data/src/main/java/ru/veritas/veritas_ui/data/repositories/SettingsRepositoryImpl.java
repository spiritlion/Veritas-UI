package ru.veritas.veritas_ui.data.repositories;

import android.content.Context;
import android.content.SharedPreferences;

import ru.veritas.veritas_ui.core.repositories.SettingsRepository;

public class SettingsRepositoryImpl implements SettingsRepository {
    private final SharedPreferences prefs;

    public SettingsRepositoryImpl(Context context) {
        prefs = context.getSharedPreferences("desktop_settings", Context.MODE_PRIVATE);
    }

    @Override
    public void saveRows(int rows) {
        prefs.edit()
                .putInt("rows", rows)
                .apply();
    }

    @Override
    public int getRows() {
         return prefs.getInt("rows", 5);
    }

    @Override
    public void saveColumns(int columns) {
        prefs.edit()
                .putInt("columns", columns)
                .apply();
    }

    @Override
    public int getColumns() {
        return prefs.getInt("columns", 4);
    }

    @Override
    public void savePages(int pages) {
        prefs.edit()
                .putInt("pages", pages)
                .apply();
    }

    @Override
    public int getPages() {
        return prefs.getInt("pages",5);
    }

    @Override
    public void savePadding(int padding) {
        prefs.edit()
                .putInt("padding", padding)
                .apply();
    }

    @Override
    public int getPadding() {
        return prefs.getInt("padding",12);
    }
}
