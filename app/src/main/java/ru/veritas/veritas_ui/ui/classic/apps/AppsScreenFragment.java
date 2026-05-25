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

import ru.veritas.veritas_ui.App;
import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.di.DependencyContainer;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.command.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.ui.classic.activity.MainActivity;
import ru.veritas.veritas_ui.ui.common.utils.DragDataHelper;

public class AppsScreenFragment extends Fragment implements AppsAdapter.DragStartListener  {
    private final DependencyContainer dependencyContainer;
    private RecyclerView recyclerView;
    private AppsAdapter adapter;
    private ProgressBar progressIndicator;
    private AppsScreenViewModel viewModel;
    private TextView errorText;
    private Button errorButton;

    public AppsScreenFragment(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
    }

    public AppsScreenFragment() {
        this(null);
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

        DependencyContainer dependencyContainer = ((App) requireActivity().getApplication()).getDependencyContainer();
        viewModel = new ViewModelProvider(
                requireActivity(),
                new AppsScreenViewModelFactory(dependencyContainer)
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
        GetAppIconUseCase getAppIconUseCase = dependencyContainer.getGetAppIconUseCase();

        adapter = new AppsAdapter(app -> viewModel.launchApp(app.getPackageName()), getAppIconUseCase);

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
        if (getActivity() instanceof MainActivity) {
            ViewPager2 viewPager = ((MainActivity) getActivity()).getViewPager();
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