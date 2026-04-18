package ru.veritas.veritas_ui.ui.classic.main.apps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.ui.classic.main.home.HomeViewModel;
import ru.veritas.veritas_ui.ui.classic.main.home.HomeViewModelFactory;

public class AppsScreenFragment extends Fragment  {

    private RecyclerView recyclerView;
    private AppsAdapter adapter;
    private ProgressBar progressIndicator;
    private AppsScreenViewModel viewModel;
    private TextView errorText;
    private Button errorButton;


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
                new AppsScreenViewModelFactory(requireContext())
        ).get(AppsScreenViewModel.class); // TODO 

        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof AppsScreenState.Loading) {
                progressIndicator.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                hideError();
            } else if (state instanceof AppsScreenState.Content) {
                progressIndicator.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                hideError();
                List<AppShortcutDTO> apps = ((AppsScreenState.Content) state).getApps();
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

    private void setupRecyclerView() {
        adapter = new AppsAdapter(new AppsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AppShortcutDTO app) {
                viewModel.launchApp(app.getPackageName());
            }

            @Override
            public void onItemLongClick(AppShortcutDTO app) {
                // Добавляем на рабочий стол
                HomeViewModel homeViewModel = new ViewModelProvider(requireActivity(),
                        new HomeViewModelFactory(requireContext())).get(HomeViewModel.class);
                homeViewModel.addShortcut(app);
                Toast.makeText(requireContext(), "Ярлык добавлен на рабочий стол", Toast.LENGTH_SHORT).show();
            }
        }, requireContext());
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        recyclerView.setAdapter(adapter);
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
}