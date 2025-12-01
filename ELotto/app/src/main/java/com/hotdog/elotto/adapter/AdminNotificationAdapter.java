package com.hotdog.elotto.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.model.Notification;

import java.util.List;

/**
 * Adapter for displaying notifications in the Admin's notification management interface.
 *
 * <p>This adapter binds Notification data to view holders and provides functionality
 * for viewing notification details and deleting notifications. Displays notification
 * status (read/unread), message content, timestamp, and associated user information.</p>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Admin Module
 * @version 1.0
 */
public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.NotificationViewHolder> {

    /**
     * The list of notifications to display in the RecyclerView.
     */
    private List<Notification> notifications;

    /**
     * Listener for handling notification actions.
     */
    private OnNotificationActionListener listener;

    /**
     * Callback interface for handling notification actions.
     *
     * <p>Implementing classes should handle the business logic for viewing
     * notification details and deleting notifications from the system.</p>
     */
    public interface OnNotificationActionListener {
        /**
         * Called when the administrator clicks on a notification to view its details.
         *
         * @param notification the notification that was clicked
         */
        void onNotificationClick(Notification notification);

        /**
         * Called when the administrator clicks to delete a notification.
         *
         * @param notification the notification to be deleted
         */
        void onDeleteNotification(Notification notification);
    }

    /**
     * Constructs a new AdminNotificationAdapter with the specified notifications and listener.
     *
     * @param notifications the list of notifications to display
     * @param listener the callback listener for handling notification actions
     */
    public AdminNotificationAdapter(List<Notification> notifications, OnNotificationActionListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder by inflating the admin notification item layout.
     *
     * @param parent the ViewGroup into which the new View will be added
     * @param viewType the view type of the new View
     * @return a new NotificationViewHolder that holds the inflated View
     */
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification, parent, false);
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
     * ViewHolder class for individual notification items in the admin notification list.
     *
     * <p>Displays notification details including event ID, message, timestamp, user ID,
     * and read/unread status. Handles click listeners for viewing details and deletion.</p>
     */
    class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvMessage, tvTimestamp, tvUserId, tvStatusBadge;
        ImageView btnDelete;

        /**
         * Constructs a new NotificationViewHolder and initializes all view references.
         *
         * @param itemView the root view of the notification item layout
         */
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvUserId = itemView.findViewById(R.id.tv_user_id);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            btnDelete = itemView.findViewById(R.id.btn_delete_notification);
        }

        /**
         * Binds notification data to the view components and sets up click listeners.
         *
         * <p>Displays the notification's event ID, message, formatted timestamp, and
         * user ID. Sets the status badge to show "Read" or "Unread" with appropriate
         * background styling. Attaches click listeners for viewing details and deletion.</p>
         *
         * @param notification the notification object containing data to display
         */
        public void bind(Notification notification) {
            // Set event ID as event name (you can enhance this by fetching event name)
            tvEventName.setText(notification.getEventId() != null ? notification.getEventId() : "Unknown Event");

            tvMessage.setText(notification.getMessage() != null ? notification.getMessage() : "No message");
            tvTimestamp.setText(notification.getFormattedTimestamp());
            tvUserId.setText("User: " + (notification.getUserId() != null ? notification.getShortUserId() : "Unknown"));

            // Status badge
            if (notification.isRead()) {
                tvStatusBadge.setText("Read");
                tvStatusBadge.setBackgroundResource(R.drawable.status_badge_background);
            } else {
                tvStatusBadge.setText("Unread");
                tvStatusBadge.setBackgroundResource(R.drawable.button_primary_background);
            }

            // Click listener for card
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });

            // Click listener for delete button
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteNotification(notification);
                }
            });
        }
    }
}