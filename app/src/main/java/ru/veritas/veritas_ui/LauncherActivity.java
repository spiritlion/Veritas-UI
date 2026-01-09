package ru.veritas.veritas_ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import ru.veritas.veritas_ui.managers.main.app.AppsManager;
import ru.veritas.veritas_ui.managers.main.desktop.DesktopItem;
import ru.veritas.veritas_ui.managers.ui.desktop.DesktopManager;
import ru.veritas.veritas_ui.ui.ViewType;
import ru.veritas.veritas_ui.ui.classic.fragment.HomeDesktopFragment;
import ru.veritas.veritas_ui.ui.classic.fragment.AppListFragment;

/**
 * Главный класс лаунчера
 */
public class LauncherActivity extends AppCompatActivity implements AppListFragment.OnAppClickListener {

    private ViewPager2 viewPager;
    private AppsManager appsManager;
    private static final String TAG = "Veritas UI";
    private LauncherPagerAdapter adapter;

    // Флаг для отслеживания предзагрузки
    private boolean isAppListPreloaded = false;
    private HomeDesktopFragment homeDesktopFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        viewPager = findViewById(R.id.viewPager);
        appsManager = new AppsManager(this);

        // Устанавливаем вертикальную ориентацию
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        // Создаем и настраиваем адаптер
        // Получаем ссылку на фрагмент рабочего стола для обновления
        adapter = new LauncherPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Включаем пользовательскую прокрутку
        viewPager.setUserInputEnabled(true);

        // Предзагружаем данные для AppListFragment
        preloadAppListData();

        // Настраиваем переходы между страницами
        setupViewPager();
    }

    /**
     * Предзагрузка данных для списка приложений
     */
    private void preloadAppListData() {
        if (!isAppListPreloaded) {
            new Thread(() -> {
                // Синхронно загружаем приложения в фоне
                appsManager.loadUserAppsSync();
                isAppListPreloaded = true;
                Log.d(TAG, "Данные приложений предзагружены");
            }).start();
        }
    }

    /**
     * Добавляем приложение на рабочий стол из списка приложений
     */
    public void addAppToDesktop(String packageName) {
        // Простая реализация - добавляем на первую свободную позицию
        DesktopManager desktopManager = new DesktopManager(this);
        List<DesktopItem> items = desktopManager.getDesktopItems();

        // Находим свободную позицию (упрощенная логика)
        int nextPos = items.size();
        int gridX = nextPos % 4;
        int gridY = nextPos / 4;

        if (gridY < 6) { // Проверяем, чтобы не выйти за пределы сетки
            desktopManager.addAppToDesktop(packageName, gridX, gridY);

            // Обновляем рабочий стол
            if (homeDesktopFragment != null) {
                homeDesktopFragment.refreshDesktop();
            }
        }
    }

    private void setupViewPager() {
        // Добавляем обработчик перелистывания
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d(TAG, "Текущая страница: " + position);

                // При переходе на страницу приложений - предзагружаем данные если нужно
                if (position == 1 && !isAppListPreloaded) {
                    preloadAppListData();
                }
            }
        });
    }

    /**
     * Метод для переключения на определенную страницу
     */
    public void switchToPage(int pageIndex) {
        if (pageIndex >= 0 && pageIndex < viewPager.getAdapter().getItemCount()) {
            viewPager.setCurrentItem(pageIndex, true);
        }
    }

    public void switchToPage(ViewType viewType) {
        switch (viewType) {
            case Main:
                switchToPage(0);
            case AppList:
                switchToPage(1);
            case Settings:
                // TODO
        }
    }

    /**
     * Запуск приложения (реализация интерфейса из AppListFragment)
     */
    @Override
    public void onAppClick(String packageName) {
        appsManager.launchApp(this, packageName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении в лаунчер - предзагружаем данные
        if (viewPager.getCurrentItem() == 1) {
            preloadAppListData();
        }
    }

    /**
     * Кастомный адаптер для ViewPager с оптимизациями
     */
    private static class LauncherPagerAdapter extends FragmentStateAdapter {

        public LauncherPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new HomeDesktopFragment();
                case 1:
                    return new AppListFragment();
                default:
                    throw new IllegalArgumentException("Invalid position: " + position);
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean containsItem(long itemId) {
            return itemId == 0 || itemId == 1;
        }
    }
}