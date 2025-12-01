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
 * Activity for administrators to browse and manage events in the system.
 *
 * <p>This activity provides administrators with the ability to view all events,
 * search/filter events by name, description, or location, view detailed event
 * information, and delete events from the system. Access is restricted to users
 * with Administrator privileges.</p>
 *
 * <p>Features include:</p>
 * <ul>
 *     <li>Real-time search filtering across event name, description, and location</li>
 *     <li>Event details dialog showing comprehensive event information</li>
 *     <li>Event deletion with confirmation dialog</li>
 *     <li>Total event count display</li>
 *     <li>Loading indicators and empty state handling</li>
 * </ul>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Admin Module
 * @version 1.0
 */
public class AdminBrowseEventsActivity extends AppCompatActivity implements AdminEventAdapter.OnEventActionListener {

    // Device ID check disabled for testing
    // private static final String ADMIN_DEVICE_ID = "ded8763e1984cbfc";

    /**
     * RecyclerView for displaying the list of events.
     */
    private RecyclerView recyclerViewEvents;

    /**
     * EditText for search/filter input.
     */
    private EditText etSearchEvents;

    /**
     * TextView displaying the total number of events.
     */
    private TextView tvTotalEvents;

    /**
     * TextView displayed when no events are found or match the search query.
     */
    private TextView tvNoEvents;

    /**
     * ProgressBar shown during loading operations.
     */
    private ProgressBar progressBar;

    /**
     * Adapter for binding event data to the RecyclerView.
     */
    private AdminEventAdapter adapter;

    /**
     * Repository for event data access operations.
     */
    private EventRepository eventRepository;

    /**
     * Complete list of all events loaded from Firestore.
     */
    private List<Event> allEvents = new ArrayList<>();

    /**
     * Filtered list of events based on search query.
     */
    private List<Event> filteredEvents = new ArrayList<>();

    /**
     * Called when the activity is starting.
     *
     * <p>Verifies that the current user has Administrator privileges before
     * initializing the activity. If access is denied, displays a toast message
     * and finishes the activity.</p>
     *
     * @param savedInstanceState the saved instance state Bundle
     */
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

    /**
     * Initializes all view components and repositories.
     *
     * <p>Binds UI elements by their IDs and creates a new EventRepository instance
     * for data access operations.</p>
     */
    private void initializeViews() {
        recyclerViewEvents = findViewById(R.id.rv_admin_events);
        etSearchEvents = findViewById(R.id.et_search_events);
        tvTotalEvents = findViewById(R.id.tv_total_events);
        tvNoEvents = findViewById(R.id.tv_no_events);
        progressBar = findViewById(R.id.progress_bar);

        eventRepository = new EventRepository();
    }

    /**
     * Sets up the RecyclerView with adapter and layout manager.
     *
     * <p>Creates an AdminEventAdapter with the filtered events list and sets this
     * activity as the action listener for handling view and delete operations.</p>
     */
    private void setupRecyclerView() {
        adapter = new AdminEventAdapter(filteredEvents, this);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(adapter);
    }

    /**
     * Sets up the search functionality with real-time text filtering.
     *
     * <p>Adds a TextWatcher to the search EditText that filters events as the
     * user types, providing instant search results.</p>
     */
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

    /**
     * Loads all events from Firestore.
     *
     * <p>Shows a progress bar during loading and updates the UI with the loaded
     * events on success. Displays an error message if loading fails.</p>
     */
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

    /**
     * Filters events based on the search query.
     *
     * <p>Performs a case-insensitive search across event name, description, and
     * location fields. If the query is empty, displays all events. Updates the
     * UI with the filtered results.</p>
     *
     * @param query the search query string
     */
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

    /**
     * Updates the UI based on current event data.
     *
     * <p>Updates the total events count, shows/hides the "no events" message
     * appropriately, and notifies the adapter of data changes.</p>
     */
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

    /**
     * Handles the view details action for an event.
     *
     * <p>Displays an AlertDialog with comprehensive event information including
     * description, location, organizer ID, max entrants, status, waitlist count,
     * and accepted count.</p>
     *
     * @param event the event to display details for
     */
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

    /**
     * Handles the delete event action.
     *
     * <p>Shows a confirmation dialog before proceeding with deletion to prevent
     * accidental deletions. The dialog warns that the action cannot be undone.</p>
     *
     * @param event the event to delete
     */
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

    /**
     * Deletes an event from Firestore after confirmation.
     *
     * <p>Shows a progress bar during deletion, removes the event from both the
     * all events and filtered events lists on success, and updates the UI. Displays
     * appropriate toast messages for success or failure.</p>
     *
     * @param event the event to delete
     */
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