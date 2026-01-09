package ru.veritas.veritas_ui.managers.main.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppsManager {

    private Context context;
    private PackageManager packageManager;
    private String ownPackageName;

    private List<AppData> cachedApps;
    private boolean isCacheValid = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String TAG = "AppsManager";

    public AppsManager(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
        this.ownPackageName = context.getPackageName();
    }

    /**
     * Загружаем пользовательские приложения (асинхронно)
     */
    public void loadUserAppsAsync(AppLoadCallback callback) {
        if (isCacheValid && cachedApps != null) {
            callback.onAppsLoaded(cachedApps);
            return;
        }

        executorService.execute(() -> {
            List<AppData> apps = loadUserAppsSync();
            cachedApps = apps;
            isCacheValid = true;

            ((android.app.Activity) context).runOnUiThread(() -> {
                callback.onAppsLoaded(apps);
            });
        });
    }

    /**
     * Синхронная загрузка приложений
     */
    public List<AppData> loadUserAppsSync() {
        List<AppData> apps = new ArrayList<>();

        // Получаем ВСЕ установленные приложения
        List<ApplicationInfo> allApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : allApps) {
            try {
                // Пропускаем наше собственное приложение
                if (appInfo.packageName.equals(ownPackageName)) {
                    continue;
                }

                // Пропускаем системные приложения без пользовательского интерфейса
                if (!shouldShowApp(appInfo)) {
                    continue;
                }

                // Проверяем, есть ли у приложения лаунчер
                Intent launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName);
                if (launchIntent == null) {
                    continue; // Пропускаем приложения без лаунчера
                }

                // Добавляем приложение в список
                apps.add(new AppData(
                        appInfo.loadLabel(packageManager).toString(),
                        appInfo.loadIcon(packageManager),
                        appInfo.packageName,
                        appInfo.enabled
                ));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Сортируем по алфавиту
        apps.sort((a, b) -> a.getAppName().compareToIgnoreCase(b.getAppName()));

        return apps;
    }

    /**
     * Очистить кэш
     */
    public void invalidateCache() {
        isCacheValid = false;
        cachedApps = null;
    }

    public interface AppLoadCallback {
        void onAppsLoaded(List<AppData> apps);
    }
    /**
     * Определяем, нужно ли показывать приложение
     */
    private boolean shouldShowApp(ApplicationInfo appInfo) {
        String packageName = appInfo.packageName;

        // 1. Не показываем наше собственное приложение
        if (packageName.equals(ownPackageName)) {
            return false;
        }

        // 2. Проверяем, является ли приложение системным
        boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        boolean isUpdatedSystemApp = (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;

        // Если это системное приложение (не обновленное), проверяем, нужно ли его показывать
        if (isSystemApp && !isUpdatedSystemApp) {
            return shouldShowSystemApp(appInfo);
        }

        // 4. Исключаем определенные системные компоненты
        return !isExcludedSystemComponent(packageName);
    }

    /**
     * Определяем, какие системные приложения показывать
     */
    private boolean shouldShowSystemApp(ApplicationInfo appInfo) {
        String packageName = appInfo.packageName;

        // Список системных приложений, которые можно показывать
        Set<String> allowedSystemApps = new HashSet<>();
        allowedSystemApps.add("com.android.settings");      // Настройки
        allowedSystemApps.add("com.android.camera");        // Камера
        allowedSystemApps.add("com.android.camera2");       // Камера 2
        allowedSystemApps.add("com.android.contacts");      // Контакты
        allowedSystemApps.add("com.android.dialer");        // Телефон
        allowedSystemApps.add("com.android.messaging");     // Сообщения
        allowedSystemApps.add("com.android.chrome");        // Chrome
        allowedSystemApps.add("com.android.calculator2");   // Калькулятор
        allowedSystemApps.add("com.android.calendar");      // Календарь
        allowedSystemApps.add("com.android.email");         // Почта
        allowedSystemApps.add("com.android.gallery3d");     // Галерея
        allowedSystemApps.add("com.android.soundrecorder"); // Диктофон
        allowedSystemApps.add("com.google.android.gm");     // Gmail
        allowedSystemApps.add("com.google.android.apps.maps"); // Карты
        allowedSystemApps.add("com.google.android.youtube"); // YouTube
        allowedSystemApps.add("com.google.android.photos"); // Google Фото

        // Проверяем точное совпадение
        if (allowedSystemApps.contains(packageName)) {
            return true;
        }

        // Проверяем по префиксам для некоторых приложений
        for (String allowedApp : allowedSystemApps) {
            if (packageName.startsWith(allowedApp)) {
                return true;
            }
        }

        // Проверяем, установлено ли в /data/app (значит, пользовательское)
        if (appInfo.sourceDir != null && appInfo.sourceDir.startsWith("/data/")) {
            return true;
        }

        return false;
    }

    public Bitmap getAppIconBitmap(String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            Drawable drawable = pm.getApplicationIcon(appInfo);

            if (drawable != null) {
                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                return bitmap;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting app icon bitmap: " + e.getMessage());
        }

        return null;
    }

    /**
     * Проверяем, является ли приложение исключенным системным компонентом
     */
    private boolean isExcludedSystemComponent(String packageName) {
        // Список системных компонентов, которые НЕ нужно показывать
        String[] excludedComponents = {
                "com.android.systemui",
                "android",
                "com.google.android.setupwizard",
                "com.android.providers.",
                "com.android.inputmethod.",
                "com.android.keyguard",
                "com.android.managedprovisioning",
                "com.android.printspooler",
                "com.android.statementservice",
                "com.google.android.gsf",
                "com.google.android.gms",
                "com.google.android.partnersetup",
                "com.android.shell",
                "com.android.nfc",
                "com.android.bluetooth",
                "com.android.location.fused",
                "com.android.emergency",
                "com.android.backupconfirm",
                "com.android.certinstaller",
                "com.android.development",
                "com.android.dreams.basic",
                "com.android.dreams.phototable",
                "com.android.externalstorage",
                "com.android.htmlviewer",
                "com.android.defcontainer",
                "com.android.pacprocessor",
                "com.android.proxyhandler",
                "com.android.sharedstoragebackup",
                "com.android.smspush",
                "com.android.vpndialogs",
                "com.android.wallpapercropper",
                "com.android.phone",
                "com.android.server",
                "com.android.smspush",
                "com.android.stk",
                "com.android.managedprovisioning",
        };

        for (String excluded : excludedComponents) {
            if (packageName.startsWith(excluded)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Запуск приложения
     */
    public void launchApp(Context context, String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);

            if (appInfo.enabled) {
                Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Не удается запустить приложение", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Приложение отключено", Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context, "Приложение не найдено", Toast.LENGTH_SHORT).show();
        }
    }
}