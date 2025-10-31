package com.hotdog.elotto.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.EventAdapter;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment displays a list of all available events that users can browse and join.
 * Implements US 01.01.03 - As an entrant, I want to be able to see a list of events
 * that I can join the waiting list for.
 *
 * serves as the View layer of MVC design
 *
 * Outstanding Issues:
 * Filter functionality not yet implemented
 * Profile navigation not yet implemented
 */
public class HomeFragment extends Fragment {

    private RecyclerView eventsRecyclerView;
    private EventAdapter eventAdapter;
    private ProgressBar loadingProgressBar;
    private View emptyStateLayout;
    private SearchView searchView;
    private ImageButton profileButton;
    private ImageButton filterButton;

    private EventRepository eventRepository;
    private List<Event> allEvents;
    private String currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize repository
        eventRepository = new EventRepository();

        // TODO: Get actual user ID from authentication system
        // For now, using a placeholder
        currentUserId = "user123";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        initializeViews(view);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup listeners
        setupListeners();

        // Load events from Firebase
        loadEvents();

        return view;
    }

    private void initializeViews(View view) {
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        searchView = view.findViewById(R.id.searchView);
        profileButton = view.findViewById(R.id.profileButton);
        filterButton = view.findViewById(R.id.filterButton);

        allEvents = new ArrayList<>();
    }

    private void setupRecyclerView() {
        // Set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        eventsRecyclerView.setLayoutManager(layoutManager);

        // Initialize adapter
        eventAdapter = new EventAdapter(allEvents, currentUserId);
        eventsRecyclerView.setAdapter(eventAdapter);

        // Set click listener for event cards
        eventAdapter.setOnEventClickListener(new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
                // TODO: Navigate to event details screen
                Toast.makeText(getContext(), "Clicked: " + event.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // Profile button click
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Navigate to profile screen
                Toast.makeText(getContext(), "Profile clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // Filter button click
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Show filter dialog
                Toast.makeText(getContext(), "Filter clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEvents(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText);
                return true;
            }
        });
    }

    private void loadEvents() {
        // Show loading indicator
        showLoading(true);

        // Fetch events from Firebase
        eventRepository.getAllEvents(new FirestoreListCallback<Event>() {
            @Override
            public void onSuccess(List<Event> events) {
                showLoading(false);
                allEvents.clear();
                allEvents.addAll(events);
                eventAdapter.updateEvents(allEvents);

                // Show empty state if no events
                if (events.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(getContext(), "Error loading events: " + error, Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }

    private void filterEvents(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Show all events
            eventAdapter.updateEvents(allEvents);
            return;
        }

        // Filter events by name or location
        List<Event> filteredEvents = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        for (Event event : allEvents) {
            if (event.getName().toLowerCase().contains(lowerCaseQuery) ||
                    event.getLocation().toLowerCase().contains(lowerCaseQuery)) {
                filteredEvents.add(event);
            }
        }

        eventAdapter.updateEvents(filteredEvents);

        // Show empty state if no results
        showEmptyState(filteredEvents.isEmpty());
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingProgressBar.setVisibility(View.VISIBLE);
            eventsRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.GONE);
        } else {
            loadingProgressBar.setVisibility(View.GONE);
            eventsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState(boolean show) {
        if (show) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            eventsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            eventsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}