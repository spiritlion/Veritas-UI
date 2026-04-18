package ru.veritas.veritas_ui.ui.classic.main.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.ui.classic.main.home.HomeScreenMode;
import ru.veritas.veritas_ui.ui.classic.main.home.HomeViewModel;
import ru.veritas.veritas_ui.ui.classic.main.home.HomeViewModelFactory;
import ru.veritas.veritas_ui.ui.classic.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private HomeViewModel homeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager.setAdapter(new MainPagerAdapter(this));
        viewPager.setUserInputEnabled(true);

        homeViewModel = new ViewModelProvider(this, new HomeViewModelFactory(this))
                .get(HomeViewModel.class);

        // Блокировка по режиму Edit
        homeViewModel.getMode().observe(this, mode -> {
            Boolean isMultiTouch = homeViewModel.getIsMultiTouch().getValue();
            if (isMultiTouch != null && isMultiTouch) return; // не мешаем мультитач
            viewPager.setUserInputEnabled(mode != HomeScreenMode.Edit);
        });

        // Блокировка по мультитач (приоритет выше)
        homeViewModel.getIsMultiTouch().observe(this, isMultiTouch -> {
            if (isMultiTouch) {
                viewPager.setUserInputEnabled(false);
            } else {
                // Восстанавливаем с учётом режима
                boolean enabled = homeViewModel.getMode().getValue() != HomeScreenMode.Edit;
                viewPager.setUserInputEnabled(enabled);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}