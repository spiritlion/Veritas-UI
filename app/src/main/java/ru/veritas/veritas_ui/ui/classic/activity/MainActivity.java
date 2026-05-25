package ru.veritas.veritas_ui.ui.classic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import ru.veritas.veritas_ui.App;
import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.di.DependencyContainer;
import ru.veritas.veritas_ui.ui.classic.VeritasFragmentFactory;
import ru.veritas.veritas_ui.ui.common.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MainPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Устанавливаем фабрику ДО вызова super.onCreate()
        DependencyContainer container = ((App) getApplication()).getDependencyContainer();
        getSupportFragmentManager().setFragmentFactory(new VeritasFragmentFactory(container));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        pagerAdapter = new MainPagerAdapter(this, container);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setUserInputEnabled(true);
        viewPager.setNestedScrollingEnabled(true);
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

    public ViewPager2 getViewPager() {
        return viewPager;
    }
}