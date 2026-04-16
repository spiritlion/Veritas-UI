package ru.veritas.veritas_ui.ui.classic.main.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;

public class HomeScreenFragment extends Fragment  {
    private ViewPager2 viewPager;
    private ViewPagerPagesAdapter adapter;
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
        viewPager = view.findViewById(R.id.viewPager);

        viewModel = new ViewModelProvider(requireActivity(),
                new HomeViewModelFactory(requireContext())).get(HomeViewModel.class);


        viewModel.loadShortcuts();
        viewModel.getState().observe(getViewLifecycleOwner(),
            state -> {
                if (state instanceof HomeScreenState.Loading) {
                    Log.d("Home f", "loading");
                    // TODO
                } else if (state instanceof HomeScreenState.Content) {
                    Log.d("Home f", "content");
                    adapter = new ViewPagerPagesAdapter(
                        new ViewPagerPagesAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(AppShortcutDTO shortcut) {
                                // Запускаем приложение
                                LaunchAppUseCase launchUseCase = new LaunchAppUseCase(requireContext());
                                launchUseCase.invoke(shortcut.getPackageName());
                            }

                            @Override
                            public void onItemLongClick(int i, int j, int k) {
                                viewModel.removeShortcut(i, j, k);
                                Toast.makeText(requireContext(), "Ярлык удалён", Toast.LENGTH_SHORT).show();
                            }
                        },
                        requireActivity(),
                        ((HomeScreenState.Content) state).getApps()
                    );
                    viewPager.setAdapter(adapter);
                } else if (state instanceof HomeScreenState.Error) {
                    Log.d("Home f", "error");
                    // TODO
                }
            }
        );

//        viewPager.setLayoutManager(new GridLayoutManager(requireContext(), 4));
    }
}