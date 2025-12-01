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
 * Adapter for displaying Event history items in the entrant's event history interface.
 *
 * <p>This adapter binds Event data with the current user's registration status to
 * the item_event_history layout. Displays event information including poster images,
 * date, location, and color-coded status badges indicating the user's relationship
 * with each event (accepted, selected, waitlisted, pending, etc.).</p>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Bhuvnesh Batta
 * @version 1.0
 * @since 2025-11-23
 */
public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.EventHistoryViewHolder> {

    /**
     * The list of events to display in the event history.
     */
    private List<Event> eventList;

    /**
     * Context for accessing resources and creating User instances.
     */
    private Context context;

    /**
     * Listener for handling event item click events.
     */
    private OnEventClickListener listener;

    /**
     * Callback interface for handling event item clicks.
     *
     * <p>Implementing classes should handle navigation to event details or
     * other appropriate actions when an event history item is clicked.</p>
     */
    public interface OnEventClickListener {
        /**
         * Called when the user clicks on an event history item.
         *
         * @param event the event that was clicked
         */
        void onEventClick(Event event);
    }

    /**
     * Constructs a new EventHistoryAdapter with the specified events and context.
     *
     * @param eventList the list of events to display in history
     * @param context the context for accessing resources
     */
    public EventHistoryAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    /**
     * Sets the listener for event item click events.
     *
     * @param listener the click listener to handle event clicks
     */
    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder by inflating the event history item layout.
     *
     * @param parent the ViewGroup into which the new View will be added
     * @param viewType the view type of the new View
     * @return a new EventHistoryViewHolder that holds the inflated View
     */
    @NonNull
    @Override
    public EventHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_history, parent, false);
        return new EventHistoryViewHolder(view);
    }

    /**
     * Binds event data to the ViewHolder at the specified position.
     *
     * @param holder the ViewHolder to bind data to
     * @param position the position of the item in the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull EventHistoryViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event);
    }

    /**
     * Returns the total number of events in the adapter.
     *
     * @return the size of the event list
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * Updates the adapter with a new list of events and refreshes the display.
     *
     * @param newEvents the new list of events to display
     */
    public void updateEvents(List<Event> newEvents) {
        this.eventList = newEvents;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for individual event history items.
     *
     * <p>Displays event information including poster image, name, date, location,
     * and a color-coded status badge. Determines the user's status by checking
     * both the User's RegisteredEvent list and the Event's entrant lists.</p>
     */
    public class EventHistoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView eventImage;
        private TextView eventName;
        private TextView eventDate;
        private TextView eventLocation;
        private TextView eventStatus;

        /**
         * Constructs a new EventHistoryViewHolder and initializes all view references.
         *
         * <p>Also sets up the click listener for the entire item view.</p>
         *
         * @param itemView the root view of the event history item layout
         */
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
         * Binds event data to the view components.
         *
         * <p>This method populates all TextViews with event information, loads and
         * displays the event poster image, and sets the appropriate status badge
         * based on the user's registration status for the event.</p>
         *
         * @param event the event object containing data to display
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
         * Loads and displays the event poster image from Base64 string.
         *
         * <p>If the Base64 string is invalid or empty, displays a placeholder image
         * instead. Handles decoding errors gracefully.</p>
         *
         * @param event the event containing the poster image data
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
         * Determines and sets the status badge for the event.
         *
         * <p>This method first attempts to retrieve the user's status from their
         * RegisteredEvent list. If not found, it falls back to checking the event's
         * entrant lists directly. Uses a brief delay to allow user data to load.</p>
         *
         * @param event the event to determine status for
         */
        private void setStatusBadge(Event event) {
            // Get current user to check their status
            User currentUser = new User(context);

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
         * Retrieves the user's status for a specific event from their RegisteredEvent list.
         *
         * @param user the current user
         * @param eventId the event ID to check
         * @return the user's Status enum value, or null if not found
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
         * Fallback method to determine user status by checking event entrant lists directly.
         *
         * <p>This method checks the event's accepted, selected, cancelled, and waitlist
         * entrant ID lists to determine the user's status. Status priority is: Accepted >
         * Selected > Cancelled > Waitlisted/Pending.</p>
         *
         * @param event the event to check
         * @param userId the current user's ID
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
                setStatusBadgeUI(Status.Selected);
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
         * Updates the status badge UI with appropriate text and background styling.
         *
         * <p>Status badge styling:</p>
         * <ul>
         *     <li>Selected/Accepted: Blue background</li>
         *     <li>Waitlisted: Red background</li>
         *     <li>Pending: Yellow background</li>
         *     <li>Declined/Withdrawn: Gray background</li>
         * </ul>
         *
         * @param status the Status enum value to display
         */
        private void setStatusBadgeUI(Status status) {
            String statusText;
            int backgroundResource;

            switch (status) {
                case Selected:
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