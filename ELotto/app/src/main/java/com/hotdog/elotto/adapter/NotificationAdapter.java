package com.hotdog.elotto.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.model.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying notifications in the entrant's notification inbox.
 *
 * <p>This adapter binds Notification data to the item_notification layout and provides
 * visual differentiation between read and unread notifications through background colors
 * and indicator dots. Displays notification titles, messages, and event poster images.</p>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Bhuvnesh Batta
 * @version 1.0
 * @since 2025-11-26
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    /**
     * The list of notifications to display in the RecyclerView.
     */
    private List<Notification> notifications = new ArrayList<>();

    /**
     * Listener for handling notification item click events.
     */
    private OnNotificationClickListener listener;

    /**
     * Callback interface for handling notification item clicks.
     *
     * <p>Implementing classes should handle marking notifications as read and
     * navigating to the associated event or performing other appropriate actions.</p>
     */
    public interface OnNotificationClickListener {
        /**
         * Called when the user clicks on a notification item.
         *
         * @param notification the notification that was clicked
         */
        void onNotificationClick(Notification notification);
    }

    /**
     * Sets the listener for notification item click events.
     *
     * @param listener the listener to handle notification clicks
     */
    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the adapter with a new list of notifications and refreshes the display.
     *
     * @param notifications the new list of notifications to display
     */
    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder by inflating the notification item layout.
     *
     * @param parent the ViewGroup into which the new View will be added
     * @param viewType the view type of the new View
     * @return a new NotificationViewHolder that holds the inflated View
     */
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    /**
     * Binds notification data to the ViewHolder at the specified position.
     *
     * @param holder the ViewHolder to bind data to
     * @param position the position of the item in the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    /**
     * Returns the total number of notifications in the adapter.
     *
     * @return the size of the notifications list
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * ViewHolder class for individual notification items.
     *
     * <p>Displays notification title, message, and event image. Applies different
     * styling for read and unread notifications (background color and indicator dot).</p>
     */
    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView body;
        private ImageView dot;
        private ImageView icon;
        private View itemView;

        /**
         * Constructs a new NotificationViewHolder and initializes all view references.
         *
         * <p>Also sets up the click listener for the entire item view.</p>
         *
         * @param itemView the root view of the notification item layout
         */
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            title = itemView.findViewById(R.id.inboxNotificationTitle);
            body = itemView.findViewById(R.id.inboxNotificationBody);
            dot = itemView.findViewById(R.id.inboxNotificationDot);
            icon = itemView.findViewById(R.id.notificationIconImage);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onNotificationClick(notifications.get(position));
                }
            });
        }

        /**
         * Binds notification data to the view components.
         *
         * <p>This method populates the title and message TextViews, loads the event
         * poster image using Glide (with placeholder for missing images), and applies
         * read/unread styling:</p>
         * <ul>
         *     <li>Unread: Light blue background (#E1F5FE), indicator dot visible</li>
         *     <li>Read: White background, indicator dot hidden</li>
         * </ul>
         *
         * @param notification the notification object containing data to display
         */
        public void bind(Notification notification) {
            title.setText(notification.getTitle());
            body.setText(notification.getMessage());

            // Load event image if available
            if (notification.getEventImageUrl() != null && !notification.getEventImageUrl().isEmpty()) {
                com.bumptech.glide.Glide.with(itemView.getContext())
                        .load(notification.getEventImageUrl())
                        .placeholder(R.drawable.baseline_image_24)
                        .error(R.drawable.baseline_image_24)
                        .into(icon);
            } else {
                icon.setImageResource(R.drawable.baseline_image_24);
            }

            if (!notification.isRead()) {
                // Unread: Light Blue background, Dot visible
                itemView.setBackgroundColor(Color.parseColor("#E1F5FE")); // Light Blue 50
                dot.setVisibility(View.VISIBLE);
            } else {
                // Read: White background, Dot gone
                itemView.setBackgroundColor(Color.WHITE);
                dot.setVisibility(View.GONE);
            }
        }
    }
}