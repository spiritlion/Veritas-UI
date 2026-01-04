package ru.veritas.veritas_ui;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import ru.veritas.veritas_ui.managers.main.app.AppsManager;
import ru.veritas.veritas_ui.managers.ui.AnimationManager;
import ru.veritas.veritas_ui.ui.fragment.MainFragment;
import ru.veritas.veritas_ui.ui.fragment.AppListFragment;
import ru.veritas.veritas_ui.ui.ViewPagerAdapter;

/**
 * Главный класс лаунчера
 */
public class LauncherActivity extends AppCompatActivity implements AppListFragment.OnAppClickListener {

    private ViewPager2 viewPager;
    private AppsManager appsManager;
    private static final String TAG = "Veritas UI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        viewPager = findViewById(R.id.viewPager);
        appsManager = new AppsManager(this);

        // Устанавливаем вертикальную ориентацию
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        // Создаем фрагменты для ViewPager
        Fragment[] fragments = new Fragment[] {
                new MainFragment(),
                new AppListFragment()
        };

        // Настраиваем адаптер
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, fragments);
        viewPager.setAdapter(adapter);

        // Можно включить/выключить пользовательскую прокрутку
        viewPager.setUserInputEnabled(true); // true - разрешить свайп, false - только кнопками

        // Настраиваем переходы между страницами
        setupViewPager();
    }

    private void setupViewPager() {
        // Добавляем обработчик перелистывания
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Можно добавить логику при смене страницы
                Log.d(TAG, "Текущая страница: " + position);

                // Если нужно обновлять список приложений при переходе на страницу
                if (position == 1) {
                    refreshAppList();
                }
            }
        });

        // Настраиваем анимации (опционально)
//         viewPager.setPageTransformer(new AnimationManager.VerticalDepthPageTransformer()); !!!ОПАСНО РАСКОМИЧИВАТЬ, ПОКА АНИМАЦИЯ НЕ НАСТРОЕНА ПРАВИЛЬНО!!!
    }

    /**
     * Метод для переключения на определенную страницу
     */
    public void switchToPage(int pageIndex) { // TODO поменять методику смена view
        if (pageIndex >= 0 && pageIndex < viewPager.getAdapter().getItemCount()) {
            viewPager.setCurrentItem(pageIndex, true); // true для анимации
        }
    }

    /**
     * Запуск приложения (реализация интерфейса из AppListFragment)
     */
    @Override
    public void onAppClick(String packageName) {
        appsManager.launchApp(this, packageName);
    }

    /**
     * Получить текущую страницу
     */
    public int getCurrentPage() {
        return viewPager.getCurrentItem();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем список приложений при возвращении, если открыта страница с приложениями
        if (viewPager.getCurrentItem() == 1) {
            refreshAppList();
        }
    }

    private void refreshAppList() {
        // Обновляем список приложений в AppListFragment
        // Нужно передать команду фрагменту
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentByTag("f" + viewPager.getCurrentItem());

        if (currentFragment instanceof AppListFragment) {
            ((AppListFragment) currentFragment).refreshAppList();
        }
    }
}