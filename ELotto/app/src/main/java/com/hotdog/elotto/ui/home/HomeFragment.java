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
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.NavController;

import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.EventAdapter;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.repository.EventRepository;

import android.view.MenuInflater;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment displays a list of all available events that users can browse and join.
 * Implements US 01.01.03 - As an entrant, I want to be able to see a list of events
 * that I can join the waiting list for.
 *
 * Serves as the View layer of MVC design.
 *
 * Outstanding Issues:
 * Filter functionality not yet implemented.
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
        eventRepository = new EventRepository();
        currentUserId = "user123"; // temporary placeholder until auth
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupListeners();
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        eventsRecyclerView.setLayoutManager(layoutManager);

        eventAdapter = new EventAdapter(allEvents, currentUserId);
        eventsRecyclerView.setAdapter(eventAdapter);

        eventAdapter.setOnEventClickListener(event ->
                Toast.makeText(getContext(), "Clicked: " + event.getName(), Toast.LENGTH_SHORT).show()
        );
    }

    private void setupListeners() {
        // Dropdown menu on profile button
        profileButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.profile_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                NavController navController = NavHostFragment.findNavController(HomeFragment.this);

                if (id == R.id.action_profile) {
                    navController.navigate(R.id.action_navigation_home_to_profileFragment);
                    return true;

                } else if (id == R.id.action_inbox) {
                    Toast.makeText(requireContext(), "Inbox clicked", Toast.LENGTH_SHORT).show();
                    return true;

                } else if (id == R.id.action_settings) {
                    navController.navigate(R.id.action_navigation_home_to_settingsFragment);
                    return true;

                } else if (id == R.id.action_faq) {
                    navController.navigate(R.id.action_navigation_home_to_faqFragment);
                    return true;

                } else if (id == R.id.action_qr) {
                    Toast.makeText(requireContext(), "Scan QR clicked", Toast.LENGTH_SHORT).show();
                    return true;

                } else {
                    return false;
                }
            });

            popupMenu.show();
        });

        // Filter button (currently not implemented)
        filterButton.setOnClickListener(v ->
                Toast.makeText(getContext(), "Filter clicked", Toast.LENGTH_SHORT).show()
        );

        // Search bar logic
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
        showLoading(true);

        eventRepository.getAllEvents(new FirestoreListCallback<Event>() {
            @Override
            public void onSuccess(List<Event> events) {
                showLoading(false);
                allEvents.clear();
                allEvents.addAll(events);
                eventAdapter.updateEvents(allEvents);
                showEmptyState(events.isEmpty());
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
            eventAdapter.updateEvents(allEvents);
            showEmptyState(allEvents.isEmpty());
            return;
        }

        List<Event> filteredEvents = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        for (Event event : allEvents) {
            if (event.getName().toLowerCase().contains(lowerCaseQuery) ||
                    event.getLocation().toLowerCase().contains(lowerCaseQuery)) {
                filteredEvents.add(event);
            }
        }

        eventAdapter.updateEvents(filteredEvents);
        showEmptyState(filteredEvents.isEmpty());
    }

    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        eventsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean show) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        eventsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
