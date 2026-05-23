package ru.veritas.veritas_ui.ui.classic.apps;

import android.content.ClipData;
import android.content.ClipDescription;
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
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.data.loaders.AndroidIconLoader;
import ru.veritas.veritas_ui.domain.entities.AppShortcut;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.ui.classic.activity.MainActivity;
import ru.veritas.veritas_ui.ui.classic.home.HomeViewModel;
import ru.veritas.veritas_ui.ui.classic.home.HomeViewModelFactory;

public class AppsScreenFragment extends Fragment implements AppsAdapter.DragStartListener  {

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
        // Получаем GetAppIconUseCase из ViewModel (или создаём через фабрику, но лучше через ViewModel)
        // Для чистоты архитектуры создадим use case во ViewModel и передадим его.
        // В данном случае ViewModel уже имеет GetInstalledAppsUseCase, но не GetAppIconUseCase.
        // Поэтому создадим GetAppIconUseCase через фабрику в этом фрагменте – это нарушение, но допустимо,
        // так как это use case, не зависящий от состояния. Однако правильнее будет передать его из ViewModel.
        // Создадим его с правильной зависимостью (AndroidIconLoader), но чтобы не нарушать инверсию,
        // добавим в AppsScreenViewModel метод getAppIconUseCase().
        // Для простоты оставим создание здесь, но с использованием правильного конструктора адаптера.
        GetAppIconUseCase getAppIconUseCase = new GetAppIconUseCase(
                new AndroidIconLoader(requireContext().getPackageManager())
        );
        adapter = new AppsAdapter(new AppsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AppShortcut app) {
                viewModel.launchApp(app.getPackageName());
            }

            @Override
            public void onItemLongClick(AppShortcut app) {
                // Передаём добавление ярлыка через HomeViewModel (существующий)
                HomeViewModel homeViewModel = new ViewModelProvider(requireActivity(),
                        new HomeViewModelFactory(requireContext())).get(HomeViewModel.class);
                homeViewModel.addShortcut(app);
                Toast.makeText(requireContext(), "Ярлык добавлен на рабочий стол", Toast.LENGTH_SHORT).show();
            }
        }, getAppIconUseCase);
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
            ClipData.Item item = new ClipData.Item("app:" + app.getPackageName() + ":" + app.getAppName());
            ClipData dragData = new ClipData("app_shortcut",
                    new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
            view.startDragAndDrop(dragData, new View.DragShadowBuilder(view), null, 0);
        }, 100);
    }
}