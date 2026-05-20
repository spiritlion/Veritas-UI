package ru.veritas.veritas_ui.ui.classic.main.home.favorites;

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
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;
import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetImageUseCase;
import ru.veritas.veritas_ui.ui.classic.main.home.AppAdapter;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    private List<AppShortcutDTO> items;
    private int pageIndex;
    private int columnCount;
    private GetImageUseCase getImageUseCase;
    private DragDropListener dragDropListener;
    private AppAdapter.DragEdgeListener dragEdgeListener;

    public interface DragDropListener {
        void onDrop(int fromPage, int fromPos, int targetPage, int targetPos);
    }

    public FavoritesAdapter(Context context, int pageIndex, int columnCount) {
        this.pageIndex = pageIndex;
        this.columnCount = columnCount;
        this.getImageUseCase = GetImageUseCase.create(context.getPackageManager());
    }

    public void updateData(List<AppShortcutDTO> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    public void setDragDropListener(DragDropListener listener) {
        this.dragDropListener = listener;
    }

    public void setDragEdgeListener(AppAdapter.DragEdgeListener listener) {
        this.dragEdgeListener = listener;
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
        AppShortcutDTO item = (items != null && position < items.size()) ? items.get(position) : null;
        if (item == null) {
            holder.card.setVisibility(View.INVISIBLE);
            return;
        }
        holder.card.setVisibility(View.VISIBLE);
        holder.icon.setImageDrawable(getImageUseCase.invoke(item));

        holder.card.setOnTouchListener(new View.OnTouchListener() {
            private Handler handler = new Handler(Looper.getMainLooper());
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
                            // Здесь можно показать меню (если нужно), пока заглушка
                            // showMenu(v, item, position);
                        };
                        handler.postDelayed(longPressRunnable, 500);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (dragLaunched) return true;

                        float deltaX = Math.abs(event.getRawX() - startX);
                        float deltaY = Math.abs(event.getRawY() - startY);

                        if (deltaX > touchSlop || deltaY > touchSlop) {
                            if (!hasMoved) {
                                hasMoved = true;
                                handler.removeCallbacks(longPressRunnable);
                                // Разрешаем родителю (RecyclerView) перехватывать скролл
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                            }

                            if (isLongPressTriggered) {
                                // Долгое нажатие уже было – запускаем drag
                                dragLaunched = true;
                                handler.removeCallbacks(longPressRunnable);
                                startDrag(v, position);
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacks(longPressRunnable);
                        v.getParent().requestDisallowInterceptTouchEvent(false);

                        if (!dragLaunched && !isLongPressTriggered && !hasMoved) {
                            // Короткий клик без движения
                            v.performClick();
                        } else if (!dragLaunched && isLongPressTriggered && !hasMoved) {
                            // Долгое нажатие без движения – можно показать меню
                            // showMenu(v, item, position);
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    private void startDrag(View v, int position) {
        ClipData.Item clipItem = new ClipData.Item("fav:" + pageIndex + ":" + position);
        ClipData dragData = new ClipData("favorites", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, clipItem);
        v.startDragAndDrop(dragData, new View.DragShadowBuilder(v), null, 0);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView icon;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.fav_card);
            icon = itemView.findViewById(R.id.fav_icon);
        }
    }
}