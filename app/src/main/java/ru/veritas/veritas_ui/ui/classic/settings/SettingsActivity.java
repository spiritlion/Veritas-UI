package ru.veritas.veritas_ui.ui.classic.settings; // или ui.classic.settings

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import ru.veritas.veritas_ui.R;

public class SettingsActivity extends AppCompatActivity {

    private SettingsViewModel viewModel;
    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activit_settings);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> {
            // Например, сохранить что-то
            viewModel.saveSetting("some value");
        });

        // Наблюдение за данными (пример)
        viewModel.getSomeSetting().observe(this, value -> {
            // обновить UI, если нужно
        });
    }
}