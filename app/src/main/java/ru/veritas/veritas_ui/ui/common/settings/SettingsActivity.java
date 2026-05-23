// ui/classic/settings/SettingsActivity.java
package ru.veritas.veritas_ui.ui.common.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.ui.classic.activity.MainActivity;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar rowsSeekBar;
    private SeekBar columnsSeekBar;
    private SeekBar pagesSeekBar;
    private SeekBar paddingSeekBar;

    private TextView rowsValue;
    private TextView columnsValue;
    private TextView pagesValue;
    private TextView paddingValue;

    private SettingsViewModel viewModel;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Инициализация View
        rowsSeekBar = findViewById(R.id.rowsSeekBar);
        columnsSeekBar = findViewById(R.id.columnsSeekBar);
        pagesSeekBar = findViewById(R.id.pagesSeekBar);
        paddingSeekBar = findViewById(R.id.paddingSeekBar);

        rowsValue = findViewById(R.id.rowsValue);
        columnsValue = findViewById(R.id.columnsValue);
        pagesValue = findViewById(R.id.pagesValue);
        paddingValue = findViewById(R.id.paddingValue);

        // Создаём ViewModel через фабрику
        viewModel = new ViewModelProvider(this, new SettingsViewModelFactory(this))
                .get(SettingsViewModel.class);

        // Наблюдаем за изменениями настроек
        viewModel.getRows().observe(this, value -> {
            rowsSeekBar.setProgress(value);
            rowsValue.setText(String.valueOf(value));
        });
        viewModel.getColumns().observe(this, value -> {
            columnsSeekBar.setProgress(value);
            columnsValue.setText(String.valueOf(value));
        });
        viewModel.getPages().observe(this, value -> {
            pagesSeekBar.setProgress(value);
            pagesValue.setText(String.valueOf(value));
        });
        viewModel.getPadding().observe(this, value -> {
            paddingSeekBar.setProgress(value);
            paddingValue.setText(String.valueOf(value));
        });

        // Слушатели SeekBar
        rowsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) viewModel.setRows(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        columnsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) viewModel.setColumns(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        pagesSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) viewModel.setPages(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        paddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) viewModel.setPadding(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Кнопка "Назад"
        findViewById(R.id.button4).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Кнопка выбора обоев (можно оставить пустым)
        findViewById(R.id.selectWallpaperButton).setOnClickListener(v -> {
            // TODO: реализовать выбор обоев
        });
    }
}