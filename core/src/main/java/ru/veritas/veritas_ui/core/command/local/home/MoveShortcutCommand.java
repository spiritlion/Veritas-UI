package ru.veritas.veritas_ui.core.command.local.home;

import ru.veritas.veritas_ui.core.command.Command;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.repositories.HomeRepository;

public class MoveShortcutCommand implements Command<Void> {
    private final HomeRepository repository;
    private final int fromPage, fromRow, fromCol;
    private final int toPage, toRow, toCol;

    // Сохраняем старое состояние для undo
    private AppShortcut fromSnapshot;
    private AppShortcut toSnapshot;

    public MoveShortcutCommand(HomeRepository repository,
                               int fromPage, int fromRow, int fromCol,
                               int toPage, int toRow, int toCol) {
        this.repository = repository;
        this.fromPage = fromPage; this.fromRow = fromRow; this.fromCol = fromCol;
        this.toPage = toPage; this.toRow = toRow; this.toCol = toCol;
    }

    @Override
    public Void execute() {
        fromSnapshot = repository.getShortcut(fromPage, fromRow, fromCol);
        toSnapshot = repository.getShortcut(toPage, toRow, toCol);

        repository.addShortcut(toPage, toRow, toCol, fromSnapshot);
        repository.addShortcut(fromPage, fromRow, fromCol, toSnapshot);
        return null;
    }

    @Override
    public void undo() {
        if (fromSnapshot == null || toSnapshot == null) return;
        repository.addShortcut(fromPage, fromRow, fromCol, fromSnapshot);
        repository.addShortcut(toPage, toRow, toCol, toSnapshot);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }
}