package com.hotdog.elotto.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.model.Event;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying Event items in a RecyclerView.
 * Binds Event data to the event_card xml layout for each item in the list.
 * The View layer of mvc
 *
 * Outstanding Issues: Still need to implement image loading
 *
 * uses RecyclerView: https://www.geeksforgeeks.org/android/android-recyclerview/
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    /**
     * The list of events to display in the RecyclerView.
     */
    private List<Event> eventList;

    /**
     * Listener for handling event item click events.
     */
    private OnEventClickListener listener;

    /**
     * The current user's ID, used to determine status badges for each event.
     */
    private String currentUserId;

    /**
     * Interface definition for a callback to be invoked when an event is clicked.
     * Basically a fancy way to scream "Hey, someone touched this!" back to the Fragment.
     */
    public interface OnEventClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param event The item that was clicked.
         */
        void onEventClick(Event event);
    }

    /**
     * Constructor to create a new EventAdapter.
     *
     * @param eventList The list of events to display. Hopefully not null or this crashes.
     * @param currentUserId The ID of the user looking at the phone. Used to color-code their rejection or acceptance.
     */
    public EventAdapter(List<Event> eventList, String currentUserId) {
        this.eventList = eventList;
        this.currentUserId = currentUserId;
    }

    /**
     * Sets the listener that catches the clicks.
     *
     * @param listener The listener that actually does the work when a click happens.
     */
    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    /**
     * Inflates the layout XML. This is where the expensive operations happen, so thank goodness
     * for the ViewHolder pattern.
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View. We only have one type, so this is decorative.
     * @return A new EventViewHolder that holds the View for each event.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_card, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds the data to the view. This runs constantly when you scroll, so don't put
     * heavy calculations here unless you like dropping frames.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
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
     * Updates the current user ID and refreshes the display to update status badges.
     *
     * @param userId the new current user ID
     */
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for individual event items in the event list.
     *
     * <p>Displays event information and status badges. Reuses views for efficiency
     * through the ViewHolder pattern. Handles decoding Base64 event poster images.</p>
     */
    public class EventViewHolder extends RecyclerView.ViewHolder {
        private ImageView eventImageView;
        private TextView eventNameTextView;
        private TextView eventDateTextView;
        private TextView eventLocationTextView;
        private TextView eventEntryCountTextView;
        private TextView eventStatusTextView;
        private TextView eventStatusTextView2;

        /**
         * Constructs a new EventViewHolder and initializes all view references.
         *
         * <p>Also sets up the click listener for the entire item view.</p>
         *
         * @param itemView the root view of the event card layout
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            eventImageView = itemView.findViewById(R.id.eventImageView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
            eventLocationTextView = itemView.findViewById(R.id.eventLocationTextView);
            eventEntryCountTextView = itemView.findViewById(R.id.eventEntryCountTextView);
            eventStatusTextView = itemView.findViewById(R.id.eventStatusTextView);
            eventStatusTextView2 = itemView.findViewById(R.id.eventStatusTextView2);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onEventClick(eventList.get(position));
                        }
                    }
                }
            });
        }

        /**
         * Binds event data to the view components.
         *
         * <p>This method populates all TextViews with event information, decodes and
         * displays the Base64 event poster image (or shows placeholder), and sets
         * the appropriate status badge based on the user's registration status.</p>
         *
         * @param event the event object containing data to display
         */
        public void bind(Event event) {
            // Set event name
            eventNameTextView.setText(event.getName());

            // Set event date and time
            if (event.getEventDateTime() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, HH:mm", Locale.getDefault());
                eventDateTextView.setText(dateFormat.format(event.getEventDateTime()));
            } else {
                eventDateTextView.setText("Date TBD");
            }

            // Set event location
            eventLocationTextView.setText(event.getLocation());

            // Set entry count
            int currentEntries = event.getCurrentWaitlistCount();
            int maxEntries = event.getMaxEntrants();
            String entryCountText = currentEntries + " Entries / " + maxEntries + " Spots";
            eventEntryCountTextView.setText(entryCountText);

            // Set status badge
            setStatusBadge(event);
            String base64Image = event.getPosterImageUrl(); // assuming your Base64 string is stored here
            if (base64Image != null && !base64Image.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    eventImageView.setImageBitmap(bitmap);
                } catch (IllegalArgumentException e) {
                    // Base64 string is invalid
                    eventImageView.setImageResource(R.drawable.baseline_image_24); // fallback
                }
            } else {
                eventImageView.setImageResource(R.drawable.baseline_image_24); // fallback
            }
            // TODO: Load event image from URL when images are implemented
            // For now just the placeholder icon will show
        }

        /**
         * Sets the status badge(s) based on the current user's registration status for
         * the event.
         *
         * <p>
         * This method checks the user's presence in various entrant lists and displays
         * appropriate status badges with color coding:
         * </p>
         * <ul>
         * <li>ACCEPTED: Green - User has accepted their invitation</li>
         * <li>SELECTED: Green with orange "ACTION REQUIRED" badge - User selected but
         * hasn't responded</li>
         * <li>WAITLISTED: Orange - Lottery drawn but user not selected</li>
         * <li>PENDING: Yellow - User on waitlist, lottery not yet drawn</li>
         * <li>CANCELED: Red - User was cancelled from the event</li>
         * </ul>
         *
         * <p>
         * If the user has not joined the event, the badge is hidden.
         * </p>
         *
         * @param event the event to check the user's status for
         */
        private void setStatusBadge(Event event) {
            // Check if user has joined this event
            boolean userInWaitlist = false;
            boolean userSelected = false;
            boolean userAccepted = false;
            boolean userCanceled = false;

            // Assume secondary status isn't needed
            eventStatusTextView2.setVisibility(View.GONE);

            if (event.getWaitlistEntrantIds() != null) {
                userInWaitlist = event.getWaitlistEntrantIds().contains(currentUserId);
            }

            if (event.getSelectedEntrantIds() != null) {
                userSelected = event.getSelectedEntrantIds().contains(currentUserId);
            }

            if (event.getAcceptedEntrantIds() != null) {
                userAccepted = event.getAcceptedEntrantIds().contains(currentUserId);
            }

            if(event.getCancelledEntrantIds() != null) {
                userCanceled = event.getCancelledEntrantIds().contains(currentUserId);
            }

            // If user hasn't joined we can hide the badge
            if (!userInWaitlist && !userSelected && !userAccepted && !userCanceled) {
                eventStatusTextView.setVisibility(View.GONE);
                return;
            }

            // User has joined so we can show the badge
            eventStatusTextView.setVisibility(View.VISIBLE);

            String statusText;
            int backgroundColor;

            if (userAccepted) {
                // User selected and accepted
                statusText = "ACCEPTED";
                backgroundColor = itemView.getContext().getColor(R.color.success_green);
            } else if (userSelected) {
                // User selected but hasn't responded so needs action
                statusText = "SELECTED";
                backgroundColor = itemView.getContext().getColor(R.color.success_green);
                eventStatusTextView2.setVisibility(View.VISIBLE);
                eventStatusTextView2.setText("ACTION REQUIRED");
                GradientDrawable drawable = (GradientDrawable) eventStatusTextView2.getBackground().mutate();
                drawable.setColor(itemView.getContext().getColor(R.color.waitlist_orange));
                // TODO: Could show two separate badges
            } else if (userInWaitlist) {
                // Check if lottery has been drawn
                if (event.getSelectedEntrantIds() != null && !event.getSelectedEntrantIds().isEmpty()) {
                    // Lottery drawn but user not selected
                    statusText = "WAITLISTED";
                    backgroundColor = itemView.getContext().getColor(R.color.waitlist_orange);
                } else {
                    // Lottery not drawn yet
                    statusText = "PENDING";
                    backgroundColor = itemView.getContext().getColor(R.color.pending_yellow);
                }
            } else if(userCanceled) {
                statusText = "CANCELED";
                backgroundColor = itemView.getContext().getColor(R.color.error_red);
            } else {
                // show pending otherwise
                statusText = "PENDING";
                backgroundColor = itemView.getContext().getColor(R.color.waitlist_orange);
            }

            eventStatusTextView.setText(statusText);

            // Using gradient drawable to be able to change the color of status_badge_background.xml
            GradientDrawable drawable = (GradientDrawable) eventStatusTextView.getBackground().mutate();
            drawable.setColor(backgroundColor);

        }
    }
}