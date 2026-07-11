package ru.veritas.veritas_ui.ui.classic.apps;

import android.content.ClipData;
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

import ru.veritas.veritas_ui.ui.R;
import ru.veritas.veritas_ui.core.entities.AppShortcut;
import ru.veritas.veritas_ui.core.command.local.home.GetAppIconUseCase;
import ru.veritas.veritas_ui.ui.common.utils.DragDataHelper;
import ru.veritas.veritas_ui.ui.common.utils.LongPressDragTouchListener;

public class AppsAdapter extends RecyclerView.Adapter<AppViewHolder> {
    private final GetAppIconUseCase getAppIconUseCase;
    private PopupMenu currentPopup;
    private List<AppShortcut> apps = new ArrayList<>();
    private final OnItemClickListener listener;
    private DragStartListener dragStartListener;

    public interface OnItemClickListener {
        void onItemClick(AppShortcut app);
    }

    public interface DragStartListener {
        void onDragStart(AppShortcut app, View view);
    }

    // Теперь use case передаётся извне (из фрагмента)
    public AppsAdapter(OnItemClickListener listener, GetAppIconUseCase getAppIconUseCase) {
        this.listener = listener;
        this.getAppIconUseCase = getAppIconUseCase;
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
        AppShortcut dto = apps.get(position);
        holder.bind(dto, listener, getAppIconUseCase);

        holder.itemView.setOnTouchListener(new LongPressDragTouchListener(new LongPressDragTouchListener.Callback() {
            @Override
            public void onClick() {
                if (listener != null) listener.onItemClick(dto);
            }

            @Override
            public void onLongPress() {
                showMenu(holder.itemView, dto);
            }

            @Override
            public void onDragStart() {
                if (currentPopup != null) {
                    currentPopup.dismiss();
                    currentPopup = null;
                }
                if (dragStartListener != null) {
                    dragStartListener.onDragStart(dto, holder.itemView);
                }
            }

            @Override
            public ClipData createDragData() {
                return DragDataHelper.createAppDragData(dto.getPackageName(), dto.getAppName());
            }
        }));
    }

    private void showMenu(View view, AppShortcut app) {
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

    public void setApps(List<AppShortcut> apps) {
        this.apps = apps;
        notifyDataSetChanged();
    }
}