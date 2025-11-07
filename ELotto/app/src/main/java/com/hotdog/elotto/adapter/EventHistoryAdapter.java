package com.hotdog.elotto.adapter;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.model.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter used for displaying event history for a user.
 * The adapter supports two different display modes:
 *
 * <ul>
 *     <li><b>DRAWN</b> – The lottery for the event has been completed.
 *         Displays the user's final status (Selected / Accepted / Wait-listed).</li>
 *     <li><b>PENDING</b> – Lottery has not been drawn yet.
 *         Displays a "Pending" status indicating the event draw is not finalized.</li>
 * </ul>
 *
 * Features:
 * <ul>
 *     <li>Decodes Base64 image strings to show event posters</li>
 *     <li>Determines user-specific status based on Event participant lists</li>
 *     <li>Supports click events via {@link OnEventClickListener}</li>
 *     <li>Handles null Firestore arrays safely</li>
 * </ul>
 *
 * Usage:
 * - Used in {@code EventHistoryFragment} to populate two RecyclerViews:
 *   one for drawn events and one for pending events.
 *
 * @see com.hotdog.elotto.ui.eventhistory.EventHistoryFragment
 */

public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.VH> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }


    public enum Mode {
        DRAWN,      // Lottery completed - show results
        PENDING     // Lottery not yet drawn - show waiting status
    }

    private final List<Event> items;
    private final String currentUserId;
    private final Mode mode;
    private OnEventClickListener listener;
    /**
     * Creates a new instance of the adapter.
     *
     * @param items         list of Event objects to display
     * @param currentUserId ID of the currently logged-in user
     * @param mode          determines whether the adapter displays DRAWN or PENDING status
     */
    public EventHistoryAdapter(List<Event> items, String currentUserId, Mode mode) {
        this.items = items;
        this.currentUserId = currentUserId;
        this.mode = mode;
    }

    /**
     * Registers a listener for click events on event cards.
     *
     * @param listener callback invoked when an event card is tapped
     */

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    /**
     * Replaces the current dataset and refreshes the RecyclerView.
     *
     * @param newItems new list of events to display
     */

    public void update(List<Event> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    /**
     * Inflates the event card layout and creates a ViewHolder.
     */

    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_history_card, parent, false);
        return new VH(view);
    }
    /**
     * Binds event data to the ViewHolder for rendering.
     *
     * @param holder   recycled item view
     * @param position index of the event in the dataset
     */

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Event event = items.get(position);
        holder.bind(event, currentUserId, mode, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder responsible for binding event data into a card layout.
     * Handles:
     * <ul>
     *     <li>Image loading (Base64 → Bitmap)</li>
     *     <li>Date, title, location formatting</li>
     *     <li>Status badge display based on lottery results</li>
     * </ul>
     */

    static class VH extends RecyclerView.ViewHolder {
        ImageView eventImageView;
        TextView eventNameTextView;
        TextView eventDateTextView;
        TextView eventLocationTextView;
        TextView eventStatusTextView;
        TextView eventSecondaryStatusTextView;

        VH(@NonNull View itemView) {
            super(itemView);
            eventImageView = itemView.findViewById(R.id.eventImageView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
            eventLocationTextView = itemView.findViewById(R.id.eventLocationTextView);
            eventStatusTextView = itemView.findViewById(R.id.eventStatusTextView);
            eventSecondaryStatusTextView = itemView.findViewById(R.id.eventSecondaryStatusTextView);
        }

        /**
         * Binds event details and user lottery status to the UI components.
         *
         * @param event    event being displayed
         * @param userId   current user ID, used to determine user status
         * @param mode     adapter mode (DRAWN or PENDING)
         * @param listener optional click listener for event selection
         */

        void bind(Event event, String userId, Mode mode, OnEventClickListener listener) {
            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });

            // Event name
            eventNameTextView.setText(event.getName() != null ? event.getName() : "Unnamed Event");

            // Date and time
            if (event.getEventDateTime() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, HH:mm", Locale.getDefault());
                eventDateTextView.setText(dateFormat.format(event.getEventDateTime()));
            } else {
                eventDateTextView.setText("Date TBD");
            }

            // Location
            eventLocationTextView.setText(event.getLocation() != null ? event.getLocation() : "Location TBD");

            // Event poster image
            loadEventImage(event);

            // Status badges - main logic
            determineAndSetStatus(event, userId, mode);
        }

        /**
         * Converts a Base64 string to a Bitmap and sets it to the ImageView.
         * If decoding fails, keeps the default placeholder image.
         */
        private void loadEventImage(Event event) {
            String base64Image = event.getPosterImageUrl();
            if (base64Image != null && !base64Image.isEmpty()) {
                try {
                    byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    if (bitmap != null) {
                        eventImageView.setImageBitmap(bitmap);
                    }
                } catch (IllegalArgumentException e) {
                    // Keep default placeholder image
                }
            }
        }

        /**
         * Determines the correct status badge based on user participation in the event:
         * - Selected → User is chosen (may or may not have accepted yet)
         * - Accepted → User confirmed participation
         * - Wait-listed → User did not win the draw
         * - Pending → Lottery hasn't been drawn yet
         */

        private void determineAndSetStatus(Event event, String userId, Mode mode) {
            // Reset secondary badge
            eventSecondaryStatusTextView.setVisibility(View.GONE);

            // Get all participant lists (with null safety)
            List<String> selected = event.getSelectedEntrantIds() != null ?
                    event.getSelectedEntrantIds() : new ArrayList<>();
            List<String> accepted = event.getAcceptedEntrantIds() != null ?
                    event.getAcceptedEntrantIds() : new ArrayList<>();
            List<String> waitlist = event.getWaitlistEntrantIds() != null ?
                    event.getWaitlistEntrantIds() : new ArrayList<>();

            // Check user's participation status
            boolean isSelected = selected.contains(userId);
            boolean isAccepted = accepted.contains(userId);
            boolean isWaitlisted = waitlist.contains(userId);

            if (mode == Mode.DRAWN) {
                // Lottery has been drawn - show final results
                setDrawnStatus(isSelected, isAccepted, isWaitlisted);
            } else {
                // Mode.PENDING - lottery not yet drawn
                setPendingStatus(isWaitlisted);
            }
        }

        /**
         * Set status for DRAWN mode (lottery completed)
         */
        private void setDrawnStatus(boolean isSelected, boolean isAccepted, boolean isWaitlisted) {
            if (isAccepted) {
                // User accepted invitation - confirmed participation
                setStatusBadge("Selected", R.color.success_green);

            } else if (isSelected && !isAccepted) {
                // User was selected but hasn't responded yet - needs action
                setStatusBadge("Selected", R.color.success_green);
                setSecondaryBadge("Action", R.color.waitlist_orange);

            } else if (isWaitlisted) {
                // User was on waitlist but NOT selected (lost lottery)
                setStatusBadge("Wait-listed", R.color.error_red);

            } else {
                // Fallback - shouldn't normally reach here
                setStatusBadge("Unknown", R.color.border_grey);
            }
        }

        /**
         * Set status for PENDING mode (lottery not yet drawn)
         */
        private void setPendingStatus(boolean isWaitlisted) {
            if (isWaitlisted) {
                // User is on waitlist, waiting for lottery draw
                setStatusBadge("Pending", R.color.waitlist_orange);
            } else {
                // Shouldn't happen in pending mode, but handle gracefully
                setStatusBadge("Pending", R.color.waitlist_orange);
            }
        }

        /**
         * Set primary status badge text and color
         */
        private void setStatusBadge(String text, int colorResId) {
            eventStatusTextView.setText(text);
            int color = ContextCompat.getColor(itemView.getContext(), colorResId);
            eventStatusTextView.setBackgroundTintList(ColorStateList.valueOf(color));
        }

        /**
         * Set secondary status badge (like "Action")
         */
        private void setSecondaryBadge(String text, int colorResId) {
            eventSecondaryStatusTextView.setText(text);
            eventSecondaryStatusTextView.setVisibility(View.VISIBLE);
            int color = ContextCompat.getColor(itemView.getContext(), colorResId);
            eventSecondaryStatusTextView.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }
}