package com.hotdog.elotto.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Shows entrant initials, name, and joined date.
 *
 * <p>Uses the item_organizer_entrant.xml layout for each item.</p>
 *
 * <p>View layer in MVC pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author [Your Name]
 * @version 1.0
 * @since 2025-11-24
 */
public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    private List<EntrantInfo> entrantList;

    /**
     * Constructor for the adapter.
     *
     * @param entrantList the initial list of entrants to display
     */
    public EntrantAdapter(List<EntrantInfo> entrantList) {
        this.entrantList = entrantList;
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
        holder.bind(entrantInfo);
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
        private final TextView joinedDateTextView;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.entrantName);
            joinedDateTextView = itemView.findViewById(R.id.entrantJoinDate);
        }

        /**
         * Binds entrant data to the view.
         *
         * @param entrantInfo the entrant information to display
         */
        public void bind(EntrantInfo entrantInfo) {
            // Set initials

            // Set name
            nameTextView.setText(entrantInfo.getName());

            // Set joined date
            if (entrantInfo.getJoinedDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                String formattedDate = "Joined " + dateFormat.format(entrantInfo.getJoinedDate());
                joinedDateTextView.setText(formattedDate);
            } else {
                joinedDateTextView.setText("Joined Recently");
            }
        }

    }
}