package com.hotdog.elotto;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.AdminEventAdapter;
import com.hotdog.elotto.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Browse and manage events
 */
public class BrowseEventsActivity extends AppCompatActivity {

    private static final String TAG = "BrowseEvents";

    private ImageView btnBack;
    private EditText searchEvents;
    private TextView textTotalEventsCount;
    private RecyclerView recyclerEvents;
    private LinearLayout emptyState;

    private AdminEventAdapter eventAdapter;
    private List<Event> allEvents;
    private List<Event> filteredEvents;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        loadEvents();
        setupSearchListener();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        searchEvents = findViewById(R.id.search_events);
        textTotalEventsCount = findViewById(R.id.text_total_events_count);
        recyclerEvents = findViewById(R.id.recycler_events);
        emptyState = findViewById(R.id.empty_state);
    }

    private void setupRecyclerView() {
        allEvents = new ArrayList<>();
        filteredEvents = new ArrayList<>();

        eventAdapter = new AdminEventAdapter(this, filteredEvents, new AdminEventAdapter.OnEventActionListener() {
            @Override
            public void onViewEvent(Event event) {
                showEventDetails(event);
            }

            @Override
            public void onDeleteEvent(Event event) {
                showDeleteConfirmationDialog(event);
            }
        });

        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerEvents.setAdapter(eventAdapter);
    }

    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Event event = doc.toObject(Event.class);
                            allEvents.add(event);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing event: " + doc.getId(), e);
                        }
                    }
                    filteredEvents.clear();
                    filteredEvents.addAll(allEvents);
                    updateUI();
                    Log.d(TAG, "Loaded " + allEvents.size() + " events");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events", e);
                    Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    private void setupSearchListener() {
        searchEvents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterEvents(String query) {
        filteredEvents.clear();

        if (query.isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Event event : allEvents) {
                String name = event.getName() != null ? event.getName().toLowerCase() : "";
                String description = event.getDescription() != null ? event.getDescription().toLowerCase() : "";
                String location = event.getLocation() != null ? event.getLocation().toLowerCase() : "";

                if (name.contains(lowerCaseQuery) ||
                        description.contains(lowerCaseQuery) ||
                        location.contains(lowerCaseQuery)) {
                    filteredEvents.add(event);
                }
            }
        }

        updateUI();
    }

    private void updateUI() {
        textTotalEventsCount.setText("Total Events : " + filteredEvents.size());

        if (filteredEvents.isEmpty()) {
            recyclerEvents.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerEvents.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }

        eventAdapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void showEventDetails(Event event) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(event.getName()).append("\n\n");
        details.append("Organizer: ").append(event.getOrganizerName()).append("\n\n");
        details.append("Location: ").append(event.getLocation()).append("\n\n");
        details.append("Max Entrants: ").append(event.getMaxEntrants()).append("\n");
        details.append("Waitlist: ").append(event.getCurrentWaitlistCount()).append("\n");
        details.append("Accepted: ").append(event.getCurrentAcceptedCount()).append("\n");

        new AlertDialog.Builder(this)
                .setTitle("Event Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDeleteConfirmationDialog(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete \"" + event.getName() + "\"?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent(Event event) {
        String eventId = event.getId();
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Cannot delete: Invalid event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                    loadEvents();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting event", e);
                    Toast.makeText(this, "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }
}