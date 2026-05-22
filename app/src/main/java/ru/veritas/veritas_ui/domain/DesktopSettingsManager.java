package ru.veritas.veritas_ui.domain;


import android.content.Context;
import android.content.SharedPreferences;


public class DesktopSettingsManager {


    private static final String PREFS =
            "desktop_settings";


    // ROWS


    public static void saveRows(
            Context context,
            int rows
    ) {


        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );


        prefs.edit()
                .putInt("rows", rows)
                .apply();
    }


    public static int getRows(
            Context context
    ) {


        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );


        return prefs.getInt("rows", 5);
    }


    // COLUMNS


    public static void saveColumns(
            Context context,
            int columns
    ) {


        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );


        prefs.edit()
                .putInt("columns", columns)
                .apply();
    }


    public static int getColumns(
            Context context
    ) {


        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );


        return prefs.getInt("columns", 4);
    }


    // PAGES


    public static void savePages(
            Context context,
            int pages
    ) {


        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );


        prefs.edit()
                .putInt("pages", pages)
                .apply();
    }


    public static int getPages(
            Context context
    ) {


        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );


        return prefs.getInt("pages", 3);
    }


    // PADDING


    public static void savePadding(
            Context context,
            int padding
    ) {


        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );


        prefs.edit()
                .putInt("padding", padding)
                .apply();
    }


    public static int getPadding(
            Context context
    ) {


        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );


        return prefs.getInt("padding", 12);
    }
}
