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
import ru.veritas.veritas_ui.core.exceptions.AppLaunchException;
import ru.veritas.veritas_ui.core.exceptions.AppUninstallException;
import ru.veritas.veritas_ui.ui.common.view.ToastData;

public class AppsScreenViewModel extends ViewModel {
    private final MutableLiveData<AppsScreenState> state = new MutableLiveData<>();
    private final CommandFactory.UseCase useCaseFactory;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<ToastData> toastMessage = new MutableLiveData<>();
    public LiveData<ToastData> getToastMessage() { return toastMessage; }

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
        try {
            useCaseFactory.getLaunchAppUseCase().invoke(packageName);
        } catch (AppLaunchException e) {
            // TODO: Add custom toast
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    public void openInfoApp(String packageName) {
        try {
            useCaseFactory.getOpenAppInfoUseCase().invoke(packageName);
        } catch (AppLaunchException e) {
            toastMessage.postValue(ToastData.error(e.getMessage()));
        }
    }

    public void uninstallApp(String packageName) {
        try {
            useCaseFactory.getUninstallAppUseCase().invoke(packageName);
        } catch (AppUninstallException e) {
            toastMessage.postValue(ToastData.error(e.getMessage()));
        }
    }
}