package com.hotdog.elotto.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.model.Notification;

import java.util.List;

/**
 * Adapter to display notifications from Firebase
 */
public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public AdminNotificationAdapter(Context context, List<Notification> notifications, OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_notification, parent, false);
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
        TextView notificationStatus, notificationMessage, notificationEvent, notificationTimestamp, notificationUser;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationStatus = itemView.findViewById(R.id.notification_status);
            notificationMessage = itemView.findViewById(R.id.notification_title);
            notificationEvent = itemView.findViewById(R.id.notification_event);
            notificationTimestamp = itemView.findViewById(R.id.notification_timestamp);
            notificationUser = itemView.findViewById(R.id.notification_organizer);
        }

        public void bind(Notification notification) {
            notificationMessage.setText(notification.getMessage() != null ?
                    notification.getMessage() : "No message");

            notificationEvent.setText("Event: " +
                    (notification.getEventId() != null ? notification.getEventId() : "Unknown"));

            notificationUser.setText("User: " +
                    (notification.getUserId() != null ? notification.getUserId() : "Unknown"));

            notificationTimestamp.setText(notification.getFormattedTimestamp());

            if (notification.isRead()) {
                notificationStatus.setText("Read");
                notificationStatus.setBackgroundResource(R.drawable.status_badge_background);
            } else {
                notificationStatus.setText("Unread");
                notificationStatus.setBackgroundResource(R.drawable.button_primary_background);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }
    }
}