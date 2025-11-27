package com.hotdog.elotto.adapter;

import android.content.Context;
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
 * Adapter for displaying events in admin panel
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> events;
    private OnEventActionListener listener;

    public interface OnEventActionListener {
        void onViewEvent(Event event);
        void onDeleteEvent(Event event);
    }

    public AdminEventAdapter(Context context, List<Event> events, OnEventActionListener listener) {
        this.context = context;
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventOrganizer, eventDate, eventEntrants;
        ImageView eventImage, btnView, btnDelete;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            eventOrganizer = itemView.findViewById(R.id.event_organizer);
            eventDate = itemView.findViewById(R.id.event_date);
            eventEntrants = itemView.findViewById(R.id.event_entrants);
            eventImage = itemView.findViewById(R.id.event_image);
            btnView = itemView.findViewById(R.id.btn_view_event);
            btnDelete = itemView.findViewById(R.id.btn_delete_event);
        }

        public void bind(Event event) {
            eventName.setText(event.getName() != null ? event.getName() : "Unnamed Event");

            eventOrganizer.setText("By " +
                    (event.getOrganizerName() != null ? event.getOrganizerName() : "Unknown"));

            if (event.getEventDateTime() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault());
                eventDate.setText(sdf.format(event.getEventDateTime()));
            } else {
                eventDate.setText("Date not set");
            }

            // FIX: getMaxEntrants() returns int, not Integer
            int waitlist = event.getCurrentWaitlistCount();
            int maxEntrants = event.getMaxEntrants(); // No null check needed for primitive int
            eventEntrants.setText(waitlist + " / " + maxEntrants + " entrants");

            // Image handling - placeholder for now
            eventImage.setImageResource(R.drawable.event_24px);

            btnView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewEvent(event);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteEvent(event);
                }
            });
        }
    }
}