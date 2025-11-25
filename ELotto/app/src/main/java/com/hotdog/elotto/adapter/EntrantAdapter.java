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
 * Adapter for displaying entrant information in a RecyclerView.
 * Shows entrant name, status text, and cancel button (when applicable).
 *
 * <p>Uses the item_organizer_entrant.xml layout for each item.</p>
 *
 * <p>Displays different content based on which tab is active (waiting, selected, accepted, cancelled).</p>
 *
 * <p>View layer in MVC pattern.</p>
 *
 * @author [Your Name]
 * @version 2.0
 * @since 2025-11-25
 */
public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    private List<EntrantInfo> entrantList;
    private String currentTab = "waiting"; // Track which tab we're on
    private OnCancelClickListener cancelClickListener;

    /**
     * Interface for cancel button clicks.
     */
    public interface OnCancelClickListener {
        void onCancelClick(EntrantInfo entrantInfo, int position);
    }

    /**
     * Constructor for the adapter.
     *
     * @param entrantList the initial list of entrants to display
     */
    public EntrantAdapter(List<EntrantInfo> entrantList) {
        this.entrantList = entrantList;
    }

    /**
     * Sets the current tab to determine how items should be displayed.
     *
     * @param tab the current tab ("waiting", "selected", "accepted", "cancelled")
     */
    public void setCurrentTab(String tab) {
        this.currentTab = tab;
        notifyDataSetChanged(); // Refresh all items to show correct content
    }

    /**
     * Sets the listener for cancel button clicks.
     *
     * @param listener the listener to handle cancel clicks
     */
    public void setCancelClickListener(OnCancelClickListener listener) {
        this.cancelClickListener = listener;
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_organizer_entrant, parent, false);
        return new EntrantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        EntrantInfo entrantInfo = entrantList.get(position);
        holder.bind(entrantInfo, currentTab, position, cancelClickListener);
    }

    @Override
    public int getItemCount() {
        return entrantList != null ? entrantList.size() : 0;
    }

    /**
     * Updates the adapter with a new list of entrants.
     *
     * @param newEntrants the new list of entrants
     */
    public void updateList(List<EntrantInfo> newEntrants) {
        this.entrantList = newEntrants;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for entrant items.
     */
    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView statusTextView;
        private final Button cancelButton;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.entrantName);
            statusTextView = itemView.findViewById(R.id.entrantStatusText);
            cancelButton = itemView.findViewById(R.id.btnCancelEntrant);
        }

        /**
         * Binds entrant data to the view.
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