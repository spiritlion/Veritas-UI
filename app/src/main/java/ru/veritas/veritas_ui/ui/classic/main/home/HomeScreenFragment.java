// HomeScreenFragment.java

package ru.veritas.veritas_ui.ui.classic.main.home;

import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;
import ru.veritas.veritas_ui.ui.classic.main.home.view.ScalableContainer;

public class HomeScreenFragment extends Fragment {
    private ViewPager2 viewPager;
    private ScalableContainer scalableContainer;
    private ViewPagerPagesAdapter adapter;
    private HomeViewModel viewModel;
    private boolean editModeTriggered = false;

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
        scalableContainer = view.findViewById(R.id.scalableContainer);
        scalableContainer.setViewPager(viewPager);

        viewModel = new ViewModelProvider(requireActivity(),
                new HomeViewModelFactory(requireContext())).get(HomeViewModel.class);

        scalableContainer.setOnMultiTouchListener(isMultiTouch -> {
            viewModel.setMultiTouch(isMultiTouch);
        });

        // Настройка обработчика жеста сведения
        scalableContainer.setOnScaleListener(new ScalableContainer.OnScaleListener() {
            @Override
            public void onScale(float scaleFactor) {
                if (scaleFactor < 1.0f && !editModeTriggered) {
                    editModeTriggered = true;
                    viewModel.changeMode(HomeScreenMode.Edit);
                    scalableContainer.setPadding(60, 60, 60, 60);
                    Toast.makeText(requireContext(), "Режим редактирования", Toast.LENGTH_SHORT).show();
                } else if (scaleFactor > 1.0f && !editModeTriggered) {
                    editModeTriggered = true;
                    viewModel.changeMode(HomeScreenMode.Base);
                    scalableContainer.setPadding(20, 20, 20, 20);
                    Toast.makeText(requireContext(), "Обычный режим", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onScaleBegin() {
                editModeTriggered = false;
            }

            @Override
            public void onScaleEnd() {
                // можно добавить логику, если нужно
            }
        });

        adapter = new ViewPagerPagesAdapter(
                new ViewPagerPagesAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(AppShortcutDTO shortcut) {
                        // Проверяем режим: если Edit, то не запускаем приложение
                        if (viewModel.getMode().getValue() == HomeScreenMode.Edit) {
                            Toast.makeText(requireContext(), "Режим редактирования: нажмите и удерживайте для перемещения", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        LaunchAppUseCase launchUseCase = new LaunchAppUseCase(requireContext());
                        launchUseCase.invoke(shortcut.getPackageName());
                    }

                    @Override
                    public void onItemLongClick(int page, int row, int col, View v) {
                        HomeScreenMode currentMode = viewModel.getMode().getValue();
                        if (currentMode == HomeScreenMode.Base) {
                            viewModel.removeShortcut(page, row, col);
                            Toast.makeText(requireContext(), "Ярлык удалён", Toast.LENGTH_SHORT).show();
                        } else if (currentMode == HomeScreenMode.Edit) {
                            // Начинаем drag & drop
                            ClipData.Item item = new ClipData.Item(page + ":" + row + ":" + col);
                            ClipData dragData = new ClipData("shortcuts", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
                            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                            v.startDragAndDrop(dragData, shadowBuilder, null, 0);
                        }
                    }
                },
                requireActivity(), 4);
        viewPager.setAdapter(adapter);


        // Остальной код без изменений (загрузка данных, адаптер и т.д.)
        viewModel.loadShortcuts();
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof HomeScreenState.Loading) {
                // TODO показать прогресс
            } else if (state instanceof HomeScreenState.Content) {
                // При каждом обновлении Content пересоздаём адаптер, чтобы отобразить актуальные данные
                adapter.setPageCount(((HomeScreenState.Content) state).getApps().size());
            } else if (state instanceof HomeScreenState.Error) {
                // TODO обработка ошибки
            }
        });
    }
}