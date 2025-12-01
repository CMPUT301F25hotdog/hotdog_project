package com.hotdog.elotto.adapter;

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
 * Adapter for displaying events in admin browse screen.
 *
 * @author Tatsat
 * @version 1.0
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    private final List<Event> events;
    private final OnEventActionListener listener;

    public interface OnEventActionListener {
        void onViewDetails(Event event);
        void onDeleteEvent(Event event);
    }

    public AdminEventAdapter(List<Event> events, OnEventActionListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEventName;
        private final TextView tvEventLocation;
        private final TextView tvEventDate;
        private final TextView tvEventEntrants;
        private final ImageView ivViewDetails;
        private final ImageView ivDelete;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvEventLocation = itemView.findViewById(R.id.tv_event_location);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
            tvEventEntrants = itemView.findViewById(R.id.tv_event_entrants);
            ivViewDetails = itemView.findViewById(R.id.iv_view_details);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }

        public void bind(Event event, OnEventActionListener listener) {
            tvEventName.setText(event.getName());

            String location = event.getLocation() != null ? event.getLocation() : "No location";
            tvEventLocation.setText(location);

            if (event.getEventDateTime() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                tvEventDate.setText(dateFormat.format(event.getEventDateTime()));
            } else {
                tvEventDate.setText("No date");
            }

            String entrantsText = "Entrants: " + event.getCurrentAcceptedCount() + "/" + event.getMaxEntrants();
            tvEventEntrants.setText(entrantsText);

            ivViewDetails.setOnClickListener(v -> listener.onViewDetails(event));
            ivDelete.setOnClickListener(v -> listener.onDeleteEvent(event));
        }
    }
}