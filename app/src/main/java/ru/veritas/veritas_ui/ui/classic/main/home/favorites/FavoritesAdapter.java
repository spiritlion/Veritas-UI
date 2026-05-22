package ru.veritas.veritas_ui.ui.classic.main.home.favorites;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetImageUseCase;
import ru.veritas.veritas_ui.ui.classic.main.home.AppAdapter;
import ru.veritas.veritas_ui.ui.classic.main.home.HomeViewModel;
import ru.veritas.veritas_ui.ui.classic.settings.SettingsActivity;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    private final Activity activity;
    private RadialMenuView radialMenu;
    private boolean radialMenuActive = false;
    private List<AppShortcutDTO> originalItems;      // исходные данные избранного
    private List<AppShortcutDTO> displayItems;       // отображаемый список (со спец. элементом)
    private int pageIndex;
    private int columnCount;
    private GetImageUseCase getImageUseCase;
    private DragDropListener dragDropListener;
    private AppAdapter.DragEdgeListener dragEdgeListener;
    private PopupMenu currentPopup;
    private OnSpecialIconClickListener specialIconClickListener;

    // Специальный маркер для идентификации
    private static final String SPECIAL_PACKAGE = "__special_all_apps__";

    public interface DragDropListener {
        void onDrop(int fromPage, int fromPos, int targetPage, int targetPos);
    }

    public interface OnSpecialIconClickListener {
        void onSpecialIconClick();
    }

    public FavoritesAdapter(Activity activity, Context context, int pageIndex, int columnCount) {
        super();
        this.activity = activity;
        this.pageIndex = pageIndex;
        this.columnCount = columnCount;
        this.getImageUseCase = GetImageUseCase.create(context.getPackageManager());
    }

    public void updateData(List<AppShortcutDTO> newList) {
        this.originalItems = newList;
        buildDisplayList();
        notifyDataSetChanged();
    }

    private void buildDisplayList() {
        displayItems = new ArrayList<>();
        if (originalItems == null) originalItems = new ArrayList<>();

        // Вставляем специальный элемент на позицию 2 (третья иконка)
        // Если исходный список меньше 3 элементов, дополняем null до позиции 2
        for (int i = 0; i < originalItems.size(); i++) {
            if (i == 2) {
                // На позицию 2 вставляем специальный элемент
                displayItems.add(createSpecialItem());
            }
            displayItems.add(originalItems.get(i));
        }
        // Если исходный список меньше 3, добавляем недостающие элементы и специальный
        if (originalItems.size() < 3) {
            while (displayItems.size() < 2) {
                displayItems.add(null); // пустые места до позиции 2
            }
            if (displayItems.size() == 2) {
                displayItems.add(createSpecialItem());
            } else if (displayItems.size() < 2) {
                // на всякий случай
                displayItems.add(createSpecialItem());
            }
        } else {
            // Если в originalItems уже есть элемент на позиции 2, он будет добавлен после специального,
            // но по условию задачи мы не хотим дублировать. Лучше вообще не добавлять originalItems[2],
            // а заменить его специальным.
            // Переделаем логику: копируем элементы, но на позиции 2 всегда ставим специальный,
            // а originalItems[2] пропускаем.
            displayItems.clear();
            for (int i = 0; i < originalItems.size(); i++) {
                if (i == 2) {
                    displayItems.add(createSpecialItem());
                    // пропускаем добавление originalItems[2]
                } else {
                    displayItems.add(originalItems.get(i));
                }
            }
            // Если после этого displayItems.size() меньше 3 (маловероятно), добавим недостающие
            while (displayItems.size() < 3) {
                displayItems.add(null);
            }
        }
    }

    private AppShortcutDTO createSpecialItem() {
        // Создаём фиктивный DTO с особым packageName
        return new AppShortcutDTO(SPECIAL_PACKAGE, "Все приложения", null);
    }

    private boolean isSpecialItem(AppShortcutDTO item) {
        return item != null && SPECIAL_PACKAGE.equals(item.getPackageName());
    }

    public void setDragDropListener(DragDropListener listener) {
        this.dragDropListener = listener;
    }

    public void setDragEdgeListener(AppAdapter.DragEdgeListener listener) {
        this.dragEdgeListener = listener;
    }

    public void setSpecialIconClickListener(OnSpecialIconClickListener listener) {
        this.specialIconClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppShortcutDTO item = (displayItems != null && position < displayItems.size())
                ? displayItems.get(position) : null;

        // Обработка пустого слота (null)
        if (item == null) {
            holder.card.setVisibility(View.INVISIBLE);
            holder.icon.setImageDrawable(null);
            holder.card.setOnTouchListener(null);
            return;
        }

        holder.card.setVisibility(View.VISIBLE);

        final boolean isSpecial = isSpecialItem(item);
        final boolean isThirdItem = (position == 2); // всегда третья позиция

        if (isSpecial) {
            // Устанавливаем специальную иконку (например, «Список приложений»)
            holder.icon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_all_apps));
        } else {
            holder.icon.setImageDrawable(getImageUseCase.invoke(item));
        }

        holder.card.setOnTouchListener(new View.OnTouchListener() {
            private final Handler handler = new Handler(Looper.getMainLooper());
            private Runnable longPressRunnable;
            private boolean dragLaunched = false;
            private boolean isLongPressTriggered = false;
            private boolean hasMoved = false;
            private float startX, startY;
            private float touchSlop;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (touchSlop == 0) {
                    touchSlop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        dragLaunched = false;
                        isLongPressTriggered = false;
                        hasMoved = false;
                        v.getParent().requestDisallowInterceptTouchEvent(true);

                        longPressRunnable = () -> {
                            isLongPressTriggered = true;
                            if (!dragLaunched) {
                                if (isThirdItem) {
                                    // Для третьей иконки (специальной или обычной) показываем радиальное меню
                                    showRadialMenu(startX, startY);
                                } else {
                                    showMenu(v, item, position);
                                }
                            }
                        };
                        handler.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (radialMenuActive) {
                            radialMenu.onFingerMoved(event.getRawX(), event.getRawY());
                            return true;
                        }
                        if (dragLaunched) return true;

                        float deltaX = Math.abs(event.getRawX() - startX);
                        float deltaY = Math.abs(event.getRawY() - startY);

                        if (deltaX > touchSlop || deltaY > touchSlop) {
                            if (!hasMoved) {
                                hasMoved = true;
                                handler.removeCallbacks(longPressRunnable);
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                            }
                            if (isLongPressTriggered && !isThirdItem) {
                                dragLaunched = true;
                                handler.removeCallbacks(longPressRunnable);
                                if (currentPopup != null) {
                                    currentPopup.dismiss();
                                    currentPopup = null;
                                }
                                startDrag(v, position);
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacks(longPressRunnable);
                        v.getParent().requestDisallowInterceptTouchEvent(false);

                        if (radialMenuActive) {
                            radialMenu.onFingerUp(event.getRawX(), event.getRawY());
                            radialMenuActive = false;
                            return true;
                        }

                        if (!dragLaunched && !isLongPressTriggered && !hasMoved) {
                            // Обычный клик
                            if (isSpecial && specialIconClickListener != null) {
                                specialIconClickListener.onSpecialIconClick();
                            } else if (!isSpecial) {
                                v.performClick();
                            }
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    private void showRadialMenu(float touchX, float touchY) {
        // Если уже есть активное меню – закрываем его с анимацией
        if (radialMenu != null) {
            removeRadialMenu();
        }

        List<RadialMenuView.MenuItem> items = new ArrayList<>();
        items.add(new RadialMenuView.MenuItem("Настройки", 0));
        items.add(new RadialMenuView.MenuItem("Виджет", 1));
        items.add(new RadialMenuView.MenuItem("Папка", 2));
        items.add(new RadialMenuView.MenuItem("Обои", 3));
        items.add(new RadialMenuView.MenuItem("Добавить", 4));

        radialMenu = new RadialMenuView(activity, touchX, touchY, items,
                new RadialMenuView.Callback() {
                    @Override
                    public void onItemSelected(int index, String label) {
                        if ("Настройки".equals(label)) {
                            Intent intent = new Intent(activity, SettingsActivity.class); // activity – поле класса
                            activity.startActivity(intent);
                        }
                        Toast.makeText(activity, "Выбрано: " + label, Toast.LENGTH_SHORT).show();
                        removeRadialMenu();
                    }
                    @Override
                    public void onDismiss() {
                        removeRadialMenu();
                    }
                });

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        decorView.addView(radialMenu, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        radialMenuActive = true;
        // Запускаем анимацию появления
        radialMenu.animateShow();
        radialMenu.setPivotX(touchX);
        radialMenu.setPivotY(touchY);
    }

    private void removeRadialMenu() {
        if (radialMenu != null) {
            // Если меню уже удаляется – отменяем предыдущую анимацию
            radialMenu.animate().cancel();
            radialMenu.animateHide(() -> {
                ViewGroup parent = (ViewGroup) radialMenu.getParent();
                if (parent != null) parent.removeView(radialMenu);
                radialMenu = null;
            });
        }
        radialMenuActive = false;
    }

    private void showMenu(View view, AppShortcutDTO app, int position) {
        // Не показываем меню для специальной иконки
        if (isSpecialItem(app)) return;

        if (currentPopup != null) {
            currentPopup.dismiss();
        }
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.popup_fav_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_item_remove_from_favorites) {
                if (view.getContext() instanceof androidx.fragment.app.FragmentActivity) {
                    HomeViewModel viewModel = new ViewModelProvider((androidx.lifecycle.ViewModelStoreOwner) view.getContext())
                            .get(HomeViewModel.class);
                    viewModel.removeFromFavorites(pageIndex, position);
                    Toast.makeText(view.getContext(), "Удалено из избранного", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (id == R.id.menu_item_about) {
                Toast.makeText(view.getContext(), "Информация о " + app.getAppName(), Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.setOnDismissListener(menu -> currentPopup = null);
        popup.show();
        currentPopup = popup;
    }

    private void startDrag(View v, int position) {
        AppShortcutDTO item = displayItems.get(position);
        if (item == null || isSpecialItem(item)) return; // специальный элемент нельзя перетаскивать
        ClipData.Item clipItem = new ClipData.Item("fav:" + pageIndex + ":" + position);
        ClipData dragData = new ClipData("favorites", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, clipItem);
        v.startDragAndDrop(dragData, new View.DragShadowBuilder(v), null, 0);
    }

    @Override
    public int getItemCount() {
        return displayItems == null ? 0 : displayItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout card;
        ImageView icon;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.inside_fav);
            icon = itemView.findViewById(R.id.fav_icon);
        }
    }
}