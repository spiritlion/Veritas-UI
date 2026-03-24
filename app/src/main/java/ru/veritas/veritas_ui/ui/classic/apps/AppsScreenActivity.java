package ru.veritas.veritas_ui.ui.classic.apps;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppInfoEntity;
import ru.veritas.veritas_ui.ui.classic.settings.SettingsActivity;

public class AppsScreenActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppsAdapter adapter;
    private ProgressBar progressIndicator;
    private AppsScreenViewModel viewModel;

    // Элементы для ошибки
    private TextView errorText;
    private Button errorButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_screen);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        progressIndicator = findViewById(R.id.progressIndicator);

        // Инициализация элементов ошибки
        errorText = findViewById(R.id.error_text);
        errorButton = findViewById(R.id.error_button);

        setupRecyclerView();

        viewModel = new ViewModelProvider(this, new AppsScreenViewModelFactory(this))
                .get(AppsScreenViewModel.class);

        // Наблюдаем за единым состоянием
        viewModel.getState().observe(this, new Observer<AppsScreenState>() {
            @Override
            public void onChanged(AppsScreenState state) {
                if (state instanceof AppsScreenState.Loading) {
                    // Показываем загрузку, скрываем всё остальное
                    progressIndicator.setVisibility(VISIBLE);
                    recyclerView.setVisibility(GONE);
                    hideError();
                } else if (state instanceof AppsScreenState.Content) {
                    // Показываем список приложений
                    progressIndicator.setVisibility(INVISIBLE);
                    recyclerView.setVisibility(VISIBLE);
                    hideError();

                    List<AppInfoEntity> apps = ((AppsScreenState.Content) state).getApps();
                    adapter.setApps(apps);
                } else if (state instanceof AppsScreenState.Error) {
                    // Показываем ошибку
                    progressIndicator.setVisibility(INVISIBLE);
                    recyclerView.setVisibility(GONE);

                    AppsScreenState.Error errorState = (AppsScreenState.Error) state;
                    showError(errorState.getMessage(), errorState.getRetryAction());
                }
            }
        });

        viewModel.loadApps();
    }

    private void setupRecyclerView() {
        adapter = new AppsAdapter(app -> viewModel.launchApp(app.getPackageName()));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Отображает сообщение об ошибке и кнопку для повторной попытки.
     * @param message текст ошибки
     * @param retryAction действие при нажатии на кнопку
     */
    private void showError(String message, Runnable retryAction) {
        errorText.setVisibility(VISIBLE);
        errorText.setText(message != null ? message : "Произошла ошибка");
        errorButton.setVisibility(VISIBLE);
        errorButton.setText("Повторить");
        errorButton.setOnClickListener(v -> retryAction.run());
    }

    /**
     * Скрывает элементы отображения ошибки.
     */
    private void hideError() {
        errorText.setVisibility(GONE);
        errorButton.setVisibility(GONE);
        errorButton.setOnClickListener(null); // очищаем слушатель
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