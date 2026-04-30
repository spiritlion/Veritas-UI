package ru.veritas.veritas_ui.ui.classic.main.home;

import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
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
    private Handler handler = new Handler();
    private Runnable pageFlipRunnable;
    private boolean isDragging = false;
//    private int originalOffscreenLimit = 1;
    private long lastFlipTime = 0;
    private static final long MIN_FLIP_INTERVAL_MS = 600; // задержка перед следующим перелистыванием
    private View leftIndicator;
    private View rightIndicator;
    private int currentPageDuringDrag = -1;
    private int totalPagesDuringDrag = 0;

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

        leftIndicator = view.findViewById(R.id.leftEdgeIndicator);
        rightIndicator = view.findViewById(R.id.rightEdgeIndicator);

        viewPager.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200);
        // Настройка обработчика жеста сведения
        scalableContainer.setOnScaleListener(new ScalableContainer.OnScaleListener() {
            @Override
            public void onScale(float scaleFactor) {
                if (scaleFactor < 1.0f && !editModeTriggered) {
                    editModeTriggered = true;
                    viewModel.changeMode(HomeScreenMode.Edit);
                    viewPager.animate().scaleX(0.85f).scaleY(0.85f).setDuration(200);
                    Toast.makeText(requireContext(), "Режим редактирования", Toast.LENGTH_SHORT).show();
                } else if (scaleFactor > 1.0f && !editModeTriggered) {
                    editModeTriggered = true;
                    viewModel.changeMode(HomeScreenMode.Base);
                    viewPager.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200);
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

        scalableContainer.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    isDragging = true;
                    // Узнаём текущую страницу и общее количество
                    currentPageDuringDrag = viewPager.getCurrentItem();
                    totalPagesDuringDrag = adapter.getItemCount();
                    updateEdgeIndicators(currentPageDuringDrag);
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:
                    if (!isDragging) return true;
                    float x = event.getX();
                    int containerWidth = scalableContainer.getWidth();
                    int edgeThreshold = (int) (50 * getResources().getDisplayMetrics().density);
                    int newPage = viewPager.getCurrentItem();

                    // Обновляем индикаторы, если страница изменилась
                    if (newPage != currentPageDuringDrag) {
                        currentPageDuringDrag = newPage;
                        updateEdgeIndicators(currentPageDuringDrag);
                    }

                    // Логика перелистывания (без изменений)
                    if (x < edgeThreshold && currentPageDuringDrag > 0) {
                        schedulePageFlip(currentPageDuringDrag - 1);
                    } else if (x > containerWidth - edgeThreshold && currentPageDuringDrag < totalPagesDuringDrag - 1) {
                        schedulePageFlip(currentPageDuringDrag + 1);
                    } else {
                        cancelPageFlip();
                    }
                    return true;

                case DragEvent.ACTION_DROP:
                case DragEvent.ACTION_DRAG_ENDED:
                    isDragging = false;
                    cancelPageFlip();
                    // Немедленно плавно прячем оба индикатора
                    hideAllEdgeIndicators();
                    // Восстановление offscreenPageLimit (если ещё нужно)...
                    return true;
            }
            return false;
        });


        adapter = new ViewPagerPagesAdapter(
                new ViewPagerPagesAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(AppShortcutDTO shortcut) {
                        if (viewModel.getMode().getValue() == HomeScreenMode.Edit) {
                            Toast.makeText(requireContext(), "Режим редактирования: нажмите и удерживайте для перемещения", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        LaunchAppUseCase launchUseCase = LaunchAppUseCase.create(requireContext());
                        launchUseCase.invoke(shortcut.getPackageName());
                    }

                    @Override
                    public void onItemLongClick(int page, int row, int col, View v) {
                        HomeScreenMode currentMode = viewModel.getMode().getValue();
                        if (currentMode == HomeScreenMode.Base) {
                            viewModel.removeShortcut(page, row, col);
                            Toast.makeText(requireContext(), "Ярлык удалён", Toast.LENGTH_SHORT).show();
                        } else if (currentMode == HomeScreenMode.Edit) {
                            Log.d("DragDrop", String.format("%d %d %d",
                                    page, row, col));
                            // Начинаем drag & drop
                            viewModel.setDragSource(page, row, col);
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
                int pages = ((HomeScreenState.Content) state).getApps().size();
                adapter.setPageCount(pages);
                viewPager.setOffscreenPageLimit(Math.max(1, pages - 1));
            } else if (state instanceof HomeScreenState.Error) {
                // TODO обработка ошибки
            }
        });
    }

    private void schedulePageFlip(int targetPage) {
        if (pageFlipRunnable != null && pageFlipRunnable.hashCode() == targetPage) {
            return; // уже запланирован переход на эту же страницу
        }
        // Проверяем кулдаун
        if (System.currentTimeMillis() - lastFlipTime < MIN_FLIP_INTERVAL_MS) {
            return; // слишком рано для нового перелистывания
        }
        cancelPageFlip();
        pageFlipRunnable = () -> {
            viewPager.setCurrentItem(targetPage, true);
            lastFlipTime = System.currentTimeMillis(); // запоминаем время перехода
        };
        handler.postDelayed(pageFlipRunnable, 300);
    }
    private void cancelPageFlip() {
        if (pageFlipRunnable != null) {
            handler.removeCallbacks(pageFlipRunnable);
            pageFlipRunnable = null;
        }
    }

    private void updateEdgeIndicators(int currentPage) {
        if (!isDragging) {
            // Если перетаскивание не активно – принудительно прячем всё
            hideAllEdgeIndicators();
            return;
        }
        // Левый индикатор
        animateIndicatorVisibility(leftIndicator, currentPage > 0);
        // Правый индикатор
        animateIndicatorVisibility(rightIndicator, currentPage < totalPagesDuringDrag - 1);
    }

    private void hideAllEdgeIndicators() {
        animateIndicatorVisibility(leftIndicator, false);
        animateIndicatorVisibility(rightIndicator, false);
    }

    private void animateIndicatorVisibility(View indicator, boolean show) {
        if (show) {
            // Плавное появление
            if (indicator.getVisibility() != View.VISIBLE) {
                indicator.setAlpha(0f);
                indicator.setVisibility(View.VISIBLE);
                indicator.animate().alpha(1f).setDuration(200).start();
            }
        } else {
            // Плавное исчезновение, после которого скрываем совсем
            if (indicator.getVisibility() == View.VISIBLE) {
                indicator.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                    indicator.setVisibility(View.GONE);
                }).start();
            }
        }
    }
}