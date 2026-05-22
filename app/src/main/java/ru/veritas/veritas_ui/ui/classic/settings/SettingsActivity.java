package ru.veritas.veritas_ui.ui.classic.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.DesktopSettingsManager;
import ru.veritas.veritas_ui.ui.classic.main.activity.MainActivity;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar rowsSeekBar;
    private SeekBar columnsSeekBar;
    private SeekBar pagesSeekBar;
    private SeekBar paddingSeekBar;

    private TextView rowsValue;
    private TextView columnsValue;
    private TextView pagesValue;
    private TextView paddingValue;

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

        // Загрузка сохранённых значений через существующие методы get...
        int savedRows = DesktopSettingsManager.getRows(this);
        int savedColumns = DesktopSettingsManager.getColumns(this);
        int savedPages = DesktopSettingsManager.getPages(this);
        int savedPadding = DesktopSettingsManager.getPadding(this);

        rowsSeekBar.setProgress(savedRows);
        columnsSeekBar.setProgress(savedColumns);
        pagesSeekBar.setProgress(savedPages);
        paddingSeekBar.setProgress(savedPadding);

        updateValueLabels();

        // Слушатели с сохранением и обновлением текста
        rowsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DesktopSettingsManager.saveRows(SettingsActivity.this, progress);
                rowsValue.setText(String.valueOf(progress));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        columnsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DesktopSettingsManager.saveColumns(SettingsActivity.this, progress);
                columnsValue.setText(String.valueOf(progress));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        pagesSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DesktopSettingsManager.savePages(SettingsActivity.this, progress);
                pagesValue.setText(String.valueOf(progress));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        paddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DesktopSettingsManager.savePadding(SettingsActivity.this, progress);
                paddingValue.setText(String.valueOf(progress));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Кнопка "Назад" – возврат на главный экран
        findViewById(R.id.button4).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Кнопка выбора обоев (можно оставить пустым или добавить логику)
        findViewById(R.id.selectWallpaperButton).setOnClickListener(v -> {
            // TODO: реализовать выбор обоев
        });
    }

    private void updateValueLabels() {
        rowsValue.setText(String.valueOf(rowsSeekBar.getProgress()));
        columnsValue.setText(String.valueOf(columnsSeekBar.getProgress()));
        pagesValue.setText(String.valueOf(pagesSeekBar.getProgress()));
        paddingValue.setText(String.valueOf(paddingSeekBar.getProgress()));
    }
}