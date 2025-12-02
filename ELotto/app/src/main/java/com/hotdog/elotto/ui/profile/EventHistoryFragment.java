package com.hotdog.elotto.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.EventAdapter;
import com.hotdog.elotto.adapter.EventHistoryAdapter;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.helpers.Status;
import com.hotdog.elotto.helpers.UserStatus;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fragment that displays the event history for the current user.
 * Shows events separated into "Drawn Events" (selected/accepted/declined)
 * and "Pending Events" (waiting/pending).
 *
 * Implements US 01.02.03: As an entrant, I want to have a history of events
 * I have registered for, whether I was selected or not.
 *
 * <p>View layer in MVC pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Layne Pitman
 * @version 1.0
 * @since 2025-11-23
 */
public class EventHistoryFragment extends Fragment {

    private static final String TAG = "EventHistoryFragment";

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView drawnEventsRecyclerView;
    private RecyclerView pendingEventsRecyclerView;
    private TextView drawnEventsEmptyText;
    private TextView pendingEventsEmptyText;

    // Adapters
    private EventAdapter drawnEventsAdapter;
    private EventAdapter pendingEventsAdapter;

    // Data
    private User currentUser;
    private EventRepository eventRepository;
    private List<Event> drawnEvents = new ArrayList<>();
    private List<Event> pendingEvents = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_history, container, false);

        initializeViews(view);

        currentUser = new User(requireContext(), (User user) -> {
            loadUserEventHistory(user);
            setupToolbar();
            setupRecyclerViews(user);
        });

        return view;
    }

    /**
     * Initialize all view components from the layout.
     *
     * @param view The root view of the fragment
     */
    private void initializeViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        drawnEventsRecyclerView = view.findViewById(R.id.drawnEventsRecyclerView);
        pendingEventsRecyclerView = view.findViewById(R.id.pendingEventsRecyclerView);
        drawnEventsEmptyText = view.findViewById(R.id.drawnEventsEmptyText);
        pendingEventsEmptyText = view.findViewById(R.id.pendingEventsEmptyText);

        eventRepository = new EventRepository();
    }

    /**
     * Setup the toolbar with back navigation.
     */
    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    /**
     * Setup both RecyclerViews with their adapters and layout managers.
     */
    private void setupRecyclerViews(User user) {
        // Setup Drawn Events RecyclerView
        drawnEventsAdapter = new EventAdapter(drawnEvents, user.getId());
        drawnEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        drawnEventsRecyclerView.setAdapter(drawnEventsAdapter);
        drawnEventsRecyclerView.setNestedScrollingEnabled(false);

        // Setup Pending Events RecyclerView
        pendingEventsAdapter = new EventAdapter(pendingEvents, user.getId());
        pendingEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingEventsRecyclerView.setAdapter(pendingEventsAdapter);
        pendingEventsRecyclerView.setNestedScrollingEnabled(false);

        EventAdapter.OnEventClickListener listener = new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
                // Check if user is registered and has Invited status
                Status userStatus = getUserStatusForEvent(event.getId(), user);

                Bundle bundle = new Bundle();
                bundle.putSerializable("event", event);
                NavController navController = NavHostFragment.findNavController(EventHistoryFragment.this);

                // If user is invited and invitation hasn't expired then go to accept/decline screen
                if (userStatus == Status.Selected) {
                    navController.navigate(R.id.action_eventHistoryFragment_to_acceptDeclineInvitation, bundle);
                } else {
                    // default to regular event details screen
                    navController.navigate(R.id.action_eventHistoryFragment_to_eventDetailsFragment, bundle);
                }
            }
        };

        // Set click listener for event cards
        drawnEventsAdapter.setOnEventClickListener(listener);
        pendingEventsAdapter.setOnEventClickListener(listener);
    }

    /**
     * Load the current user and their event history from Firebase.
     */
    private void loadUserEventHistory(User user) {
            if (user.exists()== UserStatus.Existent) {
                fetchUserEvents(user);
            } else {
                Log.e(TAG, "User does not exist");
                Toast.makeText(getContext(), "Unable to load event history", Toast.LENGTH_SHORT).show();
            }
    }

    /**
     * Fetch all events that the user has registered for from Firebase.
     */
    private void fetchUserEvents(User user) {
        List<String> registeredEventIds = user.getRegEventIds();

        if (registeredEventIds == null || registeredEventIds.isEmpty()) {
            showEmptyStates();
            return;
        }

        // Fetch events from Firebase
        eventRepository.getEventsById(registeredEventIds, new FirestoreCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                if (events != null && !events.isEmpty()) {
                    categorizeEvents(events, user);
                    updateUI();
                } else {
                    showEmptyStates();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error fetching events: " + errorMessage);
                Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                showEmptyStates();
            }
        });
    }

    /**
     * Categorize events into drawn and pending lists based on user's status.
     *
     * @param events List of all events the user is registered for
     */
    private void categorizeEvents(List<Event> events, User user) {
        drawnEvents.clear();
        pendingEvents.clear();

        for (Event event : events) {
            if(event == null) continue;
            Status userStatus = getUserStatusForEvent(event.getId(), user);

            if (userStatus != null) {
                // Categorize based on status
                switch (userStatus) {
                    case Selected:
                    case Accepted:
                    case Declined:
                        drawnEvents.add(event);
                        break;
                    case Pending:
                    case Waitlisted:
                    case Withdrawn:
                        pendingEvents.add(event);
                        break;
                }
            }
        }
    }

    /**
     * Get the user's status for a specific event.
     *
     * @param eventId The ID of the event
     * @return The user's Status for this event, or null if not found
     */
    private Status getUserStatusForEvent(String eventId, User user) {
        User.RegisteredEvent event = user.getSingleRegEvent(eventId);
        if(event!=null) return event.getStatus();

        return null;
    }

    /**
     * Update the UI with the categorized events and handle empty states.
     */
    private void updateUI() {
        // Update Drawn Events
        if(drawnEvents != null && drawnEventsAdapter != null) {
            if (drawnEvents.isEmpty()) {
                drawnEventsRecyclerView.setVisibility(View.GONE);
                drawnEventsEmptyText.setVisibility(View.VISIBLE);
            } else {
                drawnEventsRecyclerView.setVisibility(View.VISIBLE);
                drawnEventsEmptyText.setVisibility(View.GONE);
                drawnEventsAdapter.updateEvents(drawnEvents);
            }
        }

        // Update Pending Events
        if (pendingEvents != null && pendingEventsAdapter != null) {
            if (pendingEvents.isEmpty()) {
                pendingEventsRecyclerView.setVisibility(View.GONE);
                pendingEventsEmptyText.setVisibility(View.VISIBLE);
            } else {
                pendingEventsRecyclerView.setVisibility(View.VISIBLE);
                pendingEventsEmptyText.setVisibility(View.GONE);
                pendingEventsAdapter.updateEvents(pendingEvents);
            }
        }
    }

    /**
     * Show empty state messages when user has no events.
     */
    private void showEmptyStates() {
        drawnEventsRecyclerView.setVisibility(View.GONE);
        drawnEventsEmptyText.setVisibility(View.VISIBLE);
        pendingEventsRecyclerView.setVisibility(View.GONE);
        pendingEventsEmptyText.setVisibility(View.VISIBLE);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up references
        drawnEventsAdapter = null;
        pendingEventsAdapter = null;
    }
}