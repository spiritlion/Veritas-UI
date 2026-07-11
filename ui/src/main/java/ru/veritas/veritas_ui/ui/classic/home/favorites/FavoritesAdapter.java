package ru.veritas.veritas_ui.ui.classic.home.favorites;

import android.app.Activity;
import android.content.ClipData;
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

import ru.veritas.veritas_ui.core.command.local.settings.OpenSettingsUseCase;
import ru.veritas.veritas_ui.ui.R;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.command.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.ui.classic.home.AppAdapter;
import ru.veritas.veritas_ui.ui.classic.home.HomeViewModel;
import ru.veritas.veritas_ui.ui.common.utils.DragDataHelper;
import ru.veritas.veritas_ui.ui.common.utils.IconUtils;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    private final Activity activity;
    private RadialMenuView radialMenu;
    private boolean radialMenuActive = false;
    private List<AppShortcut> originalItems;
    private List<AppShortcut> displayItems;
    private int pageIndex;
    private int columnCount;
    private final GetAppIconUseCase getImageUseCase;
    private final OpenSettingsUseCase openSettingsUseCase;
    private DragDropListener dragDropListener;
    private AppAdapter.DragEdgeListener dragEdgeListener;
    private PopupMenu currentPopup;
    private OnSpecialIconClickListener specialIconClickListener;


    private static final String SPECIAL_PACKAGE = "__special_all_apps__";

    public interface DragDropListener {
        void onDrop(int fromPage, int fromPos, int targetPage, int targetPos);
    }

    public interface OnSpecialIconClickListener {
        void onSpecialIconClick();
    }

    public FavoritesAdapter(Activity activity,
                            GetAppIconUseCase getAppIconUseCase,
                            OpenSettingsUseCase openSettingsUseCase,
                            int pageIndex, int columnCount) {
        this.activity = activity;
        this.getImageUseCase = getAppIconUseCase;
        this.openSettingsUseCase = openSettingsUseCase;
        this.pageIndex = pageIndex;
        this.columnCount = columnCount;
    }

    public void updateData(List<AppShortcut> newList) {
        this.originalItems = newList;
        buildDisplayList();
        notifyDataSetChanged();
    }

    private void buildDisplayList() {
        displayItems = new ArrayList<>();
        if (originalItems == null) originalItems = new ArrayList<>();

        for (int i = 0; i < originalItems.size(); i++) {
            if (i == 2) {
                displayItems.add(createSpecialItem());
            }
            displayItems.add(originalItems.get(i));
        }
        if (originalItems.size() < 3) {
            while (displayItems.size() < 2) {
                displayItems.add(null);
            }
            if (displayItems.size() == 2) {
                displayItems.add(createSpecialItem());
            } else if (displayItems.size() < 2) {
                displayItems.add(createSpecialItem());
            }
        } else {
            displayItems.clear();
            for (int i = 0; i < originalItems.size(); i++) {
                if (i == 2) {
                    displayItems.add(createSpecialItem());
                } else {
                    displayItems.add(originalItems.get(i));
                }
            }
            while (displayItems.size() < 3) {
                displayItems.add(null);
            }
        }
    }

    private AppShortcut createSpecialItem() {
        return new AppShortcut(SPECIAL_PACKAGE, "Все приложения", null);
    }

    private boolean isSpecialItem(AppShortcut item) {
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
        AppShortcut item = (displayItems != null && position < displayItems.size())
                ? displayItems.get(position) : null;

        if (item == null) {
            holder.card.setVisibility(View.INVISIBLE);
            holder.icon.setImageDrawable(null);
            holder.card.setOnTouchListener(null);
            return;
        }

        holder.card.setVisibility(View.VISIBLE);

        final boolean isSpecial = isSpecialItem(item);
        final boolean isThirdItem = (position == 2);

        if (isSpecial) {
            holder.icon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_all_apps));
        } else {
            holder.icon.setImageDrawable(IconUtils.toDrawable(
                    getImageUseCase.invoke(item),
                    holder.icon.getResources())
            );
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
                            openSettingsUseCase.invoke();
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
        radialMenu.animateShow();
        radialMenu.setPivotX(touchX);
        radialMenu.setPivotY(touchY);
    }

    private void removeRadialMenu() {
        if (radialMenu != null) {
            radialMenu.animate().cancel();
            radialMenu.animateHide(() -> {
                ViewGroup parent = (ViewGroup) radialMenu.getParent();
                if (parent != null) parent.removeView(radialMenu);
                radialMenu = null;
            });
        }
        radialMenuActive = false;
    }

    private void showMenu(View view, AppShortcut app, int position) {
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
        AppShortcut item = displayItems.get(position);
        if (item == null || isSpecialItem(item)) return;
        ClipData dragData = DragDataHelper.createFavoriteDragData(pageIndex, position);
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