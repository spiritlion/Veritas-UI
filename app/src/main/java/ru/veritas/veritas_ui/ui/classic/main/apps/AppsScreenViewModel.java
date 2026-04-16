// AppsScreenViewModel.java
package ru.veritas.veritas_ui.ui.classic.main.apps;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.GetInstalledAppsUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;

public class AppsScreenViewModel extends AndroidViewModel {

    private final MutableLiveData<AppsScreenState> state = new MutableLiveData<>();
    private final GetInstalledAppsUseCase getInstalledAppsUseCase;
    private final LaunchAppUseCase launchAppUseCase;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AppsScreenViewModel(@NonNull Application application,
                               GetInstalledAppsUseCase getInstalledAppsUseCase,
                               LaunchAppUseCase launchAppUseCase) {
        super(application);
        this.getInstalledAppsUseCase = getInstalledAppsUseCase;
        this.launchAppUseCase = launchAppUseCase;
    }

    public LiveData<AppsScreenState> getState() {
        return state;
    }

    public void loadApps() {
        state.postValue(AppsScreenState.Loading.INSTANCE);
        executor.execute(() -> {
            try {
                List<AppShortcutDTO> apps = getInstalledAppsUseCase.invoke();
                state.postValue(new AppsScreenState.Content(apps));
            } catch (Exception e) {
                state.postValue(new AppsScreenState.Error(
                        "Ошибка загрузки приложений: " + e.getMessage(),
                        this::loadApps, // повторная попытка
                        "Повторить попытку"
                ));
            }
        });
    }

    public void launchApp(String packageName) {
        launchAppUseCase.invoke(packageName);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}