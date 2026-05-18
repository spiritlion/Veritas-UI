package ru.veritas.veritas_ui.ui.classic.main.home;

import static android.view.View.INVISIBLE;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.LaunchAppUseCase;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetImageUseCase;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private List<AppShortcutDTO> appsList;
    private ViewPagerPagesAdapter.OnItemClickListener listener;
    private GetImageUseCase getImageUseCase;
    private LaunchAppUseCase launchAppUseCase;
    private int pageIndex;
    private int columnCount;
    private PopupMenu currentPopup;
    private DragDropListener dragDropListener;

    // Callback для сообщения HomePageFragment о позиции drag у края экрана
    // direction: -1 — левый край, 0 — центр, +1 — правый край
    public interface DragEdgeListener {
        void onDragEdge(int direction);
    }
    private DragEdgeListener dragEdgeListener;

    // Порог края в dp (должен совпадать с HomeScreenFragment)
    private static final int EDGE_THRESHOLD_DP = 64;

    public AppAdapter(List<AppShortcutDTO> appsList, Context context,
                      ViewPagerPagesAdapter.OnItemClickListener listener,
                      int pageIndex, int columnCount) {
        this.appsList = appsList;
        this.listener = listener;
        this.pageIndex = pageIndex;
        this.columnCount = columnCount;
        this.getImageUseCase = GetImageUseCase.create(context.getPackageManager());
        this.launchAppUseCase = LaunchAppUseCase.create(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        ViewHolder holder = new ViewHolder(view);

//        // Добавляем DragListener на элемент
//        holder.app.setOnDragListener((v, event) -> {
//            switch (event.getAction()) {
//                case DragEvent.ACTION_DRAG_STARTED:
//                    return true;
//
//                case DragEvent.ACTION_DRAG_LOCATION: {
//                    // Вычисляем абсолютную X-координату, т.к. event.getX() — относительно карточки
//                    int[] loc = new int[2];
//                    v.getLocationOnScreen(loc);
//                    float absX = loc[0] + event.getX();
//                    float screenWidth = v.getResources().getDisplayMetrics().widthPixels;
//                    float edgePx = EDGE_THRESHOLD_DP * v.getResources().getDisplayMetrics().density;
//
//                    int direction;
//                    if (absX < edgePx) {
//                        direction = -1;
//                    } else if (absX > screenWidth - edgePx) {
//                        direction = 1;
//                    } else {
//                        direction = 0;
//                    }
//                    if (dragEdgeListener != null) dragEdgeListener.onDragEdge(direction);
//                    return true;
//                }
//
//                case DragEvent.ACTION_DRAG_ENDED:
//                case DragEvent.ACTION_DRAG_EXITED:
//                    if (dragEdgeListener != null) dragEdgeListener.onDragEdge(0);
//                    return true;
//
//                case DragEvent.ACTION_DROP: {
//                    if (dragEdgeListener != null) dragEdgeListener.onDragEdge(0);
//                    ClipData clipData = event.getClipData();
//                    if (clipData != null && clipData.getItemCount() > 0) {
//                        String data = clipData.getItemAt(0).getText().toString();
//                        String[] parts = data.split(":");
//                        if (parts.length == 3 && dragDropListener != null) {
//                            int fromPage = Integer.parseInt(parts[0]);
//                            int fromRow = Integer.parseInt(parts[1]);
//                            int fromCol = Integer.parseInt(parts[2]);
//                            int targetPos = holder.getAdapterPosition();
//                            if (targetPos != RecyclerView.NO_POSITION) {
//                                int targetRow = targetPos / columnCount;
//                                int targetCol = targetPos % columnCount;
//                                dragDropListener.onDrop(fromPage, fromRow, fromCol,
//                                        pageIndex, targetRow, targetCol);
//                            }
//                        }
//                    }
//                    return true;
//                }
//
//                default:
//                    return false;
//            }
//        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppShortcutDTO app = appsList.get(position);
        if (app == null) {
            holder.setVisibility(INVISIBLE);
            return;
        }
        holder.setVisibility(View.VISIBLE);
        holder.appName.setText(app.getAppName());
        holder.appIcon.setImageDrawable(getImageUseCase.invoke(app));

        holder.app.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(app);
        });

        holder.app.setOnTouchListener(new View.OnTouchListener() {
            private Handler handler = new Handler(Looper.getMainLooper());
            private Runnable longPressRunnable;
            private boolean isMenuShown = false;
            private boolean dragLaunched = false;
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
                        isMenuShown = false;
                        dragLaunched = false;
                        v.getParent().requestDisallowInterceptTouchEvent(true);

                        longPressRunnable = () -> {
                            if (!dragLaunched) {
                                showAppMenu(holder.app, app);
                                isMenuShown = true;
                            }
                        };
                        handler.postDelayed(longPressRunnable, 500);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (dragLaunched) return true;
                        float deltaX = Math.abs(event.getRawX() - startX);
                        float deltaY = Math.abs(event.getRawY() - startY);
                        if (deltaX > touchSlop || deltaY > touchSlop) {
                            dragLaunched = true;
                            handler.removeCallbacks(longPressRunnable);
                            if (isMenuShown && currentPopup != null) {
                                currentPopup.dismiss();
                                currentPopup = null;
                                isMenuShown = false;
                            }
                            int row = position / columnCount;
                            int col = position % columnCount;
                            ClipData.Item item = new ClipData.Item(pageIndex + ":" + row + ":" + col);
                            ClipData dragData = new ClipData("shortcuts",
                                    new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
                            v.startDragAndDrop(dragData, new View.DragShadowBuilder(v), null, 0);
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacks(longPressRunnable);
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        if (!dragLaunched && !isMenuShown) {
                            v.performClick();
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return appsList == null ? 0 : appsList.size();
    }

    public void updateData(List<AppShortcutDTO> newList) {
        this.appsList = newList;
        notifyDataSetChanged();
    }

    public void setListener(ViewPagerPagesAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setDragEdgeListener(DragEdgeListener listener) {
        this.dragEdgeListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        LinearLayout app;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            app = itemView.findViewById(R.id.inside_app);
            appIcon = itemView.findViewById(R.id.app_icon);
            appName = itemView.findViewById(R.id.app_name);
        }

        void setVisibility(int visibility) {
            app.setVisibility(visibility);
        }
    }

    private void showAppMenu(View view, AppShortcutDTO app) {
        if (currentPopup != null) {
            currentPopup.dismiss();
        }
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.popup_app_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_item_uninstall) {
                int position = appsList.indexOf(app);
                if (position != -1) {
                    int row = position / columnCount;
                    int col = position % columnCount;
                    // TODO: Удаление из базы данных
                }
                return true;
            } else if (id == R.id.menu_item_about) {
                launchAppUseCase.invoke(app.getPackageName() + " ?info");
                return true;
            }
            return false;
        });
        popup.setOnDismissListener(menu -> currentPopup = null);
        popup.show();
        currentPopup = popup;
    }

    public interface DragDropListener {
        void onDrop(int fromPage, int fromRow, int fromCol,
                    int targetPage, int targetRow, int targetCol);
    }

    public void setDragDropListener(DragDropListener listener) {
        this.dragDropListener = listener;
    }
}