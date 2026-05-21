package ru.veritas.veritas_ui.domain.use_cases.local.home;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;

public class ToDoubleListUseCase {
    public static List<List<AppShortcutDTO>> invoke(List<List<List<AppShortcutDTO>>> tripleList) {
        List<List<AppShortcutDTO>> doubleList = new ArrayList<>();
        if (tripleList == null || tripleList.isEmpty()) {
            Log.e("double list", (tripleList == null) + "");
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

    public static List<List<AppShortcutDTO>> invoke(List<List<List<AppShortcutDTO>>> tripleList, int sizeX, int sizeY) {
        List<List<AppShortcutDTO>> doubleList = new ArrayList<>();

        // Проверка входных данных
        if (tripleList == null || tripleList.isEmpty() || sizeX <= 0 || sizeY <= 0) {
            return doubleList;
        }

        // 1. Собрать все элементы в плоский список
        List<AppShortcutDTO> flatList = new ArrayList<>();
        for (List<List<AppShortcutDTO>> outer : tripleList) {
            for (List<AppShortcutDTO> inner : outer) {
                flatList.addAll(inner);
            }
        }

        // 2. Преобразовать плоский список в матрицу sizeX x sizeY
        int flatSize = flatList.size();
        for (int i = 0; i < sizeX; i++) {
            List<AppShortcutDTO> row = new ArrayList<>(sizeY);
            for (int j = 0; j < sizeY; j++) {
                int index = i * sizeY + j;
                if (index < flatSize) {
                    row.add(flatList.get(index));
                } else {
                    row.add(null); // если элементов не хватает – заполняем null
                }
            }
            doubleList.add(row);
        }

        return doubleList;
    }
}
