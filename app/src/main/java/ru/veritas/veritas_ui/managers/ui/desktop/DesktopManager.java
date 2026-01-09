// [file name]: DesktopManager.java
package ru.veritas.veritas_ui.managers.ui.desktop;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import ru.veritas.veritas_ui.managers.main.app.AppData;
import ru.veritas.veritas_ui.managers.main.app.AppsManager;
import ru.veritas.veritas_ui.managers.main.desktop.DesktopItem;

public class DesktopManager {
    private static final String PREF_NAME = "desktop_config";
    private static final String KEY_ITEMS = "desktop_items";

    private Context context;
    private SharedPreferences prefs;
    private Gson gson;

    public DesktopManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Получаем все элементы рабочего стола
     */
    public List<DesktopItem> getDesktopItems() {
        String json = prefs.getString(KEY_ITEMS, "[]");
        Type type = new TypeToken<ArrayList<DesktopItem>>(){}.getType();
        List<DesktopItem> items = gson.fromJson(json, type);

        // Если элементов нет, создаем начальные
        if (items == null || items.isEmpty()) {
            items = createDefaultDesktopItems();
            saveDesktopItems(items);
        }

        return items;
    }

    /**
     * Сохраняем элементы рабочего стола
     */
    public void saveDesktopItems(List<DesktopItem> items) {
        String json = gson.toJson(items);
        prefs.edit().putString(KEY_ITEMS, json).apply();
    }

    /**
     * Создаем дефолтные элементы (можно будет настроить позже)
     */
    private List<DesktopItem> createDefaultDesktopItems() {
        List<DesktopItem> items = new ArrayList<>();

        // Пример: добавляем несколько дефолтных приложений
        // В реальном приложении можно сделать настройку или определять популярные приложения

        return items;
    }

    /**
     * Добавляем приложение на рабочий стол
     */
    public void addAppToDesktop(String packageName, int gridX, int gridY) {
        List<DesktopItem> items = getDesktopItems();
        items.add(new DesktopItem(DesktopItem.Type.APP, packageName, gridX, gridY));
        saveDesktopItems(items);
    }

    /**
     * Удаляем элемент с рабочего стола
     */
    public void removeFromDesktop(int positionX, int positionY) {
        List<DesktopItem> items = getDesktopItems();
        for (int i = items.size() - 1; i >= 0; i--) {
            DesktopItem item = items.get(i);
            if (item.getPositionX() == positionX && item.getPositionY() == positionY) {
                items.remove(i);
            }
        }
        saveDesktopItems(items);
    }

    /**
     * Получаем данные приложений для элементов на рабочем столе
     */
    public List<AppData> getDesktopApps(List<DesktopItem> desktopItems) {
        List<AppData> desktopApps = new ArrayList<>();
        AppsManager appsManager = new AppsManager(context);
        List<AppData> allApps = appsManager.loadUserAppsSync();

        for (DesktopItem item : desktopItems) {
            if (item.getType() == DesktopItem.Type.APP) {
                for (AppData app : allApps) {
                    if (app.getPackageName().equals(item.getPackageName())) {
                        desktopApps.add(app);
                        break;
                    }
                }
            }
        }

        return desktopApps;
    }
}