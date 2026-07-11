package ru.veritas.veritas_ui.ui.common.utils;

import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.core.entities.AppShortcut;

public class ToDoubleListUtils {
    public static List<List<AppShortcut>> invoke(List<List<List<AppShortcut>>> tripleList) {
        List<List<AppShortcut>> doubleList = new ArrayList<>();
        if (tripleList == null || tripleList.isEmpty()) {
            return doubleList;
        }
        for (int i = 0; i < tripleList.size(); i++) {
            doubleList.add(new ArrayList<>());
            for (int j = 0; j < tripleList.get(i).size(); j++) {
                for (int k = 0; k < tripleList.get(i).get(j).size(); k++) {
                    doubleList.get(i).add(
                            tripleList
                                .get(i)
                                .get(j)
                                .get(k)
                    );
                }
            }
        }
        return doubleList;
    }

    public static List<List<AppShortcut>> invoke(List<List<List<AppShortcut>>> tripleList, int sizeX, int sizeY) {
        List<List<AppShortcut>> doubleList = new ArrayList<>();

        if (tripleList == null || tripleList.isEmpty() || sizeX <= 0 || sizeY <= 0) {
            return doubleList;
        }

        List<AppShortcut> flatList = new ArrayList<>();
        for (List<List<AppShortcut>> outer : tripleList) {
            for (List<AppShortcut> inner : outer) {
                flatList.addAll(inner);
            }
        }

        int flatSize = flatList.size();
        for (int i = 0; i < sizeX; i++) {
            List<AppShortcut> row = new ArrayList<>(sizeY);
            for (int j = 0; j < sizeY; j++) {
                int index = i * sizeY + j;
                if (index < flatSize) {
                    row.add(flatList.get(index));
                } else {
                    row.add(null); 
                }
            }
            doubleList.add(row);
        }

        return doubleList;
    }
}
