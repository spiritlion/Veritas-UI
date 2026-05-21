package ru.veritas.veritas_ui.ui.classic.main.home;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;
import ru.veritas.veritas_ui.ui.classic.main.home.favorites.FavoritesPageFragment;
import ru.veritas.veritas_ui.ui.classic.main.home.favorites.FavoritesViewPagerAdapter;

public class HomeScreenFragment extends Fragment {

    private static final long PAGE_SWITCH_DELAY_MS = 600;
    private ViewPager2 viewPager;
    private ViewPagerPagesAdapter adapter;
    private HomeViewModel viewModel;
    private View leftEdgeIndicator;
    private View rightEdgeIndicator;
    private FrameLayout homeFragmentLayout;

    private boolean isDragging = false;
    private int currentPageDuringDrag = -1;
    private int totalPagesDuringDrag = 0;
    private Runnable pageFlipRunnable;
    private final Handler handler = new Handler();
    private long lastFlipTime = 0;
    private static final long MIN_FLIP_INTERVAL_MS = 600;

    // Для подсветки элемента в RecyclerView
    private View highlightedView = null;


    private ViewPager2 favoritesViewPager;
    private FavoritesViewPagerAdapter favoritesAdapter;
    private View leftFavIndicator, rightFavIndicator;
    private boolean isFavDragging = false;

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
        viewPager.setOffscreenPageLimit(3);
        homeFragmentLayout = view.findViewById(R.id.home_fragment);
        leftEdgeIndicator = view.findViewById(R.id.leftEdgeIndicator);
        rightEdgeIndicator = view.findViewById(R.id.rightEdgeIndicator);


        favoritesViewPager = view.findViewById(R.id.favoritesViewPager);
        leftFavIndicator = view.findViewById(R.id.leftFavIndicator);
        rightFavIndicator = view.findViewById(R.id.rightFavIndicator);
        favoritesAdapter = new FavoritesViewPagerAdapter(this);
        favoritesViewPager.setAdapter(favoritesAdapter);
        favoritesViewPager.setOffscreenPageLimit(2);

        viewModel = new ViewModelProvider(requireActivity(),
                new HomeViewModelFactory(requireContext())).get(HomeViewModel.class);

        viewModel.loadShortcuts();

        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof HomeScreenState.Content) {
                List<List<List<AppShortcutDTO>>> apps = ((HomeScreenState.Content) state).getApps();
                if (adapter == null) {
                    adapter = new ViewPagerPagesAdapter(createClickListener(), requireActivity(), apps, 4);
                    viewPager.setAdapter(adapter);
                } else {
                    adapter.setPagesData(apps);
                }
            }
        });

        viewModel.isDragging().observe(getViewLifecycleOwner(), dragging -> {
            isDragging = dragging;
            if (dragging) {
                currentPageDuringDrag = viewPager.getCurrentItem();
                totalPagesDuringDrag = adapter.getItemCount();
                updateEdgeIndicators(currentPageDuringDrag);
            } else {
                cancelPageFlip();
                hideAllEdgeIndicators();
            }
        });

        viewModel.getDragEdge().observe(getViewLifecycleOwner(), direction -> {
            if (!isDragging) return;
            if (direction == -1 && currentPageDuringDrag > 0) {
                schedulePageFlip(currentPageDuringDrag - 1);
            } else if (direction == 1 && currentPageDuringDrag < totalPagesDuringDrag - 1) {
                schedulePageFlip(currentPageDuringDrag + 1);
            } else if (direction == 0) {
                cancelPageFlip();
            }
        });

        setupDragAndDrop();


        // Подписка на страницы избранного
        viewModel.getFavoritesPages().observe(getViewLifecycleOwner(), pages -> {
            if (pages != null && favoritesAdapter != null) {
                favoritesAdapter.setPages(pages);
                if (favoritesViewPager.getCurrentItem() >= pages.size()) {
                    favoritesViewPager.setCurrentItem(pages.size() - 1);
                }
            }
        });

// Загрузка избранного при старте
        viewModel.loadFavorites();

// Настройка drag‑and‑drop для избранного
        setupFavoritesDragAndDrop();
    }

    private void setupDragAndDrop() {
        homeFragmentLayout.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d("screen", "started");
                    isDragging = true;
                    currentPageDuringDrag = viewPager.getCurrentItem();
                    totalPagesDuringDrag = adapter.getItemCount();
                    updateEdgeIndicators(currentPageDuringDrag);
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:
                    Log.d("screen", "location");
                    if (!isDragging) return true;
                    float x = event.getX();
                    float y = event.getY();

                    // Подсветка элемента под курсором
//                    handleHighlight(x, y);

                    // Автоперелистывание при приближении к краю
                    int containerWidth = homeFragmentLayout.getWidth();
                    int edgeThreshold = (int) (50 * getResources().getDisplayMetrics().density);
                    if (x < edgeThreshold && currentPageDuringDrag > 0) {
                        Log.d("direction","Left");
                        schedulePageFlip(currentPageDuringDrag - 1);
                    } else if (x > containerWidth - edgeThreshold && currentPageDuringDrag < totalPagesDuringDrag - 1) {
                        Log.d("direction","Right");
                        schedulePageFlip(currentPageDuringDrag + 1);
                    } else {
                        cancelPageFlip();
                    }
                    return true;

                case DragEvent.ACTION_DROP:
                    Log.d("screen", "drop");
                    float dropX = event.getX();
                    float dropY = event.getY();
                    // Проверяем, не попал ли дроп на панель избранного
                    int[] favLocation = new int[2];
                    favoritesViewPager.getLocationOnScreen(favLocation);
                    int[] parentLocation = new int[2];
                    homeFragmentLayout.getLocationOnScreen(parentLocation);
                    float relativeX = dropX + parentLocation[0] - favLocation[0];
                    float relativeY = dropY + parentLocation[1] - favLocation[1];
                    if (relativeX >= 0 && relativeX <= favoritesViewPager.getWidth() &&
                            relativeY >= 0 && relativeY <= favoritesViewPager.getHeight()) {
                        // Дроп на панели избранного – не обрабатываем здесь, пусть идёт дальше
                        return false;
                    }
                    // Иначе обрабатываем дроп на рабочем столе
                    isDragging = false;
                    cancelPageFlip();
                    clearChildHighlight();
                    handleDrop(event); // нужно реализовать
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d("screen", "exited");
                    isDragging = false;
                    cancelPageFlip();
                    clearChildHighlight();
                    hideAllEdgeIndicators();
                    return true;

                default:
                    return false;
            }
        });
    }

    private void handleHighlight(float x, float y) {
        Log.d("handleHighlight", "Called");
        Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (!(currentFragment instanceof HomePageFragment)) return;

        RecyclerView recyclerView = ((HomePageFragment) currentFragment).getRecyclerView();
        if (recyclerView == null) return;

        // Преобразуем координаты из homeFragmentLayout в систему координат RecyclerView
        int[] parentLocation = new int[2];
        homeFragmentLayout.getLocationOnScreen(parentLocation);
        int[] recyclerLocation = new int[2];
        recyclerView.getLocationOnScreen(recyclerLocation);

        float recyclerX = x + parentLocation[0] - recyclerLocation[0];
        float recyclerY = y + parentLocation[1] - recyclerLocation[1];

        View child = recyclerView.findChildViewUnder(recyclerX, recyclerY);
        highlightChild(child);
    }

    private void handleDrop(DragEvent event) {
        isDragging = false;
        cancelPageFlip();
        clearChildHighlight();

        ClipData clipData = event.getClipData();
        if (clipData == null || clipData.getItemCount() == 0) return;

        String data = clipData.getItemAt(0).getText().toString();
        String[] parts = data.split(":");
        if (parts.length != 3) return;

        int fromPage = Integer.parseInt(parts[0]);
        int fromRow = Integer.parseInt(parts[1]);
        int fromCol = Integer.parseInt(parts[2]);

        float x = event.getX();
        float y = event.getY();

        Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (!(currentFragment instanceof HomePageFragment)) return;

        RecyclerView recyclerView = ((HomePageFragment) currentFragment).getRecyclerView();
        if (recyclerView == null) return;

        int[] parentLocation = new int[2];
        homeFragmentLayout.getLocationOnScreen(parentLocation);
        int[] recyclerLocation = new int[2];
        recyclerView.getLocationOnScreen(recyclerLocation);

        float recyclerX = x + parentLocation[0] - recyclerLocation[0];
        float recyclerY = y + parentLocation[1] - recyclerLocation[1];

        View targetView = recyclerView.findChildViewUnder(recyclerX, recyclerY);
        if (targetView == null) return;

        int targetPos = recyclerView.getChildAdapterPosition(targetView);
        if (targetPos == RecyclerView.NO_POSITION) return;

        int columnCount = ((GridLayoutManager) recyclerView.getLayoutManager()).getSpanCount();
        int targetRow = targetPos / columnCount;
        int targetCol = targetPos % columnCount;

        viewModel.moveShortcut(fromPage, fromRow, fromCol,
                viewPager.getCurrentItem(), targetRow, targetCol);
    }

    private void highlightChild(View child) {
        // Если это тот же самый элемент – ничего не делаем
        if (highlightedView == child) return;

        // Снимаем подсветку с предыдущего
        clearChildHighlight();

        if (child != null) {
            child.setBackgroundResource(R.drawable.highlight_border);
            highlightedView = child;
        }
    }

    private void clearChildHighlight() {
        if (highlightedView != null) {
            highlightedView.setBackground(null);
            highlightedView = null;
        }
    }

    // ----- Методы для индикаторов краёв (ваши, без изменений) -----
    private void updateEdgeIndicators(int currentPage) {
        if (!isDragging) {
            hideAllEdgeIndicators();
            return;
        }
        animateIndicatorVisibility(leftEdgeIndicator, currentPage > 0);
        animateIndicatorVisibility(rightEdgeIndicator, currentPage < totalPagesDuringDrag - 1);
    }

    private void hideAllEdgeIndicators() {
        animateIndicatorVisibility(leftEdgeIndicator, false);
        animateIndicatorVisibility(rightEdgeIndicator, false);
    }

    private void animateIndicatorVisibility(View indicator, boolean show) {
        if (show) {
            if (indicator.getVisibility() != View.VISIBLE) {
                indicator.setAlpha(0f);
                indicator.setVisibility(View.VISIBLE);
                indicator.animate().alpha(1f).setDuration(80).start();
            }
        } else {
            if (indicator.getVisibility() == View.VISIBLE) {
                indicator.animate().alpha(0f).setDuration(80).withEndAction(() ->
                        indicator.setVisibility(View.GONE)).start();
            }
        }
    }

    private int scheduledTargetPage = -1;

    private void schedulePageFlip(int targetPage) {
        if (scheduledTargetPage == targetPage) return;
        cancelPageFlip();
        scheduledTargetPage = targetPage;
        pageFlipRunnable = () -> {
            viewPager.setCurrentItem(targetPage, true);
            currentPageDuringDrag = targetPage;   // обновляем текущую страницу
            scheduledTargetPage = -1;
            updateEdgeIndicators(targetPage);
        };
        handler.postDelayed(pageFlipRunnable, 300);
    }

    private void cancelPageFlip() {
        if (pageFlipRunnable != null) {
            handler.removeCallbacks(pageFlipRunnable);
            pageFlipRunnable = null;
            scheduledTargetPage = -1;
        }
    }


    private void setupFavoritesDragAndDrop() {
        favoritesViewPager.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    isFavDragging = true;
                    viewModel.setDragging(true);
                    updateFavIndicators(favoritesViewPager.getCurrentItem());
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    isFavDragging = false;
                    cancelFavPageFlip();
                    hideAllFavIndicators();
                    return true;
                default: return false;
            }
        });
    }

    private Handler favHandler = new Handler();
    private Runnable favFlipRunnable;
    private void updateFavIndicators(int currentPage) {
        boolean hasLeft = currentPage > 0;
        boolean hasRight = currentPage < favoritesAdapter.getItemCount() - 1;
        animateIndicatorVisibility(leftFavIndicator, hasLeft);
        animateIndicatorVisibility(rightFavIndicator, hasRight);
    }
    private void hideAllFavIndicators() {
        animateIndicatorVisibility(leftFavIndicator, false);
        animateIndicatorVisibility(rightFavIndicator, false);
    }

    // Публичные методы, вызываемые из дочернего FavoritesPageFragment
    public void onFavoritesDragLocation(float x, float y) {
        int width = favoritesViewPager.getWidth();
        int threshold = (int) (50 * getResources().getDisplayMetrics().density);
        int currentPage = favoritesViewPager.getCurrentItem();
        int totalPages = favoritesAdapter.getItemCount();

        Log.d("FavAutoScroll", "x=" + x + ", width=" + width + ", threshold=" + threshold + ", currentPage=" + currentPage + ", totalPages=" + totalPages);

        if (x < threshold && currentPage > 0) {
            scheduleFavPageFlip(currentPage - 1);
        } else if (x > width - threshold && currentPage < totalPages - 1) {
            scheduleFavPageFlip(currentPage + 1);
        } else {
            cancelFavPageFlip();
        }
    }

    public void onFavoritesDragEnd() {
        cancelFavPageFlip();
        hideAllFavIndicators();
    }

    private int scheduledFavTargetPage = -1;


    // Обновлённые методы для планирования перелистывания (можно оставить как есть)
    private void scheduleFavPageFlip(int targetPage) {
        if (scheduledFavTargetPage == targetPage) return;
        cancelFavPageFlip();
        scheduledFavTargetPage = targetPage;
        favFlipRunnable = () -> {
            favoritesViewPager.setCurrentItem(targetPage, true);
            favFlipRunnable = null;
            scheduledFavTargetPage = -1;
            updateFavIndicators(targetPage);
        };
        favHandler.postDelayed(favFlipRunnable, 300);
    }

    private void cancelFavPageFlip() {
        if (favFlipRunnable != null) {
            favHandler.removeCallbacks(favFlipRunnable);
            favFlipRunnable = null;
            scheduledFavTargetPage = -1;
        }
    }

    private void handleDropOnFavorites(DragEvent event) {
        ClipData data = event.getClipData();
        if (data == null) return;
        String text = data.getItemAt(0).getText().toString();
        String[] parts = text.split(":");

        // Получаем текущий фрагмент избранного
        int targetPage = favoritesViewPager.getCurrentItem();
        Fragment currentFavFragment = getChildFragmentManager().findFragmentByTag("f" + targetPage);
        if (!(currentFavFragment instanceof FavoritesPageFragment)) return;

        RecyclerView rv = ((FavoritesPageFragment) currentFavFragment).getRecyclerView();
        if (rv == null) return;

        // Преобразуем координаты из favoritesViewPager в координаты RecyclerView
        int[] favLocation = new int[2];
        favoritesViewPager.getLocationOnScreen(favLocation);
        int[] rvLocation = new int[2];
        rv.getLocationOnScreen(rvLocation);
        float rvX = event.getX() + favLocation[0] - rvLocation[0];
        float rvY = event.getY() + favLocation[1] - rvLocation[1];

        View targetView = rv.findChildViewUnder(rvX, rvY);
        if (targetView == null) return;

        int targetPos = rv.getChildAdapterPosition(targetView);
        if (targetPos == RecyclerView.NO_POSITION) return;

        if (parts.length == 2) {
            // Перемещение внутри избранного
            int fromPage = Integer.parseInt(parts[0]);
            int fromPos = Integer.parseInt(parts[1]);
            viewModel.swapFavorites(fromPage, fromPos, targetPage, targetPos);
        } else if (parts.length == 3) {
            // Перемещение с рабочего стола в избранное
            int fromPage = Integer.parseInt(parts[0]);
            int fromRow = Integer.parseInt(parts[1]);
            int fromCol = Integer.parseInt(parts[2]);
            viewModel.swapDesktopWithFavorites(fromPage, fromRow, fromCol, targetPage, targetPos);
        }
    }

    private ViewPagerPagesAdapter.OnItemClickListener createClickListener() {
        return new ViewPagerPagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AppShortcutDTO shortcut) {
                LaunchAppUseCase.create(requireContext()).invoke(shortcut.getPackageName());
            }

            @Override
            public void onItemLongClick(int page, int row, int col, View v) {
                viewModel.setDragSource(page, row, col);
                viewModel.setDragging(true);
                ClipData.Item item = new ClipData.Item(page + ":" + row + ":" + col);
                ClipData dragData = new ClipData("shortcuts",
                        new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
                v.startDragAndDrop(dragData, new View.DragShadowBuilder(v), null, 0);
            }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.setDragging(false);
        handler.removeCallbacksAndMessages(null);
    }
}