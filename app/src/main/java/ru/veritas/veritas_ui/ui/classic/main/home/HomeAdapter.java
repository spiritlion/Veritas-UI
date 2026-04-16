package ru.veritas.veritas_ui.ui.classic.main.home;

import static android.view.View.INVISIBLE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import ru.veritas.veritas_ui.R;
import ru.veritas.veritas_ui.domain.entities.AppShortcutDTO;
import ru.veritas.veritas_ui.domain.use_cases.local.home.GetImageUseCase;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.PageViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(AppShortcutDTO shortcut);
        void onItemLongClick(AppShortcutDTO shortcut);
    }

    private List<List<AppShortcutDTO>> pages; // each inner list = shortcuts on one page
    private final OnItemClickListener listener;
    private final Context context;

    public HomeAdapter(OnItemClickListener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    public void setPages(List<List<AppShortcutDTO>> pages) {
        this.pages = pages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_page, parent, false);
        return new PageViewHolder(view, listener, context);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        holder.bind(pages.get(position));
    }

    @Override
    public int getItemCount() {
        return pages == null ? 0 : pages.size();
    }

    // ViewHolder for a single page (contains an inner RecyclerView)
    static class PageViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerView recyclerPage;
        private final OnItemClickListener listener;
        private final Context context;

        PageViewHolder(@NonNull View itemView, OnItemClickListener listener, Context context) {
            super(itemView);
            this.listener = listener;
            this.context = context;
            recyclerPage = itemView.findViewById(R.id.recyclerPage);
            recyclerPage.setLayoutManager(new GridLayoutManager(itemView.getContext(), 4));
        }

        void bind(List<AppShortcutDTO> shortcuts) {
            // Inner adapter for the grid of apps on this page
            AppGridAdapter gridAdapter = new AppGridAdapter(shortcuts, listener, context);
            recyclerPage.setAdapter(gridAdapter);
        }
    }

    // Inner RecyclerView adapter for the grid of apps
    static class AppGridAdapter extends RecyclerView.Adapter<AppGridAdapter.AppViewHolder> {
        private final List<AppShortcutDTO> shortcuts;
        private final OnItemClickListener listener;
        private final GetImageUseCase getImageUseCase;

        AppGridAdapter(List<AppShortcutDTO> shortcuts, OnItemClickListener listener, Context context) {
            this.shortcuts = shortcuts;
            this.listener = listener;
            this.getImageUseCase = new GetImageUseCase(context.getPackageManager());
        }

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app, parent, false);
            return new AppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
            holder.bind(shortcuts.get(position), listener, getImageUseCase);
        }

        @Override
        public int getItemCount() {
            return shortcuts == null ? 0 : shortcuts.size();
        }

        static class AppViewHolder extends RecyclerView.ViewHolder {
            private final MaterialCardView cardView;
            private final ImageView icon;
            private final TextView name;

            public AppViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = (MaterialCardView) itemView;
                icon = itemView.findViewById(R.id.app_icon);
                name = itemView.findViewById(R.id.app_name);
            }

            public void bind(final AppShortcutDTO shortcut,
                             final OnItemClickListener listener,
                             GetImageUseCase getImageUseCase) {
                if (shortcut == null) {
                    cardView.setVisibility(INVISIBLE);
                } else {
                    icon.setImageDrawable(getImageUseCase.invoke(shortcut));
                    name.setText(shortcut.getAppName());
                    cardView.setOnClickListener(v -> listener.onItemClick(shortcut));
                    cardView.setOnLongClickListener(v -> {
                        listener.onItemLongClick(shortcut);
                        return true;
                    });
                }
            }
        }
    }
}