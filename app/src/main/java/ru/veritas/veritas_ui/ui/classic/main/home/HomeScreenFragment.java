package ru.veritas.veritas_ui.ui.classic.main.home;

import android.app.AlertDialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;
import ru.veritas.veritas_ui.ui.classic.main.home.favorites.FavoritesPagerAdapter;
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
    private View leftFavIndicator;
    private View rightFavIndicator;
    private int currentPageDuringDrag = -1;
    private int totalPagesDuringDrag = 0;

    // Для перелистывания favoritesViewPager
    private Runnable favoritesPageFlipRunnable;
    private int currentFavoritesPageDuringDrag = -1;
    private int totalFavoritesPagesDuringDrag = 0;
    private long lastFavoritesFlipTime = 0;
    private String dragStartZone = null; // "desktop" или "favorites"


    private ViewPager2 favoritesViewPager;
    private FavoritesPagerAdapter favoritesPagerAdapter;

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
        favoritesViewPager = view.findViewById(R.id.favoritesViewPager); // ← перенесено вверх

        // Регистрируем оба ViewPager2
        scalableContainer.registerViewPager(viewPager);
        if (favoritesViewPager != null) {
            scalableContainer.registerViewPager(favoritesViewPager);
        }

        viewModel = new ViewModelProvider(requireActivity(),
                new HomeViewModelFactory(requireContext())).get(HomeViewModel.class);

        scalableContainer.setOnMultiTouchListener(isMultiTouch -> {
            viewModel.setMultiTouch(isMultiTouch);
        });

        leftIndicator = view.findViewById(R.id.leftEdgeIndicator);
        rightIndicator = view.findViewById(R.id.rightEdgeIndicator);
        leftFavIndicator = view.findViewById(R.id.leftFavIndicator);
        rightFavIndicator = view.findViewById(R.id.rightFavIndicator);

        viewPager.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200);
        // Настройка обработчика жеста сведения
        scalableContainer.setOnScaleListener(new ScalableContainer.OnScaleListener() {
            @Override
            public void onScale(float scaleFactor) {
                if (scaleFactor < 1.0f && !editModeTriggered) {
                    editModeTriggered = true;
                    viewModel.changeMode(HomeScreenMode.Edit);
                    viewPager.animate().scaleX(0.85f).scaleY(0.85f).setDuration(200);
                    if (favoritesViewPager != null) {
                        favoritesViewPager.animate().scaleX(0.85f).scaleY(0.85f).setDuration(200);
                    }
                    Toast.makeText(requireContext(), "Режим редактирования", Toast.LENGTH_SHORT).show();
                } else if (scaleFactor > 1.0f && !editModeTriggered) {
                    editModeTriggered = true;
                    viewModel.changeMode(HomeScreenMode.Base);
                    viewPager.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200);
                    if (favoritesViewPager != null) {
                        favoritesViewPager.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200);
                    }
                    Toast.makeText(requireContext(), "Обычный режим", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onScaleBegin() {
                editModeTriggered = false;
            }

            @Override
            public void onScaleEnd() { }
        });

        scalableContainer.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    isDragging = true;
                    currentPageDuringDrag = viewPager.getCurrentItem();
                    totalPagesDuringDrag = adapter.getItemCount();
                    currentFavoritesPageDuringDrag = favoritesViewPager.getCurrentItem();
                    totalFavoritesPagesDuringDrag = favoritesPagerAdapter.getItemCount();

                    // Показываем индикаторы сразу, в зависимости от того, откуда начали драг
                    if ("favorites".equals(dragStartZone)) {
                        hideAllEdgeIndicators();
                        updateFavoriteEdgeIndicators(currentFavoritesPageDuringDrag);
                    } else {
                        // по умолчанию рабочий стол (на случай, если dragStartZone == null)
                        hideAllFavoriteIndicators();
                        updateEdgeIndicators(currentPageDuringDrag);
                    }
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION: {
                    if (!isDragging) return true;

                    float x = event.getX();
                    float y = event.getY();
                    // Получаем границы favoritesViewPager относительно scalableContainer
                    int favLeft = favoritesViewPager.getLeft();
                    int favTop = favoritesViewPager.getTop();
                    int favRight = favoritesViewPager.getRight();
                    int favBottom = favoritesViewPager.getBottom();

                    boolean overFavorites = (
                            x >= favLeft &&
                            x <= favRight &&
                            y >= favTop &&
                            y <= favBottom
                    );
                    Log.d("zone", "overFavorites=" + overFavorites + " rect=[" + favLeft + "," + favTop + "," + favRight + "," + favBottom + "]");

                    // Всегда показываем/скрываем индикаторы в зависимости от зоны
                    if (overFavorites) {
                        hideAllEdgeIndicators();              // скрыли индикаторы рабочего стола
                        updateFavoriteEdgeIndicators(currentFavoritesPageDuringDrag);  // показали избранного
                        cancelPageFlip();
                    } else {
                        hideAllFavoriteIndicators();          // скрыли избранного
                        updateEdgeIndicators(currentPageDuringDrag);                  // показали рабочего стола
                        cancelFavoritesPageFlip();
                    }

                    // Обновляем страницы (могли измениться после перелистывания)
                    int newDesktopPage = viewPager.getCurrentItem();
                    if (newDesktopPage != currentPageDuringDrag) {
                        currentPageDuringDrag = newDesktopPage;
                        updateEdgeIndicators(currentPageDuringDrag);
                    }
                    int newFavPage = favoritesViewPager.getCurrentItem();
                    if (newFavPage != currentFavoritesPageDuringDrag) {
                        currentFavoritesPageDuringDrag = newFavPage;
                        updateFavoriteEdgeIndicators(currentFavoritesPageDuringDrag);
                    }

                    // Краевые зоны – только для запуска перелистывания (индикаторы уже обновлены)
                    if (overFavorites) {
                        int favWidth = favoritesViewPager.getWidth();
                        int edgeThreshold = (int) (50 * getResources().getDisplayMetrics().density);
                        if (x < edgeThreshold && currentFavoritesPageDuringDrag > 0) {
                            scheduleFavoritesPageFlip(currentFavoritesPageDuringDrag - 1);
                        } else if (x > favWidth - edgeThreshold && currentFavoritesPageDuringDrag < totalFavoritesPagesDuringDrag - 1) {
                            scheduleFavoritesPageFlip(currentFavoritesPageDuringDrag + 1);
                        } else {
                            cancelFavoritesPageFlip();
                        }
                    } else {
                        int containerWidth = scalableContainer.getWidth();
                        int edgeThreshold = (int) (50 * getResources().getDisplayMetrics().density);
                        if (x < edgeThreshold && currentPageDuringDrag > 0) {
                            schedulePageFlip(currentPageDuringDrag - 1);
                        } else if (x > containerWidth - edgeThreshold && currentPageDuringDrag < totalPagesDuringDrag - 1) {
                            schedulePageFlip(currentPageDuringDrag + 1);
                        } else {
                            cancelPageFlip();
                        }
                    }
                    return true;
                }
                case DragEvent.ACTION_DROP:
                case DragEvent.ACTION_DRAG_ENDED:
                    isDragging = false;
                    cancelPageFlip();
                    cancelFavoritesPageFlip();
                    hideAllEdgeIndicators();
                    hideAllFavoriteIndicators();
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
                        dragStartZone = "desktop";
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


        // === Панель избранного (постраничная) ===
        favoritesPagerAdapter = new FavoritesPagerAdapter(
                requireActivity(), // ← исправлено: FragmentActivity
                new FavoritesPagerAdapter.OnFavoriteClickListener() {
                    @Override
                    public void onFavoriteClick(AppShortcutDTO shortcut) {
                        if (viewModel.getMode().getValue() == HomeScreenMode.Edit) return;
                        LaunchAppUseCase.create(requireContext()).invoke(shortcut.getPackageName());
                    }

                    @Override
                    public void onFavoriteLongClick(AppShortcutDTO shortcut, int pageIndex, int position, View v) {
                        dragStartZone = "favorites";
                        HomeScreenMode mode = viewModel.getMode().getValue();
                        if (mode == HomeScreenMode.Edit) {
                            // Начинаем drag
                            ClipData.Item item = new ClipData.Item("favorites:" + pageIndex + ":" + position);
                            ClipData dragData = new ClipData("favorites_shortcut",
                                    new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
                            View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                            v.startDragAndDrop(dragData, shadow, null, 0);
                            // Можно установить drag source в ViewModel для визуальной подсветки, если нужно
                        } else {
                            // Обычный режим – диалог удаления
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Удалить из избранного")
                                    .setMessage(shortcut.getAppName())
                                    .setPositiveButton("Удалить", (d, w) -> viewModel.removeFromFavorites(pageIndex, position))
                                    .setNegativeButton("Отмена", null)
                                    .show();
                        }
                    }
                }
        );
        favoritesViewPager.setAdapter(favoritesPagerAdapter);
        favoritesViewPager.setOffscreenPageLimit(1);

        viewModel.getFavoritesPages().observe(getViewLifecycleOwner(), pages -> {
            favoritesPagerAdapter.setPageCount(pages.size());
        });



        viewModel.loadFavorites();
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
                indicator.animate().alpha(1f).setDuration(80).start();
            }
        } else {
            // Плавное исчезновение, после которого скрываем совсем
            if (indicator.getVisibility() == View.VISIBLE) {
                indicator.animate().alpha(0f).setDuration(80).withEndAction(() -> {
                    indicator.setVisibility(View.GONE);
                }).start();
            }
        }
    }


    private void schedulePageFlip(int targetPage) {
        if (pageFlipRunnable != null && pageFlipRunnable.hashCode() == targetPage) {
            return;
        }
        if (System.currentTimeMillis() - lastFlipTime < MIN_FLIP_INTERVAL_MS) {
            return;
        }
        cancelPageFlip();
        pageFlipRunnable = () -> {
            viewPager.setCurrentItem(targetPage, true);
            lastFlipTime = System.currentTimeMillis();
            // Сразу обновляем текущую страницу и состояние индикаторов
            currentPageDuringDrag = targetPage;
            updateEdgeIndicators(targetPage);
        };
        handler.postDelayed(pageFlipRunnable, 300);
    }

    private void scheduleFavoritesPageFlip(int targetPage) {
        if (favoritesPageFlipRunnable != null && favoritesPageFlipRunnable.hashCode() == targetPage) {
            return;
        }
        if (System.currentTimeMillis() - lastFavoritesFlipTime < MIN_FLIP_INTERVAL_MS) {
            return;
        }
        cancelFavoritesPageFlip();
        favoritesPageFlipRunnable = () -> {
            favoritesViewPager.setCurrentItem(targetPage, true);
            lastFavoritesFlipTime = System.currentTimeMillis();
            currentFavoritesPageDuringDrag = targetPage;
            updateFavoriteEdgeIndicators(targetPage);
        };
        handler.postDelayed(favoritesPageFlipRunnable, 300);
    }

    private void cancelFavoritesPageFlip() {
        if (favoritesPageFlipRunnable != null) {
            handler.removeCallbacks(favoritesPageFlipRunnable);
            favoritesPageFlipRunnable = null;
        }
    }

    // Аналогично updateEdgeIndicators для избранного
    private void updateFavoriteEdgeIndicators(int currentFavPage) {
        if (!isDragging) {
            hideAllFavoriteIndicators();
            return;
        }
        animateFavoriteIndicatorVisibility(leftFavIndicator, currentFavPage > 0);
        animateFavoriteIndicatorVisibility(rightFavIndicator, currentFavPage < totalFavoritesPagesDuringDrag - 1);
    }

    private void hideAllFavoriteIndicators() {
        animateFavoriteIndicatorVisibility(leftFavIndicator, false);
        animateFavoriteIndicatorVisibility(rightFavIndicator, false);
    }

    private void animateFavoriteIndicatorVisibility(View indicator, boolean show) {
        if (show) {
            if (indicator.getVisibility() != View.VISIBLE) {
                indicator.setAlpha(0f);
                indicator.setVisibility(View.VISIBLE);
                indicator.animate().alpha(1f).setDuration(80).start();
            }
        } else {
            if (indicator.getVisibility() == View.VISIBLE) {
                indicator.animate().alpha(0f).setDuration(80)
                        .withEndAction(() -> indicator.setVisibility(View.GONE))
                        .start();
            }
        }
    }
}