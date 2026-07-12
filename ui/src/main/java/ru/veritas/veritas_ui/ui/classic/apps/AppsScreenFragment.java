package ru.veritas.veritas_ui.ui.classic.apps;

import android.content.ClipData;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import ru.veritas.veritas_ui.core.command.CommandFactory;
import ru.veritas.veritas_ui.core.command.local.GetInstalledAppsUseCase;
import ru.veritas.veritas_ui.core.command.local.LaunchAppUseCase;
import ru.veritas.veritas_ui.ui.R;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.command.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.ui.common.ViewPagerHost;
import ru.veritas.veritas_ui.ui.common.utils.DragDataHelper;

public class AppsScreenFragment extends Fragment implements AppsAdapter.DragStartListener  {
    private RecyclerView recyclerView;
    private AppsAdapter adapter;
    private ProgressBar progressIndicator;
    private AppsScreenViewModel viewModel;
    private TextView errorText;
    private Button errorButton;
    private final CommandFactory.UseCase useCaseFactory;

    public AppsScreenFragment(CommandFactory.UseCase useCaseFactory) {
        this.useCaseFactory = useCaseFactory;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apps_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressIndicator = view.findViewById(R.id.progressIndicatorApps);
        errorText = view.findViewById(R.id.error_text_apps);
        errorButton = view.findViewById(R.id.error_button_apps);

        setupRecyclerView();

        viewModel = new ViewModelProvider(
                requireActivity(),
                new AppsScreenViewModelFactory(useCaseFactory)
        ).get(AppsScreenViewModel.class);

        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof AppsScreenState.Loading) {
                progressIndicator.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                hideError();
            } else if (state instanceof AppsScreenState.Content) {
                progressIndicator.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                hideError();
                List<AppShortcut> apps = ((AppsScreenState.Content) state).getApps();
                adapter.setApps(apps);
            } else if (state instanceof AppsScreenState.Error) {
                progressIndicator.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.GONE);
                AppsScreenState.Error errorState = (AppsScreenState.Error) state;
                showError(errorState.getMessage(), errorState.getRetryAction());
            }
        });

        viewModel.loadApps();


    }

    // Только фрагмент установки адаптера, остальной код без изменений
    private void setupRecyclerView() {
        // === DI: получаем GetAppIconUseCase из контейнера ===
        adapter = new AppsAdapter(
                app -> viewModel.launchApp(
                        app.getPackageName()
                ),
                useCaseFactory.getGetAppIconUseCase(),
                new AppsAdapter.OnItemMenuListener() {
                    @Override
                    public void onInfoClick(String packageName) {
                        viewModel.openInfoApp(packageName);
                    }

                    @Override
                    public void onDeleteClick(String packageName) {
                        viewModel.uninstallApp(packageName);
                    }
                }
        );

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        recyclerView.setAdapter(adapter);
        adapter.setDragStartListener(this);
    }

    private void showError(String message, Runnable retryAction) {
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(message != null ? message : "Произошла ошибка");
        errorButton.setVisibility(View.VISIBLE);
        errorButton.setText("Повторить");
        errorButton.setOnClickListener(v -> retryAction.run());
    }

    private void hideError() {
        errorText.setVisibility(View.GONE);
        errorButton.setVisibility(View.GONE);
        errorButton.setOnClickListener(null);
    }


    @Override
    public void onDragStart(AppShortcut app, View view) {
        if (getActivity() instanceof ViewPagerHost) {
            ViewPager2 viewPager = ((ViewPagerHost) getActivity()).getViewPager();
            if (viewPager != null && viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem(0, true);
            }
        }
        view.postDelayed(() -> {
            ClipData dragData = DragDataHelper.createAppDragData(
                    app.getPackageName(),
                    app.getAppName()
            );
            view.startDragAndDrop(dragData, new View.DragShadowBuilder(view), null,0);
        }, 100);
    }
}