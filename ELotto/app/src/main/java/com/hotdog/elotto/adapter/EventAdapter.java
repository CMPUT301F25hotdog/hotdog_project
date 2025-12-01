package com.hotdog.elotto.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
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
import android.graphics.drawable.GradientDrawable;
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

    private List<Event> eventList;
    private OnEventClickListener listener;
    private String currentUserId;

    // This will allow the fragment to know when a user clicks on an event.
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(List<Event> eventList, String currentUserId) {
        this.eventList = eventList;
        this.currentUserId = currentUserId;
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void updateEvents(List<Event> newEvents) {
        this.eventList = newEvents;
        notifyDataSetChanged();
    }
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        notifyDataSetChanged();
    }

    // ViewHolder class to reuse the views for efficiency
    public class EventViewHolder extends RecyclerView.ViewHolder {
        private ImageView eventImageView;
        private TextView eventNameTextView;
        private TextView eventDateTextView;
        private TextView eventLocationTextView;
        private TextView eventEntryCountTextView;
        private TextView eventStatusTextView;
        private TextView eventStatusTextView2;

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

        // pass in the current user id to check their status for each event
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