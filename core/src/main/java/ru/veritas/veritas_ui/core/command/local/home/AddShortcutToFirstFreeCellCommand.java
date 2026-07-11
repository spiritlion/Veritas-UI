package ru.veritas.veritas_ui.core.command.local.home;

import ru.veritas.veritas_ui.core.command.Command;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.repositories.HomeRepository;
import java.util.List;

public class AddShortcutToFirstFreeCellCommand implements Command<Void> {
    private final HomeRepository repository;
    private final AppShortcut shortcut;
    private int addedPage = -1, addedRow = -1, addedCol = -1;

    public AddShortcutToFirstFreeCellCommand(HomeRepository repository, AppShortcut shortcut) {
        this.repository = repository;
        this.shortcut = shortcut;
    }

    @Override
    public Void execute() {
        List<List<List<AppShortcut>>> all = repository.getShortcuts();
        for (int p = 0; p < all.size(); p++) {
            List<List<AppShortcut>> page = all.get(p);
            for (int r = 0; r < page.size(); r++) {
                List<AppShortcut> row = page.get(r);
                for (int c = 0; c < row.size(); c++) {
                    if (row.get(c) == null) {
                        repository.addShortcut(p, r, c, shortcut);
                        addedPage = p; addedRow = r; addedCol = c;
                        return null;
                    }
                }
            }
        }
        throw new IllegalStateException("No free cell on desktop");
    }

    @Override
    public void undo() {
        if (addedPage >= 0) {
            repository.removeShortcut(addedPage, addedRow, addedCol);
        }
    }

    @Override
    public boolean isUndoable() { return true; }
}