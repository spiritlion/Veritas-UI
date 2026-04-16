package ru.veritas.veritas_ui.domain.use_cases.local.home;

import ru.veritas.veritas_ui.data.repositories.HomeRepository;

public class RemoveShortcutUseCase {
    private final HomeRepository homeRepository;

    public RemoveShortcutUseCase(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
    }

    public void invoke(int i, int j, int k) {
        homeRepository.removeShortcut(i, j, k);
    }
}