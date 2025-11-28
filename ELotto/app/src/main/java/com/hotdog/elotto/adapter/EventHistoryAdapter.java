package com.hotdog.elotto.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.helpers.Status;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.User;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying Event history items in a RecyclerView.
 * Binds Event data with user status information to the item_event_history layout.
 *
 * <p>View layer in MVC pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author [Your Name]
 * @version 1.0
 * @since 2025-11-23
 */
public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.EventHistoryViewHolder> {

    private List<Event> eventList;
    private Context context;
    private OnEventClickListener listener;

    /**
     * Interface for handling event item click events.
     */
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    /**
     * Constructor for EventHistoryAdapter.
     *
     * @param eventList List of events to display
     * @param context Context for accessing resources
     */
    public EventHistoryAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    /**
     * Set the click listener for event items.
     *
     * @param listener The click listener
     */
    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_history, parent, false);
        return new EventHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventHistoryViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * Update the events list and refresh the RecyclerView.
     *
     * @param newEvents New list of events to display
     */
    public void updateEvents(List<Event> newEvents) {
        this.eventList = newEvents;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for event history items.
     */
    public class EventHistoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView eventImage;
        private TextView eventName;
        private TextView eventDate;
        private TextView eventLocation;
        private TextView eventStatus;

        public EventHistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            eventImage = itemView.findViewById(R.id.eventImage);
            eventName = itemView.findViewById(R.id.eventName);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            eventStatus = itemView.findViewById(R.id.eventStatus);

            // Set click listener for the entire card
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onEventClick(eventList.get(position));
                    }
                }
            });
        }

        /**
         * Bind event data to the view holder.
         *
         * @param event The event to display
         */
        public void bind(Event event) {
            // Set event name
            eventName.setText(event.getName());

            // Set event date
            if (event.getEventDateTime() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());
                eventDate.setText(dateFormat.format(event.getEventDateTime()));
            } else {
                eventDate.setText("Date TBD");
            }

            // Set event location
            if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                eventLocation.setText(event.getLocation());
            } else {
                eventLocation.setText("Location TBD");
            }

            // Load and set event image
            loadEventImage(event);

            // Set status badge
            setStatusBadge(event);
        }

        /**
         * Load the event poster image from Base64 string or show placeholder.
         *
         * @param event The event containing the image data
         */
        private void loadEventImage(Event event) {
            String base64Image = event.getPosterImageUrl();

            if (base64Image != null && !base64Image.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    eventImage.setImageBitmap(bitmap);
                } catch (IllegalArgumentException e) {
                    // Base64 string is invalid, use placeholder
                    eventImage.setImageResource(R.drawable.baseline_image_24);
                }
            } else {
                // No image available, use placeholder
                eventImage.setImageResource(R.drawable.baseline_image_24);
            }
        }

        /**
         * Set the status badge with appropriate text and background color.
         * Determines status based on which list the user is in for this event.
         *
         * @param event The event to check status for
         */
        private void setStatusBadge(Event event) {
            // Get current user to check their status
            User currentUser = new User(context, false);

            // Wait briefly for user data to load
            new android.os.Handler().postDelayed(() -> {
                String userId = currentUser.getId();
                Status userStatus = getUserStatusForEvent(currentUser, event.getId());

                if (userStatus != null) {
                    setStatusBadgeUI(userStatus);
                } else {
                    // Fallback: check event lists directly
                    checkEventListsForStatus(event, userId);
                }
            }, 100);
        }

        /**
         * Get the user's status for a specific event from their RegisteredEvent list.
         *
         * @param user The current user
         * @param eventId The event ID to check
         * @return The user's status, or null if not found
         */
        private Status getUserStatusForEvent(User user, String eventId) {
            List<User.RegisteredEvent> regEvents = user.getRegEvents();

            if (regEvents != null) {
                for (User.RegisteredEvent regEvent : regEvents) {
                    if (regEvent.getEventId().equals(eventId)) {
                        return regEvent.getStatus();
                    }
                }
            }

            return null;
        }

        /**
         * Fallback method to check event lists if user status is not found.
         *
         * @param event The event to check
         * @param userId The current user's ID
         */
        private void checkEventListsForStatus(Event event, String userId) {
            boolean inAccepted = event.getAcceptedEntrantIds() != null &&
                    event.getAcceptedEntrantIds().contains(userId);
            boolean inSelected = event.getSelectedEntrantIds() != null &&
                    event.getSelectedEntrantIds().contains(userId);
            boolean inWaitlist = event.getWaitlistEntrantIds() != null &&
                    event.getWaitlistEntrantIds().contains(userId);
            boolean inCancelled = event.getCancelledEntrantIds() != null &&
                    event.getCancelledEntrantIds().contains(userId);

            if (inAccepted) {
                setStatusBadgeUI(Status.Accepted);
            } else if (inSelected) {
                setStatusBadgeUI(Status.Invited);
            } else if (inCancelled) {
                setStatusBadgeUI(Status.Declined);
            } else if (inWaitlist) {
                // Check if lottery has been drawn
                if (event.getSelectedEntrantIds() != null && !event.getSelectedEntrantIds().isEmpty()) {
                    setStatusBadgeUI(Status.Waitlisted);
                } else {
                    setStatusBadgeUI(Status.Pending);
                }
            } else {
                setStatusBadgeUI(Status.Pending);
            }
        }

        /**
         * Update the status badge UI with the appropriate status text and color.
         *
         * @param status The status to display
         */
        private void setStatusBadgeUI(Status status) {
            String statusText;
            int backgroundResource;

            switch (status) {
                case Invited:
                    statusText = "Selected";
                    backgroundResource = R.drawable.button_primary_background;
                    break;
                case Accepted:
                    statusText = "Accepted";
                    backgroundResource = R.drawable.button_primary_background;
                    break;
                case Declined:
                    statusText = "Declined";
                    backgroundResource = R.drawable.button_disabled_background;
                    break;
                case Waitlisted:
                    statusText = "Waitlisted";
                    backgroundResource = R.drawable.button_leave_background;
                    break;
                case Withdrawn:
                    statusText = "Withdrawn";
                    backgroundResource = R.drawable.button_disabled_background;
                    break;
                case Pending:
                default:
                    statusText = "Pending";
                    backgroundResource = R.drawable.button_pending_background;
                    break;
            }

            eventStatus.setText(statusText);
            eventStatus.setBackgroundResource(backgroundResource);
        }
    }
}