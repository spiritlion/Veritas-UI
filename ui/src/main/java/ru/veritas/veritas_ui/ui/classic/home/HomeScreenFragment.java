package ru.veritas.veritas_ui.ui.classic.home;

import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import ru.veritas.veritas_ui.core.command.CommandFactory;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.entities.DragEventData;
import ru.veritas.veritas_ui.ui.R;
import ru.veritas.veritas_ui.ui.classic.home.favorites.FavoritesPageFragment;
import ru.veritas.veritas_ui.ui.classic.home.favorites.FavoritesViewPagerAdapter;
import ru.veritas.veritas_ui.ui.common.utils.DragDataHelper;
import ru.veritas.veritas_ui.ui.common.utils.DragHighlightHelper;
import ru.veritas.veritas_ui.ui.common.utils.EdgeAutoScrollController;
import ru.veritas.veritas_ui.ui.common.view.ToastData;

public class HomeScreenFragment extends Fragment {

    private final CommandFactory.HomeScreen homeCommandFactory;
    private final CommandFactory.Favorites favoritesCommandFactory;
    private final CommandFactory.UseCase useCaseFactory;

    // Новые утилиты
    private EdgeAutoScrollController mainPagerController;
    private EdgeAutoScrollController favoritesPagerController;
    private final DragHighlightHelper highlightHelper = new DragHighlightHelper(R.drawable.highlight_border);

    // View
    private ViewPager2 viewPager;
    private ViewPagerPagesAdapter adapter;
    private HomeViewModel viewModel;
    private View leftEdgeIndicator, rightEdgeIndicator;
    private FrameLayout homeFragmentLayout;
    private ViewPager2 favoritesViewPager;
    private FavoritesViewPagerAdapter favoritesAdapter;
    private View leftFavIndicator, rightFavIndicator;

    // Состояние drag (для синхронизации с ViewModel)
    private boolean isDragging = false;

    public HomeScreenFragment(
            CommandFactory.HomeScreen homeCommandFactory,
            CommandFactory.Favorites favoritesCommandFactory,
            CommandFactory.UseCase useCaseFactory
    ) {
        this.homeCommandFactory = homeCommandFactory;
        this.favoritesCommandFactory = favoritesCommandFactory;
        this.useCaseFactory = useCaseFactory;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация View
        viewPager = view.findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(3);
        homeFragmentLayout = view.findViewById(R.id.home_fragment);
        leftEdgeIndicator = view.findViewById(R.id.leftEdgeIndicator);
        rightEdgeIndicator = view.findViewById(R.id.rightEdgeIndicator);

        favoritesViewPager = view.findViewById(R.id.favoritesViewPager);
        leftFavIndicator = view.findViewById(R.id.leftFavIndicator);
        rightFavIndicator = view.findViewById(R.id.rightFavIndicator);
        favoritesAdapter = new FavoritesViewPagerAdapter(
                this,
                useCaseFactory.getGetAppIconUseCase(),
                useCaseFactory.getOpenSettingsUseCase()
        );
        favoritesViewPager.setAdapter(favoritesAdapter);
        favoritesViewPager.setOffscreenPageLimit(2);

        // ViewModel
        viewModel = new ViewModelProvider(
                requireActivity(),
                new HomeViewModelFactory(homeCommandFactory, favoritesCommandFactory, useCaseFactory)
        ).get(HomeViewModel.class);
        viewModel.loadInitialData();

        // Подписка на состояние рабочего стола
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof HomeScreenState.Content) {
                List<List<List<AppShortcut>>> apps = ((HomeScreenState.Content) state).getApps();
                if (adapter == null) {
                    adapter = new ViewPagerPagesAdapter(
                            createClickListener(),
                            requireActivity(),
                            useCaseFactory.getGetAppIconUseCase(),
                            useCaseFactory.getLaunchAppUseCase(),
                            4
                    );
                    adapter.setPagesData(apps);
                    viewPager.setAdapter(adapter);
                } else {
                    adapter.setPagesData(apps);
                }
            }
        });

        // Подписка на страницы избранного
        viewModel.getFavoritesPages().observe(getViewLifecycleOwner(), pages -> {
            if (pages != null && favoritesAdapter != null) {
                favoritesAdapter.setPages(pages);
                if (favoritesViewPager.getCurrentItem() >= pages.size()) {
                    favoritesViewPager.setCurrentItem(pages.size() - 1);
                }
            }
        });

        // Toast-сообщения
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), toastData -> {
            Toast toast = Toast.makeText(
                    requireContext(),
                    toastData.getMessage(),
                    toastData.getDurationIsShort() ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG
            );
            if (toastData.getType() == ToastData.ToastType.Error) {
                LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View toastView = inflater.inflate(R.layout.custom_toast_error, null);
                TextView tvErrorMessage = toastView.findViewById(R.id.tv_error_message);
                tvErrorMessage.setText(toastData.getMessage());
                toast.setView(toastView);
            }
            toast.show();
        });

        // Создаём контроллеры автоперелистывания
        mainPagerController = new EdgeAutoScrollController(
                viewPager,
                () -> adapter != null ? adapter.getItemCount() : 0,
                leftEdgeIndicator,
                rightEdgeIndicator
        );

        favoritesPagerController = new EdgeAutoScrollController(
                favoritesViewPager,
                () -> favoritesAdapter != null ? favoritesAdapter.getItemCount() : 0,
                leftFavIndicator,
                rightFavIndicator
        );

        // Настройка drag-and-drop
        setupDragAndDrop();
        setupFavoritesDragAndDrop();

        // Подписка на состояние drag (для синхронизации с ViewModel)
        viewModel.isDragging().observe(getViewLifecycleOwner(), dragging -> {
            isDragging = dragging;
            if (!dragging) {
                // Если drag завершён внешне (например, через ViewModel), сбрасываем контроллеры
                mainPagerController.onDragEnded();
                favoritesPagerController.onDragEnded();
                highlightHelper.clear();
            }
        });

        // Подписка на направление края (приходит из AppAdapter через dragEdgeListener)
        viewModel.getDragEdge().observe(getViewLifecycleOwner(), direction ->
                mainPagerController.onDragDirectionChanged(direction)
        );
    }

    // ----- Основной drag-and-drop (рабочий стол) -----
    private void setupDragAndDrop() {
        homeFragmentLayout.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d("HomeDrag", "started");
                    isDragging = true;
                    mainPagerController.onDragStarted();
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:
                    if (!isDragging) return true;
                    float x = event.getX();
                    float y = event.getY();

                    // Подсветка элемента под курсором
                    handleHighlight(x, y);

                    // Автоперелистывание
                    float containerWidth = homeFragmentLayout.getWidth();
                    float edgeThresholdPx = EdgeAutoScrollController.DEFAULT_EDGE_THRESHOLD_DP
                            * getResources().getDisplayMetrics().density;
                    mainPagerController.onDragLocation(x, containerWidth, edgeThresholdPx);
                    return true;

                case DragEvent.ACTION_DROP:
                    Log.d("HomeDrag", "drop");
                    // Проверяем, не попал ли дроп на панель избранного
                    float dropX = event.getX();
                    float dropY = event.getY();
                    int[] favLocation = new int[2];
                    favoritesViewPager.getLocationOnScreen(favLocation);
                    int[] parentLocation = new int[2];
                    homeFragmentLayout.getLocationOnScreen(parentLocation);
                    float relativeX = dropX + parentLocation[0] - favLocation[0];
                    float relativeY = dropY + parentLocation[1] - favLocation[1];
                    if (relativeX >= 0 && relativeX <= favoritesViewPager.getWidth() &&
                            relativeY >= 0 && relativeY <= favoritesViewPager.getHeight()) {
                        // Дроп на панель избранного – не обрабатываем здесь, пусть идёт дальше
                        return false;
                    }
                    // Обрабатываем дроп на рабочем столе
                    isDragging = false;
                    mainPagerController.onDragEnded();
                    highlightHelper.clear();
                    handleDrop(event);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d("HomeDrag", "ended/exited");
                    isDragging = false;
                    mainPagerController.onDragEnded();
                    highlightHelper.clear();
                    return true;

                default:
                    return false;
            }
        });
    }

    // ----- Drag-and-drop избранного -----
    private void setupFavoritesDragAndDrop() {
        favoritesViewPager.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    viewModel.setDragging(true);
                    favoritesPagerController.onDragStarted();
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                case DragEvent.ACTION_DRAG_EXITED:
                    viewModel.setDragging(false);
                    favoritesPagerController.onDragEnded();
                    return true;

                default:
                    return false;
            }
        });
    }

    // Методы, вызываемые из FavoritesPageFragment
    public void onFavoritesDragLocation(float x, float y) {
        float containerWidth = favoritesViewPager.getWidth();
        float edgeThresholdPx = EdgeAutoScrollController.DEFAULT_EDGE_THRESHOLD_DP
                * getResources().getDisplayMetrics().density;
        favoritesPagerController.onDragLocation(x, containerWidth, edgeThresholdPx);
    }

    public void onFavoritesDragEnd() {
        favoritesPagerController.onDragEnded();
    }

    // ----- Вспомогательные методы -----

    private void handleHighlight(float x, float y) {
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

        View child = recyclerView.findChildViewUnder(recyclerX, recyclerY);
        highlightHelper.highlight(child);
    }

    private void handleDrop(DragEvent event) {
        ClipData clipData = event.getClipData();
        DragEventData data = DragDataHelper.parse(clipData);
        if (data == null) return;

        float x = event.getX();
        float y = event.getY();

        int currentPage = viewPager.getCurrentItem();
        Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f" + currentPage);
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

        switch (data.getSourceType()) {
            case APPS:
                viewModel.addShortcutToDesktop(
                        new AppShortcut(data.getPackageName(), data.getAppName(), null),
                        currentPage, targetRow, targetCol
                );
                break;
            case HOME:
                viewModel.moveShortcut(data.getPage(), data.getRow(), data.getCol(),
                        currentPage, targetRow, targetCol);
                break;
            case FAVORITES:
                viewModel.swapShortcutWithFavoriteAndHome(currentPage, targetRow, targetCol,
                        data.getPage(), data.getPosition());
                break;
        }
    }

    private ViewPagerPagesAdapter.OnItemClickListener createClickListener() {
        return new ViewPagerPagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AppShortcut shortcut) {
                viewModel.launchApp(shortcut.getPackageName());
            }

            @Override
            public void onItemLongClick(int page, int row, int col, View v) {
                viewModel.setDragSource(page, row, col);
                viewModel.setDragging(true);
                ClipData dragData = DragDataHelper.createHomeShortcutDragData(page, row, col);
                v.startDragAndDrop(dragData, new View.DragShadowBuilder(v), null, 0);
            }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.setDragging(false);
        mainPagerController.onDragEnded();
        favoritesPagerController.onDragEnded();
        highlightHelper.clear();
    }

    public void openAllApps() {
        // заглушка
    }
}