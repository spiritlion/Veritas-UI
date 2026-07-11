// AppsScreenViewModel.java
package ru.veritas.veritas_ui.ui.classic.apps;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.veritas.veritas_ui.core.command.CommandFactory;
import ru.veritas.veritas_ui.core.entities.AppShortcut;

public class AppsScreenViewModel extends ViewModel {

    private final MutableLiveData<AppsScreenState> state = new MutableLiveData<>();
    private final CommandFactory.UseCase useCaseFactory;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AppsScreenViewModel(CommandFactory.UseCase useCaseFactory) {
        this.useCaseFactory = useCaseFactory;
    }

    public LiveData<AppsScreenState> getState() {
        return state;
    }

    public void loadApps() {
        state.postValue(AppsScreenState.Loading.INSTANCE);
        executor.execute(() -> {
            try {
                List<AppShortcut> apps = useCaseFactory.getGetInstalledAppUseCase().invoke();
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
        useCaseFactory.getLaunchAppUseCase().invoke(packageName);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}