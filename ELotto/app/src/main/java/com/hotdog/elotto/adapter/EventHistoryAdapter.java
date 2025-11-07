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
 * Adapter for displaying event history in two modes:
 * - DRAWN: Events where lottery has been completed
 * - PENDING: Events still waiting for lottery draw
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

    public EventHistoryAdapter(List<Event> items, String currentUserId, Mode mode) {
        this.items = items;
        this.currentUserId = currentUserId;
        this.mode = mode;
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void update(List<Event> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_history_card, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Event event = items.get(position);
        holder.bind(event, currentUserId, mode, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

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
         * Load event poster image from Base64 string
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
         * Determine user's status and set appropriate badges with colors
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