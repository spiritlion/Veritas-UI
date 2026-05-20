package ru.veritas.veritas_ui.ui.classic.main.apps;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetImageUseCase;

public class AppsAdapter extends RecyclerView.Adapter<AppViewHolder> {
    private final GetImageUseCase getImageUseCase;
    private PopupMenu currentPopup;
    private List<AppShortcutDTO> apps = new ArrayList<>();
    private final OnItemClickListener listener;
    private DragStartListener dragStartListener;

    public interface OnItemClickListener {
        void onItemClick(AppShortcutDTO app);
    }

    public interface DragStartListener {
        void onDragStart(AppShortcutDTO app, View view);
    }

    public AppsAdapter(OnItemClickListener listener, Context context) {
        this.listener = listener;
        this.getImageUseCase = GetImageUseCase.create(context.getPackageManager());
    }

    public void setDragStartListener(DragStartListener listener) {
        this.dragStartListener = listener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppShortcutDTO dto = apps.get(position);
        holder.bind(dto, listener, getImageUseCase);

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            private final Handler handler = new Handler(Looper.getMainLooper());
            private Runnable longPressRunnable;
            private boolean isLongPressTriggered = false;
            private boolean hasMoved = false;
            private boolean dragStarted = false;
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
                        isLongPressTriggered = false;
                        hasMoved = false;
                        dragStarted = false;
                        // Разрешаем родителю перехватывать события (для скролла)
                        v.getParent().requestDisallowInterceptTouchEvent(false);

                        longPressRunnable = () -> {
                            isLongPressTriggered = true;
                            // Запрещаем родителю перехватывать, чтобы при движении не начался скролл
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            if (!dragStarted) {
                                showMenu(v, dto);
                            }
                        };
                        handler.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (dragStarted) return true;

                        float deltaX = Math.abs(event.getRawX() - startX);
                        float deltaY = Math.abs(event.getRawY() - startY);

                        if (deltaX > touchSlop || deltaY > touchSlop) {
                            if (!hasMoved) {
                                hasMoved = true;
                                // Если движение началось до срабатывания long press – отменяем
                                if (!isLongPressTriggered) {
                                    handler.removeCallbacks(longPressRunnable);
                                    v.getParent().requestDisallowInterceptTouchEvent(false);
                                }
                            }

                            if (isLongPressTriggered && !dragStarted) {
                                dragStarted = true;
                                handler.removeCallbacks(longPressRunnable);
                                // Закрываем меню, если показано
                                if (currentPopup != null) {
                                    currentPopup.dismiss();
                                    currentPopup = null;
                                }
                                if (dragStartListener != null) {
                                    dragStartListener.onDragStart(dto, v);
                                }
                                // Запрещаем родителю перехватывать, чтобы драг работал
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacks(longPressRunnable);
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        if (!dragStarted && !isLongPressTriggered && !hasMoved) {
                            v.performClick();
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    private void showMenu(View view, AppShortcutDTO app) {
        if (currentPopup != null) {
            currentPopup.dismiss();
        }
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.popup_app_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_item_uninstall) {
                // TODO: удаление
                return true;
            } else if (id == R.id.menu_item_about) {
                // TODO: информация
                return true;
            }
            return false;
        });
        popup.setOnDismissListener(menu -> currentPopup = null);
        popup.show();
        currentPopup = popup;
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public void setApps(List<AppShortcutDTO> apps) {
        this.apps = apps;
        notifyDataSetChanged();
    }
}