package com.hotdog.elotto.adapter;

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
import com.hotdog.elotto.model.Event;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.VH> {

    public interface OnEventClickListener { void onEventClick(Event e); }

    public enum Mode { DRAWN, PENDING } // which section this adapter is used for

    private final List<Event> items;
    private final String currentUserId;
    private final Mode mode;
    private OnEventClickListener listener;

    public EventHistoryAdapter(List<Event> items, String currentUserId, Mode mode) {
        this.items = items;
        this.currentUserId = currentUserId;
        this.mode = mode;
    }

    public void setOnEventClickListener(OnEventClickListener l) { this.listener = l; }

    public void update(List<Event> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_history_card, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        Event e = items.get(position);
        h.bind(e, currentUserId, mode, listener);
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView eventImageView;
        TextView eventNameTextView, eventDateTextView, eventLocationTextView;
        TextView eventStatusTextView, eventSecondaryStatusTextView;

        VH(@NonNull View itemView) {
            super(itemView);
            eventImageView = itemView.findViewById(R.id.eventImageView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
            eventLocationTextView = itemView.findViewById(R.id.eventLocationTextView);
            eventStatusTextView = itemView.findViewById(R.id.eventStatusTextView);
            eventSecondaryStatusTextView = itemView.findViewById(R.id.eventSecondaryStatusTextView);
        }

        void bind(Event e, String currentUserId, Mode mode, OnEventClickListener listener) {
            itemView.setOnClickListener(v -> { if (listener != null) listener.onEventClick(e); });

            // Title
            eventNameTextView.setText(e.getName());

            // Date/time
            if (e.getEventDateTime() != null) {
                eventDateTextView.setText(
                        new SimpleDateFormat("MMMM dd, HH:mm", Locale.getDefault())
                                .format(e.getEventDateTime()));
            } else {
                eventDateTextView.setText("Date TBD");
            }

            // Location
            eventLocationTextView.setText(e.getLocation());

            // Poster (Base64 if present; otherwise keep placeholder)
            String base64 = e.getPosterImageUrl();
            if (base64 != null && !base64.isEmpty()) {
                try {
                    byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    eventImageView.setImageBitmap(bmp);
                } catch (IllegalArgumentException ignore) { /* keep default */ }
            }

            // ==== STATUS BADGES ====
            // We only change TEXT/visibility. Colors/backgrounds stay as defined in XML.
            eventSecondaryStatusTextView.setVisibility(View.GONE);

            boolean inSelected = e.getSelectedEntrantIds() != null
                    && e.getSelectedEntrantIds().contains(currentUserId);
            boolean inAccepted = e.getAcceptedEntrantIds() != null
                    && e.getAcceptedEntrantIds().contains(currentUserId);
            boolean inWaitlist = e.getWaitlistEntrantIds() != null
                    && e.getWaitlistEntrantIds().contains(currentUserId);

            if (mode == Mode.DRAWN) {
                // This list shows the “selected” side of history
                eventStatusTextView.setText("Selected");
                // If user is selected but not yet accepted, show an action pill
                if (inSelected && !inAccepted) {
                    eventSecondaryStatusTextView.setText("Action");
                    eventSecondaryStatusTextView.setVisibility(View.VISIBLE);
                }
            } else {
                // PENDING section: show Pending or Waitlisted
                boolean lotteryRan = e.getSelectedEntrantIds() != null
                        && !e.getSelectedEntrantIds().isEmpty();

                if (inWaitlist && lotteryRan && !inSelected) {
                    eventStatusTextView.setText("Waitlisted");
                } else {
                    eventStatusTextView.setText("Pending");
                }
            }
        }
    }
}
