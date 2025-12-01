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
 * Adapter for displaying events in the Admin's event management RecyclerView.
 * Provides functionality for admins to view event details and delete events.
 *
 * <p>This adapter binds Event data to the admin_event_item layout and handles
 * user interactions through the OnEventActionListener interface.</p>
 *
 * <p>View layer in MVC pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Bhuvnesh Batta
 * @version 1.0
 * @since 2025-12-01
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    private final List<Event> events;
    private final OnEventActionListener listener;

    /**
     * Interface for handling admin actions on events.
     * Implemented by the fragment or activity hosting this adapter.
     */
    public interface OnEventActionListener {
        /**
         * Called when admin clicks to view event details.
         *
         * @param event the event to view details for
         */
        void onViewDetails(Event event);

        /**
         * Called when admin clicks to delete an event.
         *
         * @param event the event to delete
         */
        void onDeleteEvent(Event event);
    }

    /**
     * Constructs a new AdminEventAdapter.
     *
     * @param events the list of events to display
     * @param listener the listener to handle event action callbacks
     */
    public AdminEventAdapter(List<Event> events, OnEventActionListener listener) {
        this.events = events;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder by inflating the admin event item layout.
     *
     * @param parent the ViewGroup into which the new View will be added
     * @param viewType the view type of the new View
     * @return a new EventViewHolder that holds the inflated View
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }
    /**
     * Binds event data to the ViewHolder at the specified position.
     *
     * @param holder the ViewHolder to bind data to
     * @param position the position of the item in the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }
    /**
     * Returns the total number of events in the adapter.
     *
     * @return the size of the events list
     */
    @Override
    public int getItemCount() {
        return events.size();
    }
    /**
     * ViewHolder class for individual event items in the admin event list.
     *
     * <p>Holds references to all view components and handles binding event
     * data to those views, as well as click listeners for admin actions.</p>
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEventName;
        private final TextView tvEventLocation;
        private final TextView tvEventDate;
        private final TextView tvEventEntrants;
        private final ImageView ivViewDetails;
        private final ImageView ivDelete;

        /**
         * Constructs a new EventViewHolder and initializes all view references.
         *
         * @param itemView the root view of the event item layout
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvEventLocation = itemView.findViewById(R.id.tv_event_location);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
            tvEventEntrants = itemView.findViewById(R.id.tv_event_entrants);
            ivViewDetails = itemView.findViewById(R.id.iv_view_details);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
        /**
         * Binds event data to the view components and sets up click listeners.
         *
         * <p>This method populates all TextViews with event information and attaches
         * click listeners to the view details and delete buttons. Handles null values
         * gracefully by displaying default text when data is missing.</p>
         *
         * @param event the event object containing data to display
         * @param listener the listener to handle view details and delete actions
         */
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