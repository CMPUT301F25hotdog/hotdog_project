package com.hotdog.elotto.ui.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.AdminEventAdapter;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.helpers.UserType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Admin Browse Events Activity.
 * Implements US 03.04.01 (Browse events) and US 03.01.01 (Remove events).
 *
 * @author Admin Module
 * @version 1.0
 */
public class AdminBrowseEventsActivity extends AppCompatActivity implements AdminEventAdapter.OnEventActionListener {

    // Device ID check disabled for testing
    // private static final String ADMIN_DEVICE_ID = "ded8763e1984cbfc";

    private RecyclerView recyclerViewEvents;
    private EditText etSearchEvents;
    private TextView tvTotalEvents, tvNoEvents;
    private ProgressBar progressBar;

    private AdminEventAdapter adapter;
    private EventRepository eventRepository;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for Admin Access
        // Styled as a callback to conform to the new User creation style
        User currentUser = new User(this, new Consumer<User>() {
            @Override
            public void accept(User user) {
                if (user.getType() != UserType.Administrator) {
                    Toast.makeText(getApplicationContext(), "Access Denied: Admin only", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                setContentView(R.layout.activity_admin_browse_events);

                initializeViews();
                setupRecyclerView();
                setupSearch();
                loadEvents();
            }
        });

        // DEVICE ID CHECK REMOVED FOR TESTING
        // String deviceId = Settings.Secure.getString(getContentResolver(),
        // Settings.Secure.ANDROID_ID);
        // if (!ADMIN_DEVICE_ID.equals(deviceId)) {
        // Toast.makeText(this, "Unauthorized access", Toast.LENGTH_SHORT).show();
        // finish();
        // return;
        // }
    }

    private void initializeViews() {
        recyclerViewEvents = findViewById(R.id.rv_admin_events);
        etSearchEvents = findViewById(R.id.et_search_events);
        tvTotalEvents = findViewById(R.id.tv_total_events);
        tvNoEvents = findViewById(R.id.tv_no_events);
        progressBar = findViewById(R.id.progress_bar);

        eventRepository = new EventRepository();
    }

    private void setupRecyclerView() {
        adapter = new AdminEventAdapter(filteredEvents, this);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearchEvents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoEvents.setVisibility(View.GONE);

        eventRepository.getAllEvents(new com.hotdog.elotto.callback.FirestoreListCallback<Event>() {
            @Override
            public void onSuccess(List<Event> events) {
                progressBar.setVisibility(View.GONE);
                allEvents.clear();
                allEvents.addAll(events);
                filteredEvents.clear();
                filteredEvents.addAll(events);

                updateUI();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                tvNoEvents.setVisibility(View.VISIBLE);
                tvNoEvents.setText("Error loading events: " + errorMessage);
                Toast.makeText(AdminBrowseEventsActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterEvents(String query) {
        filteredEvents.clear();

        if (query.isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Event event : allEvents) {
                // Null-safe checks for all searchable fields
                String name = event.getName() != null ? event.getName().toLowerCase() : "";
                String description = event.getDescription() != null ? event.getDescription().toLowerCase() : "";
                String location = event.getLocation() != null ? event.getLocation().toLowerCase() : "";

                if (name.contains(lowerQuery) ||
                        description.contains(lowerQuery) ||
                        location.contains(lowerQuery)) {
                    filteredEvents.add(event);
                }
            }
        }

        updateUI();
    }

    private void updateUI() {
        tvTotalEvents.setText("Total Events: " + allEvents.size());

        if (filteredEvents.isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
            tvNoEvents.setText(allEvents.isEmpty() ? "No events found" : "No matching events");
        } else {
            tvNoEvents.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onViewDetails(Event event) {
        // Show event details dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(event.getName());

        StringBuilder details = new StringBuilder();
        details.append("Description: ").append(event.getDescription() != null ? event.getDescription() : "N/A")
                .append("\n\n");
        details.append("Location: ").append(event.getLocation() != null ? event.getLocation() : "N/A").append("\n\n");
        details.append("Organizer ID: ").append(event.getOrganizerId()).append("\n\n");
        details.append("Max Entrants: ").append(event.getMaxEntrants()).append("\n\n");
        details.append("Status: ").append(event.getStatus()).append("\n\n");
        details.append("Waitlist Count: ").append(event.getCurrentWaitlistCount()).append("\n\n");
        details.append("Accepted Count: ").append(event.getCurrentAcceptedCount());

        builder.setMessage(details.toString());
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    @Override
    public void onDeleteEvent(Event event) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage(
                        "Are you sure you want to delete \"" + event.getName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent(Event event) {
        progressBar.setVisibility(View.VISIBLE);

        eventRepository.deleteEvent(event.getId(), new com.hotdog.elotto.callback.OperationCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                allEvents.remove(event);
                filteredEvents.remove(event);
                updateUI();
                Toast.makeText(AdminBrowseEventsActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminBrowseEventsActivity.this, "Failed to delete event: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}