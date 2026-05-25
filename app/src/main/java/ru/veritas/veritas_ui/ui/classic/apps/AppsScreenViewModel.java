// AppsScreenViewModel.java
package ru.veritas.veritas_ui.ui.classic.apps;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.command.local.GetInstalledAppsUseCase;
import ru.veritas.veritas_ui.domain.command.local.LaunchAppUseCase;

public class AppsScreenViewModel extends ViewModel {

    private final MutableLiveData<AppsScreenState> state = new MutableLiveData<>();
    private final GetInstalledAppsUseCase getInstalledAppsUseCase;
    private final LaunchAppUseCase launchAppUseCase;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AppsScreenViewModel(GetInstalledAppsUseCase getInstalledAppsUseCase,
                               LaunchAppUseCase launchAppUseCase) {
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
                List<AppShortcut> apps = getInstalledAppsUseCase.invoke();
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