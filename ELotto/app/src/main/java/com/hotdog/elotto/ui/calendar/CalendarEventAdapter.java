package com.hotdog.elotto.ui.calendar;

import android.content.Context;
import android.content.res.ColorStateList;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter used by the Calendar screen to display each Event
 * inside the event card layout. It formats the event's name, date,
 * location, entry counts, and status into a visually styled card
 * defined in {@code event_card.xml}.
 *
 * <p>The adapter does not handle clicks or navigation; it simply binds
 * the data and refreshes the RecyclerView when the event list changes.
 */
public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.EventViewHolder> {

    private final LayoutInflater inflater;
    private final Context context;
    private List<Event> events;

    /**
     * Formatter for displaying event date & time nicely,
     * e.g. "March 5, 14:30".
     */
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMMM d, HH:mm", Locale.getDefault());

    /**
     * Creates a new adapter for showing event cards.
     *
     * @param context the screen context (used to inflate layouts)
     * @param events  initial list of events to display
     */
    public CalendarEventAdapter(Context context, List<Event> events) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.events = events;
    }

    /**
     * Updates the adapter with a fresh list of events.
     *
     * @param newEvents new list to display in the RecyclerView
     */
    public void setEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.eventNameTextView.setText(event.getName());
        holder.eventLocationTextView.setText(event.getLocation());

        // Format & display event date
        Date d = getEventDate(event);
        holder.eventDateTextView.setText(d != null ? dateFormat.format(d) : "");

        // Show "24 Entries / 50 Spots"
        holder.eventEntryCountTextView.setText(getEntryDisplay(event));

        // Style status badge ("Open", "Full", etc.)
        String rawStatus = event.getStatus() == null ? "UNKNOWN" : event.getStatus();
        holder.eventStatusTextView.setText(toTitleCase(rawStatus));
        holder.eventStatusTextView.setBackgroundTintList(
                ColorStateList.valueOf(getStatusColor(rawStatus))
        );
    }

    @Override
    public int getItemCount() {
        return events == null ? 0 : events.size();
    }

    /**
     * ViewHolder that stores references to all
     * UI components inside a single event card.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImageView;
        TextView eventNameTextView;
        TextView eventDateTextView;
        TextView eventLocationTextView;
        TextView eventEntryCountTextView;
        TextView eventStatusTextView;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImageView = itemView.findViewById(R.id.eventImageView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
            eventLocationTextView = itemView.findViewById(R.id.eventLocationTextView);
            eventEntryCountTextView = itemView.findViewById(R.id.eventEntryCountTextView);
            eventStatusTextView = itemView.findViewById(R.id.eventStatusTextView);
        }
    }

    /** Returns the event's date used for formatting in the card. */
    private Date getEventDate(Event event) {
        return event.getEventDateTime();
    }

    /** Builds the text: “24 Entries / 50 Spots”. */
    private String getEntryDisplay(Event event) {
        int entries = event.getCurrentAcceptedCount() + event.getCurrentWaitlistCount();
        int spots = event.getMaxEntrants();
        return entries + " Entries / " + spots + " Spots";
    }

    /** Returns a status color based on event state. */
    private int getStatusColor(String statusRaw) {
        String s = statusRaw == null ? "" : statusRaw.toUpperCase(Locale.ROOT);

        switch (s) {
            case "OPEN":
                return ContextCompat.getColor(context, R.color.success_green);
            case "FULL":
                return ContextCompat.getColor(context, R.color.error_red);
            case "CLOSED":
                return ContextCompat.getColor(context, R.color.pending_yellow);
            case "COMPLETED":
                return ContextCompat.getColor(context, R.color.main_icon_blue);
            default:
                return ContextCompat.getColor(context, R.color.main_icon_blue);
        }
    }

    /** Converts "OPEN" → "Open", "FULL" → "Full" for the badge text. */
    private String toTitleCase(String statusRaw) {
        if (statusRaw == null || statusRaw.isEmpty()) return "";
        String lower = statusRaw.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
