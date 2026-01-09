package ru.veritas.veritas_ui.ui.custom.fragment;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.LauncherActivity;
import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.managers.main.app.AppData;
import ru.veritas.veritas_ui.managers.main.app.AppsManager;

public class CustomAppListFragment extends Fragment {

    private static final String TAG = "CustomAppList";
    private WebView webView;
    private AppsManager appsManager;
    private List<AppData> apps = new ArrayList<>();
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_list_custom, container, false);

        webView = view.findViewById(R.id.webViewAppList);
        appsManager = new AppsManager(getContext());

        setupWebView();
        loadAppsAsync();

        return view;
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();

        // Включаем JavaScript
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        // Оптимизация производительности
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setLoadsImagesAutomatically(true);

        // Для корректного отображения на мобильных устройствах
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);

        // Настройки для разных версий Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        // Отключаем кэширование для упрощения (можно включить позже)
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        // Добавляем JavaScript интерфейс
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        // Настраиваем WebViewClient для обработки загрузки страницы
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(TAG, "Page started loading: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "Page finished loading: " + url);
                // После загрузки страницы загружаем приложения
                loadAppsIntoWebView();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e(TAG, "WebView error: " + error.getDescription());

                // Показываем fallback контент
                String html = "<html><body style='padding: 20px; text-align: center;'>" +
                        "<h3>Не удалось загрузить список приложений</h3>" +
                        "<p>Проверьте подключение к интернету</p>" +
                        "<button onclick='window.Android.reload()' style='padding: 10px 20px; background: #2196F3; color: white; border: none; border-radius: 5px;'>Повторить</button>" +
                        "</body></html>";
                webView.loadData(html, "text/html", "UTF-8");
            }
        });

        // Настраиваем WebChromeClient для прогресса
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                Log.d(TAG, "Loading progress: " + newProgress + "%");
            }
        });

        // Загружаем HTML из строки (вместо файла)
        loadHtmlContent();
    }

    private void loadHtmlContent() {
        // Загружаем HTML непосредственно из строки
        String html = getWebAppHtml();
        webView.loadDataWithBaseURL("file:///android_asset/custom/app_list", html, "text/html", "UTF-8", null);
    }

    private void loadAppsAsync() {
        if (isLoading) return;

        isLoading = true;
        appsManager.loadUserAppsAsync(new AppsManager.AppLoadCallback() {
            @Override
            public void onAppsLoaded(List<AppData> loadedApps) {
                apps = loadedApps;
                isLoading = false;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Обновляем WebView с новыми данными
                        loadAppsIntoWebView();
                    });
                }
            }
        });
    }

    private void loadAppsIntoWebView() {
        if (webView == null || apps.isEmpty()) {
            return;
        }

        try {
            // Создаем список приложений для передачи в JavaScript
            List<WebApp> webApps = new ArrayList<>();

            for (AppData app : apps) {
                WebApp webApp = new WebApp();
                webApp.appName = app.getAppName();
                webApp.packageName = app.getPackageName();
                webApp.enabled = app.isEnabled();

                // Определяем, является ли приложение системным
                webApp.isSystemApp = isSystemApp(app.getPackageName());

                webApps.add(webApp);
            }

            // Конвертируем в JSON
            Gson gson = new Gson();
            String jsonApps = gson.toJson(webApps);

            // Создаем системную информацию
            int enabledCount = 0;
            int disabledCount = 0;

            for (AppData app : apps) {
                if (app.isEnabled()) {
                    enabledCount++;
                } else {
                    disabledCount++;
                }
            }

            String systemInfo = "{" +
                    "\"totalApps\": " + apps.size() + "," +
                    "\"enabledApps\": " + enabledCount + "," +
                    "\"disabledApps\": " + disabledCount +
                    "}";

            // Вызываем JavaScript функцию для обновления списка
            String javascript = "javascript:(function() {" +
                    "try {" +
                    "  var apps = " + jsonApps + ";" +
                    "  var systemInfo = " + systemInfo + ";" +
                    "  console.log('Apps loaded:', apps.length);" +
                    "  if (typeof window.updateAppList === 'function') {" +
                    "    window.updateAppList(apps);" +
                    "  } else {" +
                    "    window.apps = apps;" +
                    "    window.systemInfo = systemInfo;" +
                    "    console.log('updateAppList not defined yet');" +
                    "  }" +
                    "} catch(e) {" +
                    "  console.error('Error in javascript:', e);" +
                    "}" +
                    "})()";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(javascript, null);
            } else {
                webView.loadUrl(javascript);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading apps into WebView: " + e.getMessage());
        }
    }

    private boolean isSystemApp(String packageName) {
        try {
            PackageManager pm = getContext().getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // Класс для передачи данных в JavaScript
    private static class WebApp {
        String appName;
        String packageName;
        boolean enabled;
        boolean isSystemApp;
    }

    // JavaScript интерфейс
    private class WebAppInterface {
        @JavascriptInterface
        public String getApps() {
            // Возвращаем приложения в формате JSON
            Gson gson = new Gson();

            List<WebApp> webApps = new ArrayList<>();
            for (AppData app : apps) {
                WebApp webApp = new WebApp();
                webApp.appName = app.getAppName();
                webApp.packageName = app.getPackageName();
                webApp.enabled = app.isEnabled();
                webApp.isSystemApp = isSystemApp(app.getPackageName());
                webApps.add(webApp);
            }

            return gson.toJson(webApps);
        }

        @JavascriptInterface
        public String getSystemInfo() {
            int enabledCount = 0;
            int disabledCount = 0;

            for (AppData app : apps) {
                if (app.isEnabled()) {
                    enabledCount++;
                } else {
                    disabledCount++;
                }
            }

            return "{" +
                    "\"totalApps\": " + apps.size() + "," +
                    "\"enabledApps\": " + enabledCount + "," +
                    "\"disabledApps\": " + disabledCount +
                    "}";
        }

        @JavascriptInterface
        public void launchApp(String packageName) {
            if (getActivity() instanceof LauncherActivity) {
                ((LauncherActivity) getActivity()).onAppClick(packageName);
            }
        }

        @JavascriptInterface
        public void goBack() {
            if (getActivity() instanceof LauncherActivity) {
                getActivity().runOnUiThread(() -> {
                    ((LauncherActivity) getActivity()).switchToPage(0); // Возврат на главный экран
                });
            }
        }

        @JavascriptInterface
        public void reload() {
            getActivity().runOnUiThread(() -> {
                webView.reload();
                loadAppsAsync();
            });
        }

        @JavascriptInterface
        public void showToast(String message) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
        // Обновляем список приложений при возвращении
        loadAppsAsync();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) {
            webView.destroy();
        }
    }

    private String getWebAppHtml() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"ru\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">\n" +
                "    <title>Приложения - Veritas UI</title>\n" +
                "    <style>\n" +
                "        * {\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            box-sizing: border-box;\n" +
                "            -webkit-tap-highlight-color: transparent;\n" +
                "        }\n" +
                "        \n" +
                "        :root {\n" +
                "            --primary-color: #2196F3;\n" +
                "            --background-color: #FFFFFF;\n" +
                "            --text-color: #333333;\n" +
                "            --border-color: #E0E0E0;\n" +
                "            --shadow-color: rgba(0, 0, 0, 0.1);\n" +
                "            --item-background: #FFFFFF;\n" +
                "            --header-background: #2196F3;\n" +
                "            --header-text: #FFFFFF;\n" +
                "        }\n" +
                "        \n" +
                "        body {\n" +
                "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;\n" +
                "            background-color: var(--background-color);\n" +
                "            color: var(--text-color);\n" +
                "            overflow-x: hidden;\n" +
                "            touch-action: pan-y;\n" +
                "        }\n" +
                "        \n" +
                "        .header {\n" +
                "            background-color: var(--header-background);\n" +
                "            color: var(--header-text);\n" +
                "            padding: 16px;\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "            position: sticky;\n" +
                "            top: 0;\n" +
                "            z-index: 100;\n" +
                "            box-shadow: 0 2px 4px var(--shadow-color);\n" +
                "        }\n" +
                "        \n" +
                "        .back-button {\n" +
                "            background: none;\n" +
                "            border: none;\n" +
                "            color: var(--header-text);\n" +
                "            font-size: 16px;\n" +
                "            padding: 8px;\n" +
                "            margin-right: 16px;\n" +
                "            cursor: pointer;\n" +
                "            border-radius: 4px;\n" +
                "            transition: background-color 0.2s;\n" +
                "        }\n" +
                "        \n" +
                "        .title {\n" +
                "            font-size: 20px;\n" +
                "            font-weight: 500;\n" +
                "            flex-grow: 1;\n" +
                "        }\n" +
                "        \n" +
                "        .search-container {\n" +
                "            padding: 12px 16px;\n" +
                "            background-color: var(--background-color);\n" +
                "            border-bottom: 1px solid var(--border-color);\n" +
                "        }\n" +
                "        \n" +
                "        .search-input {\n" +
                "            width: 100%;\n" +
                "            padding: 12px 16px;\n" +
                "            border: 2px solid var(--border-color);\n" +
                "            border-radius: 24px;\n" +
                "            font-size: 16px;\n" +
                "            outline: none;\n" +
                "            transition: border-color 0.2s;\n" +
                "        }\n" +
                "        \n" +
                "        .search-input:focus {\n" +
                "            border-color: var(--primary-color);\n" +
                "        }\n" +
                "        \n" +
                "        .apps-grid {\n" +
                "            display: grid;\n" +
                "            grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));\n" +
                "            gap: 12px;\n" +
                "            padding: 16px;\n" +
                "            max-width: 800px;\n" +
                "            margin: 0 auto;\n" +
                "        }\n" +
                "        \n" +
                "        .app-item {\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "            align-items: center;\n" +
                "            padding: 12px 8px;\n" +
                "            background-color: var(--item-background);\n" +
                "            border-radius: 12px;\n" +
                "            cursor: pointer;\n" +
                "            transition: all 0.2s;\n" +
                "            text-decoration: none;\n" +
                "            color: var(--text-color);\n" +
                "            border: 1px solid transparent;\n" +
                "            user-select: none;\n" +
                "        }\n" +
                "        \n" +
                "        .app-item:hover {\n" +
                "            background-color: #F5F5F5;\n" +
                "            transform: translateY(-2px);\n" +
                "        }\n" +
                "        \n" +
                "        .app-item:active {\n" +
                "            background-color: #EEEEEE;\n" +
                "            transform: translateY(0);\n" +
                "        }\n" +
                "        \n" +
                "        .app-icon {\n" +
                "            width: 48px;\n" +
                "            height: 48px;\n" +
                "            border-radius: 12px;\n" +
                "            margin-bottom: 8px;\n" +
                "            object-fit: cover;\n" +
                "            box-shadow: 0 2px 4px var(--shadow-color);\n" +
                "        }\n" +
                "        \n" +
                "        .app-name {\n" +
                "            font-size: 12px;\n" +
                "            text-align: center;\n" +
                "            line-height: 1.2;\n" +
                "            max-height: 28px;\n" +
                "            overflow: hidden;\n" +
                "            display: -webkit-box;\n" +
                "            -webkit-line-clamp: 2;\n" +
                "            -webkit-box-orient: vertical;\n" +
                "            word-break: break-word;\n" +
                "        }\n" +
                "        \n" +
                "        .app-package {\n" +
                "            display: none;\n" +
                "        }\n" +
                "        \n" +
                "        .loading-container {\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            height: 200px;\n" +
                "            flex-direction: column;\n" +
                "        }\n" +
                "        \n" +
                "        .loading-spinner {\n" +
                "            width: 40px;\n" +
                "            height: 40px;\n" +
                "            border: 3px solid var(--border-color);\n" +
                "            border-top-color: var(--primary-color);\n" +
                "            border-radius: 50%;\n" +
                "            animation: spin 1s linear infinite;\n" +
                "            margin-bottom: 16px;\n" +
                "        }\n" +
                "        \n" +
                "        .loading-text {\n" +
                "            color: #666;\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "        \n" +
                "        .empty-state {\n" +
                "            text-align: center;\n" +
                "            padding: 48px 16px;\n" +
                "            color: #666;\n" +
                "        }\n" +
                "        \n" +
                "        .empty-state-icon {\n" +
                "            font-size: 48px;\n" +
                "            margin-bottom: 16px;\n" +
                "        }\n" +
                "        \n" +
                "        .empty-state-text {\n" +
                "            font-size: 16px;\n" +
                "            margin-bottom: 8px;\n" +
                "        }\n" +
                "        \n" +
                "        .empty-state-subtext {\n" +
                "            font-size: 14px;\n" +
                "            color: #999;\n" +
                "        }\n" +
                "        \n" +
                "        .filters {\n" +
                "            display: flex;\n" +
                "            overflow-x: auto;\n" +
                "            padding: 8px 16px;\n" +
                "            background-color: var(--background-color);\n" +
                "            border-bottom: 1px solid var(--border-color);\n" +
                "            gap: 8px;\n" +
                "            -webkit-overflow-scrolling: touch;\n" +
                "        }\n" +
                "        \n" +
                "        .filter-button {\n" +
                "            padding: 8px 16px;\n" +
                "            border: 1px solid var(--border-color);\n" +
                "            background-color: var(--background-color);\n" +
                "            border-radius: 20px;\n" +
                "            font-size: 14px;\n" +
                "            white-space: nowrap;\n" +
                "            cursor: pointer;\n" +
                "            transition: all 0.2s;\n" +
                "        }\n" +
                "        \n" +
                "        .filter-button.active {\n" +
                "            background-color: var(--primary-color);\n" +
                "            color: white;\n" +
                "            border-color: var(--primary-color);\n" +
                "        }\n" +
                "        \n" +
                "        @keyframes spin {\n" +
                "            to { transform: rotate(360deg); }\n" +
                "        }\n" +
                "        \n" +
                "        @media (max-width: 600px) {\n" +
                "            .apps-grid {\n" +
                "                grid-template-columns: repeat(auto-fill, minmax(72px, 1fr));\n" +
                "                gap: 10px;\n" +
                "                padding: 12px;\n" +
                "            }\n" +
                "            \n" +
                "            .app-icon {\n" +
                "                width: 40px;\n" +
                "                height: 40px;\n" +
                "            }\n" +
                "            \n" +
                "            .app-name {\n" +
                "                font-size: 11px;\n" +
                "            }\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"header\">\n" +
                "        <button class=\"back-button\" onclick=\"Android.goBack()\">← Назад</button>\n" +
                "        <div class=\"title\">Все приложения</div>\n" +
                "    </div>\n" +
                "    \n" +
                "    <div class=\"search-container\">\n" +
                "        <input \n" +
                "            type=\"text\" \n" +
                "            class=\"search-input\" \n" +
                "            placeholder=\"Поиск приложений...\" \n" +
                "            oninput=\"filterApps()\"\n" +
                "            id=\"searchInput\"\n" +
                "        >\n" +
                "    </div>\n" +
                "    \n" +
                "    <div class=\"filters\" id=\"filters\">\n" +
                "        <button class=\"filter-button active\" onclick=\"setFilter('all')\">Все</button>\n" +
                "        <button class=\"filter-button\" onclick=\"setFilter('enabled')\">Включенные</button>\n" +
                "        <button class=\"filter-button\" onclick=\"setFilter('disabled')\">Отключенные</button>\n" +
                "    </div>\n" +
                "    \n" +
                "    <div class=\"apps-grid\" id=\"appsGrid\">\n" +
                "        <!-- Приложения будут загружены здесь -->\n" +
                "    </div>\n" +
                "    \n" +
                "    <div id=\"loading\" class=\"loading-container\">\n" +
                "        <div class=\"loading-spinner\"></div>\n" +
                "        <div class=\"loading-text\">Загрузка приложений...</div>\n" +
                "    </div>\n" +
                "    \n" +
                "    <div id=\"emptyState\" class=\"empty-state\" style=\"display: none;\">\n" +
                "        <div class=\"empty-state-icon\">📱</div>\n" +
                "        <div class=\"empty-state-text\">Приложения не найдены</div>\n" +
                "        <div class=\"empty-state-subtext\">Попробуйте изменить поисковый запрос</div>\n" +
                "    </div>\n" +
                "    \n" +
                "    <script>\n" +
                "        // Глобальная переменная для хранения всех приложений\n" +
                "        let allApps = [];\n" +
                "        let currentFilter = 'all';\n" +
                "        let currentSearch = '';\n" +
                "        \n" +
                "        // Функция для возврата назад\n" +
                "        function goBack() {\n" +
                "            if (window.Android && window.Android.goBack) {\n" +
                "                Android.goBack();\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        // Функция для фильтрации приложений\n" +
                "        function filterApps() {\n" +
                "            const searchInput = document.getElementById('searchInput');\n" +
                "            currentSearch = searchInput.value.toLowerCase().trim();\n" +
                "            renderApps();\n" +
                "        }\n" +
                "        \n" +
                "        // Функция для установки фильтра\n" +
                "        function setFilter(filter) {\n" +
                "            currentFilter = filter;\n" +
                "            \n" +
                "            // Обновляем активную кнопку фильтра\n" +
                "            document.querySelectorAll('.filter-button').forEach(btn => {\n" +
                "                btn.classList.remove('active');\n" +
                "            });\n" +
                "            event.target.classList.add('active');\n" +
                "            \n" +
                "            renderApps();\n" +
                "        }\n" +
                "        \n" +
                "        // Функция для рендеринга приложений\n" +
                "        function renderApps() {\n" +
                "            const appsGrid = document.getElementById('appsGrid');\n" +
                "            const loadingElement = document.getElementById('loading');\n" +
                "            const emptyStateElement = document.getElementById('emptyState');\n" +
                "            \n" +
                "            // Показываем загрузку\n" +
                "            loadingElement.style.display = 'flex';\n" +
                "            emptyStateElement.style.display = 'none';\n" +
                "            appsGrid.innerHTML = '';\n" +
                "            \n" +
                "            // Фильтруем приложения\n" +
                "            const filteredApps = allApps.filter(app => {\n" +
                "                // Поиск по названию\n" +
                "                const matchesSearch = currentSearch === '' || \n" +
                "                    app.appName.toLowerCase().includes(currentSearch) ||\n" +
                "                    app.packageName.toLowerCase().includes(currentSearch);\n" +
                "                \n" +
                "                // Фильтр по состоянию\n" +
                "                let matchesFilter = true;\n" +
                "                switch (currentFilter) {\n" +
                "                    case 'enabled':\n" +
                "                        matchesFilter = app.enabled === true;\n" +
                "                        break;\n" +
                "                    case 'disabled':\n" +
                "                        matchesFilter = app.enabled === false;\n" +
                "                        break;\n" +
                "                    case 'all':\n" +
                "                    default:\n" +
                "                        matchesFilter = true;\n" +
                "                }\n" +
                "                \n" +
                "                return matchesSearch && matchesFilter;\n" +
                "            });\n" +
                "            \n" +
                "            // Имитируем задержку для плавности\n" +
                "            setTimeout(() => {\n" +
                "                loadingElement.style.display = 'none';\n" +
                "                \n" +
                "                if (filteredApps.length === 0) {\n" +
                "                    emptyStateElement.style.display = 'block';\n" +
                "                    return;\n" +
                "                }\n" +
                "                \n" +
                "                // Сортируем приложения по алфавиту\n" +
                "                filteredApps.sort((a, b) => a.appName.localeCompare(b.appName));\n" +
                "                \n" +
                "                // Рендерим приложения\n" +
                "                filteredApps.forEach(app => {\n" +
                "                    const appElement = document.createElement('div');\n" +
                "                    appElement.className = 'app-item';\n" +
                "                    appElement.onclick = () => {\n" +
                "                        if (app.enabled) {\n" +
                "                            Android.launchApp(app.packageName);\n" +
                "                        } else {\n" +
                "                            Android.showToast('Приложение отключено и не может быть запущено');\n" +
                "                        }\n" +
                "                    };\n" +
                "                    \n" +
                "                    // Создаем элемент иконки\n" +
                "                    const iconElement = document.createElement('div');\n" +
                "                    iconElement.className = 'app-icon';\n" +
                "                    iconElement.style.backgroundColor = app.enabled ? '#2196F3' : '#BDBDBD';\n" +
                "                    iconElement.style.display = 'flex';\n" +
                "                    iconElement.style.alignItems = 'center';\n" +
                "                    iconElement.style.justifyContent = 'center';\n" +
                "                    iconElement.style.color = 'white';\n" +
                "                    iconElement.style.fontWeight = 'bold';\n" +
                "                    iconElement.textContent = app.appName.charAt(0).toUpperCase();\n" +
                "                    \n" +
                "                    // Если приложение отключено, делаем иконку серой\n" +
                "                    if (!app.enabled) {\n" +
                "                        iconElement.style.opacity = '0.6';\n" +
                "                    }\n" +
                "                    \n" +
                "                    // Создаем элемент названия\n" +
                "                    const nameElement = document.createElement('div');\n" +
                "                    nameElement.className = 'app-name';\n" +
                "                    nameElement.textContent = app.appName;\n" +
                "                    \n" +
                "                    appElement.appendChild(iconElement);\n" +
                "                    appElement.appendChild(nameElement);\n" +
                "                    \n" +
                "                    appsGrid.appendChild(appElement);\n" +
                "                });\n" +
                "                \n" +
                "                // Обновляем статистику в заголовке\n" +
                "                updateStats(filteredApps.length);\n" +
                "            }, 100);\n" +
                "        }\n" +
                "        \n" +
                "        // Функция для обновления статистики\n" +
                "        function updateStats(count) {\n" +
                "            const titleElement = document.querySelector('.title');\n" +
                "            const totalApps = allApps.length;\n" +
                "            titleElement.textContent = 'Приложения (' + count + '/' + totalApps + ')';\n" +
                "        }\n" +
                "        \n" +
                "        // Функция для загрузки приложений\n" +
                "        function loadApps() {\n" +
                "            // Получаем приложения из Android\n" +
                "            try {\n" +
                "                if (window.Android && Android.getApps) {\n" +
                "                    allApps = JSON.parse(Android.getApps());\n" +
                "                    console.log('Loaded ' + allApps.length + ' apps');\n" +
                "                    renderApps();\n" +
                "                    updateFilterCounts();\n" +
                "                }\n" +
                "            } catch (e) {\n" +
                "                console.error('Error loading apps:', e);\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        // Функция для обновления счетчиков в фильтрах\n" +
                "        function updateFilterCounts() {\n" +
                "            const enabledCount = allApps.filter(app => app.enabled).length;\n" +
                "            const disabledCount = allApps.filter(app => !app.enabled).length;\n" +
                "            \n" +
                "            const filterButtons = document.querySelectorAll('.filter-button');\n" +
                "            filterButtons.forEach(btn => {\n" +
                "                const text = btn.textContent;\n" +
                "                if (text.includes('Включенные')) {\n" +
                "                    btn.textContent = 'Включенные (' + enabledCount + ')';\n" +
                "                } else if (text.includes('Отключенные')) {\n" +
                "                    btn.textContent = 'Отключенные (' + disabledCount + ')';\n" +
                "                }\n" +
                "            });\n" +
                "        }\n" +
                "        \n" +
                "        // Функция для обновления списка приложений извне\n" +
                "        window.updateAppList = function(apps) {\n" +
                "            allApps = apps;\n" +
                "            console.log('Updated app list:', allApps.length);\n" +
                "            renderApps();\n" +
                "            updateFilterCounts();\n" +
                "        };\n" +
                "        \n" +
                "        // Инициализация при загрузке страницы\n" +
                "        document.addEventListener('DOMContentLoaded', () => {\n" +
                "            console.log('DOM loaded');\n" +
                "            loadApps();\n" +
                "            \n" +
                "            // Фокус на поле поиска при загрузке\n" +
                "            setTimeout(() => {\n" +
                "                const searchInput = document.getElementById('searchInput');\n" +
                "                if (searchInput) {\n" +
                "                    searchInput.focus();\n" +
                "                }\n" +
                "            }, 300);\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}