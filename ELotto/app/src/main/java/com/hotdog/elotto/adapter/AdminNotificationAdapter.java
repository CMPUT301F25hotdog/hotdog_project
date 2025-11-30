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
 * Adapter for displaying notifications in admin panel
 */
public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private OnNotificationActionListener listener;

    public interface OnNotificationActionListener {
        void onNotificationClick(Notification notification);
        void onDeleteNotification(Notification notification);
    }

    public AdminNotificationAdapter(List<Notification> notifications, OnNotificationActionListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvMessage, tvTimestamp, tvUserId, tvStatusBadge;
        ImageView btnDelete;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvUserId = itemView.findViewById(R.id.tv_user_id);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            btnDelete = itemView.findViewById(R.id.btn_delete_notification);
        }

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