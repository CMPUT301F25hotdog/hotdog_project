package com.hotdog.elotto.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.model.EntrantInfo;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying entrant information in the Organizer's event management interface.
 *
 * <p>This adapter dynamically displays different content based on the current tab
 * (waiting, selected, accepted, cancelled). It shows entrant names, status-specific
 * text, and conditionally displays cancel buttons for entrant management.</p>
 *
 * <p>Uses the item_organizer_entrant.xml layout for each item.</p>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Bhuvnesh Batta
 * @version 2.0
 * @since 2025-11-25
 */
public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    /**
     * The list of entrants to display in the RecyclerView.
     */
    private List<EntrantInfo> entrantList;

    /**
     * The current active tab, determines how entrant items are displayed.
     * Valid values: "waiting", "selected", "accepted", "cancelled"
     */
    private String currentTab = "waiting"; // Track which tab we're on

    /**
     * Listener for handling cancel button clicks.
     */
    private OnCancelClickListener cancelClickListener;

    /**
     * Callback interface for handling cancel button clicks.
     *
     * <p>Implementing classes should handle the business logic for cancelling
     * entrants from the event.</p>
     */
    public interface OnCancelClickListener {
        /**
         * Called when the organizer clicks to cancel an entrant.
         *
         * @param entrantInfo the entrant to be cancelled
         * @param position the position of the entrant in the list
         */
        void onCancelClick(EntrantInfo entrantInfo, int position);
    }

    /**
     * Constructs a new EntrantAdapter with the specified list of entrants.
     *
     * @param entrantList the initial list of entrants to display
     */
    public EntrantAdapter(List<EntrantInfo> entrantList) {
        this.entrantList = entrantList;
    }

    /**
     * Sets the current active tab to determine how items should be displayed.
     *
     * <p>Calling this method triggers a full refresh of all items in the RecyclerView
     * to update their display based on the new tab context.</p>
     *
     * @param tab the current tab ("waiting", "selected", "accepted", "cancelled")
     */
    public void setCurrentTab(String tab) {
        this.currentTab = tab;
        notifyDataSetChanged(); // Refresh all items to show correct content
    }

    /**
     * Sets the listener for cancel button click events.
     *
     * @param listener the listener to handle cancel clicks
     */
    public void setCancelClickListener(OnCancelClickListener listener) {
        this.cancelClickListener = listener;
    }

    /**
     * Creates a new ViewHolder by inflating the organizer entrant item layout.
     *
     * @param parent the ViewGroup into which the new View will be added
     * @param viewType the view type of the new View
     * @return a new EntrantViewHolder that holds the inflated View
     */
    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_organizer_entrant, parent, false);
        return new EntrantViewHolder(view);
    }

    /**
     * Binds entrant data to the ViewHolder at the specified position.
     *
     * @param holder the ViewHolder to bind data to
     * @param position the position of the item in the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        EntrantInfo entrantInfo = entrantList.get(position);
        holder.bind(entrantInfo, currentTab, position, cancelClickListener);
    }

    /**
     * Returns the total number of entrants in the adapter.
     *
     * @return the size of the entrant list, or 0 if the list is null
     */
    @Override
    public int getItemCount() {
        return entrantList != null ? entrantList.size() : 0;
    }

    /**
     * Updates the adapter with a new list of entrants and refreshes the display.
     *
     * @param newEntrants the new list of entrants to display
     */
    public void updateList(List<EntrantInfo> newEntrants) {
        this.entrantList = newEntrants;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for individual entrant items in the organizer's entrant list.
     *
     * <p>Displays entrant name and status-specific information based on the current
     * tab. Conditionally shows cancel button for entrant management.</p>
     */
    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView statusTextView;
        private final Button cancelButton;

        /**
         * Constructs a new EntrantViewHolder and initializes all view references.
         *
         * @param itemView the root view of the entrant item layout
         */
        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.entrantName);
            statusTextView = itemView.findViewById(R.id.entrantStatusText);
            cancelButton = itemView.findViewById(R.id.btnCancelEntrant);
        }

        /**
         * Binds entrant data to the view components based on the current tab context.
         *
         * <p>This method adjusts the display based on the current tab:</p>
         * <ul>
         *     <li>Waiting tab: Shows "Joined [date]", hides cancel button</li>
         *     <li>Selected tab: Shows "Pending Response", shows cancel button</li>
         *     <li>Accepted tab: Shows "Accepted", shows cancel button</li>
         *     <li>Cancelled tab: Shows "Cancelled", hides cancel button</li>
         * </ul>
         *
         * <p>If the joined date is null in the waiting tab, displays "Joined Recently"
         * as a fallback.</p>
         *
         * @param entrantInfo the entrant information to display
         * @param currentTab the current active tab
         * @param position the position in the list
         * @param cancelClickListener the listener for cancel button clicks
         */
        public void bind(EntrantInfo entrantInfo, String currentTab, int position, OnCancelClickListener cancelClickListener) {
            // Set name
            nameTextView.setText(entrantInfo.getName());

            // Set status text and cancel button visibility based on tab
            switch (currentTab) {
                case "waiting":
                    // Waiting tab: Show "Joined [date]", hide cancel button
                    if (entrantInfo.getJoinedDate() != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                        String formattedDate = "Joined " + dateFormat.format(entrantInfo.getJoinedDate());
                        statusTextView.setText(formattedDate);
                    } else {
                        statusTextView.setText("Joined Recently");
                    }
                    cancelButton.setVisibility(View.GONE);
                    break;

                case "selected":
                    // Selected tab: Show "Pending Response", show cancel button
                    statusTextView.setText("Pending Response");
                    cancelButton.setVisibility(View.VISIBLE);
                    break;

                case "accepted":
                    // Accepted tab: Show "Accepted", show cancel button
                    statusTextView.setText("Accepted");
                    cancelButton.setVisibility(View.VISIBLE);
                    break;

                case "cancelled":
                    // Cancelled tab: Show "Cancelled", hide cancel button
                    statusTextView.setText("Cancelled");
                    cancelButton.setVisibility(View.GONE);
                    break;

                default:
                    statusTextView.setText("");
                    cancelButton.setVisibility(View.GONE);
                    break;
            }

            // Set cancel button click listener
            if (cancelClickListener != null) {
                cancelButton.setOnClickListener(v -> {
                    cancelClickListener.onCancelClick(entrantInfo, position);
                });
            }
        }
    }
}