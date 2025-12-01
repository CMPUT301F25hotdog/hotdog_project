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
 * The bridge between our data and the RecyclerView, because apparently,
 * Android can't just list things without a middleman.
 * <p>
 * Manages the display of Event objects, handles clicks, and determines
 * if you are cool enough to get into the party (status badges).
 *
 * @author Ethan Carter & Layne Pitman
 * </p>
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnEventClickListener listener;
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
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of events. If 0, the screen looks sadly empty.
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * Updates the list of events and refreshes the view.
     * Using notifyDataSetChanged is a nuclear option, but DiffUtil is too much work right now.
     *
     * @param newEvents The new list of events to display.
     */
    public void updateEvents(List<Event> newEvents) {
        this.eventList = newEvents;
        notifyDataSetChanged();
    }

    /**
     * Updates the current user ID. Useful if someone logs out and logs back in as a different
     * person without killing the app.
     *
     * @param userId The new user ID.
     */
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        notifyDataSetChanged();
    }

    /**
     * A wrapper around the view to prevent repeated findViewById calls.
     * Because apparently looking up IDs by string is slow or something.
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
         * Finds all the views in the layout and sets up the click listener.
         * The grunt work of the UI.
         *
         * @param itemView The view containing all the stuff we want to show.
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
         * Actually puts the text and images on the screen.
         * Also decodes Base64 images because storing actual image files is for quitters.
         *
         * @param event The event object containing the data to display.
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
         * The giant logic block that decides how to hurt the user's feelings.
         * Colors the badge based on whether they are accepted, rejected, or stuck in limbo.
         *
         * @param event The event to check the user's status against.
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