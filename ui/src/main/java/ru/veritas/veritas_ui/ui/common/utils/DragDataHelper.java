// ui/common/utils/DragDataHelper.java
package ru.veritas.veritas_ui.ui.common.utils;

import android.content.ClipData;

import ru.veritas.veritas_ui.core.entities.DragEventData;

public class DragDataHelper {
    private static final String MIME_TYPE = "text/plain";

    public static ClipData createAppDragData(String packageName, String appName) {
        String data = "app:" + packageName + ":" + appName;
        ClipData.Item item = new ClipData.Item(data);
        return new ClipData("app_shortcut", new String[]{MIME_TYPE}, item);
    }

    public static ClipData createHomeShortcutDragData(int page, int row, int col) {
        String data = "home:" + page + ":" + row + ":" + col;
        ClipData.Item item = new ClipData.Item(data);
        return new ClipData("home_shortcut", new String[]{MIME_TYPE}, item);
    }

    public static ClipData createFavoriteDragData(int page, int position) {
        String data = "fav:" + page + ":" + position;
        ClipData.Item item = new ClipData.Item(data);
        return new ClipData("favorite", new String[]{MIME_TYPE}, item);
    }

    public static DragEventData parse(ClipData clipData) {
        if (clipData == null || clipData.getItemCount() == 0) return null;
        String text = clipData.getItemAt(0).getText().toString();
        String[] parts = text.split(":");
        if (parts.length < 2) return null;

        switch (parts[0]) {
            case "app":
                if (parts.length >= 3)
                    return new DragEventData(parts[1], parts[2]);
                break;
            case "home":
                if (parts.length >= 4)
                    return new DragEventData(Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                break;
            case "fav":
                if (parts.length >= 3)
                    return new DragEventData(Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]));
                break;
        }
        return null;
    }
}