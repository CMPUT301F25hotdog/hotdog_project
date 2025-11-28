package com.hotdog.elotto.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.databinding.FragmentInboxBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Inbox screen for notifications, using the existing event_card.xml layout.
 */
public class NotificationsFragment extends Fragment {

    private FragmentInboxBinding binding;
    private NotificationsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // NOTE: layout file is fragment_inbox.xml -> FragmentInboxBinding
        binding = FragmentInboxBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Back button
        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // RecyclerView setup
        RecyclerView recyclerView = binding.recyclerNotifications;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationsAdapter();
        recyclerView.setAdapter(adapter);

        // Dummy data for now – matches the mockup visually
        List<NotificationItem> sample = new ArrayList<>();
        sample.add(new NotificationItem(
                "Mountain Biking",
                "May 25, 9:00",
                "Tewarting, QLD",
                "Selected",
                true
        ));
        sample.add(new NotificationItem(
                "Horseback Riding",
                "June 6, 11:00",
                "Arkhangai, MN",
                "Waitlisted",
                true
        ));
        sample.add(new NotificationItem(
                "Skydiving",
                "June 1, 13:00",
                "Byron Bay, AU",
                "Selected",
                false
        ));

        adapter.setItems(sample);
        updateUnreadCount(sample);

        return root;
    }

    private void updateUnreadCount(List<NotificationItem> list) {
        int unread = 0;
        for (NotificationItem n : list) {
            if (n.isNew) unread++;
        }
        TextView unreadText = binding.textUnreadCount;
        unreadText.setText(unread + " unread notifications");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ---------- Small model + adapter inside this file ----------

    /** Simple model for inbox items */
    static class NotificationItem {
        String title;
        String dateTime;
        String location;
        String status;   // Selected / Waitlisted / Not selected
        boolean isNew;

        NotificationItem(String title, String dateTime,
                         String location, String status,
                         boolean isNew) {
            this.title = title;
            this.dateTime = dateTime;
            this.location = location;
            this.status = status;
            this.isNew = isNew;
        }
    }

    /** Adapter that reuses event_card.xml for each inbox row */
    static class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

        private final List<NotificationItem> items = new ArrayList<>();

        void setItems(List<NotificationItem> newItems) {
            items.clear();
            if (newItems != null) items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.event_card, parent, false);   // reuse event_card.xml
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            TextView eventNameTextView;
            TextView eventDateTextView;
            TextView eventLocationTextView;
            TextView eventStatusTextView;
            TextView eventEntryCountTextView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
                eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
                eventLocationTextView = itemView.findViewById(R.id.eventLocationTextView);
                eventStatusTextView = itemView.findViewById(R.id.eventStatusTextView);
                eventEntryCountTextView = itemView.findViewById(R.id.eventEntryCountTextView);
            }

            void bind(NotificationItem n) {
                eventNameTextView.setText(n.title);
                eventDateTextView.setText(n.dateTime);
                eventLocationTextView.setText(n.location);

                // For inbox we don't really care about entry count; hide it
                eventEntryCountTextView.setVisibility(View.GONE);

                // Status badge text
                eventStatusTextView.setText(n.status);
                // You already have status_badge_background on this TextView in XML,
                // so colors will come from that drawable / tint.
            }
        }
    }
}
