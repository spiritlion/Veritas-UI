package ru.veritas.veritas_ui.ui.classic.main.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;

public class HomeScreenFragment extends Fragment  {

    private RecyclerView recyclerView;
    private HomeAdapter adapter;
    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        setupRecyclerView();

        viewModel = new ViewModelProvider(requireActivity(),
                new HomeViewModelFactory(requireContext())).get(HomeViewModel.class);

        viewModel.getShortcuts().observe(getViewLifecycleOwner(), shortcuts -> {
            if (shortcuts != null) {
                adapter.setShortcuts(shortcuts);
            }
        });

        viewModel.loadShortcuts();
    }

    private void setupRecyclerView() {
        adapter = new HomeAdapter(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AppShortcut shortcut) {
                // Запускаем приложение
                LaunchAppUseCase launchUseCase = new LaunchAppUseCase(requireContext());
                launchUseCase.invoke(shortcut.getPackageName());
            }

            @Override
            public void onItemLongClick(AppShortcut shortcut) {
                // Удаляем ярлык
                viewModel.removeShortcut(shortcut.getPackageName());
                Toast.makeText(requireContext(), "Ярлык удалён", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        recyclerView.setAdapter(adapter);
    }

}