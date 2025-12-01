package com.hotdog.elotto.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.EventAdapter;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.EventRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.hotdog.elotto.helpers.Status;

/**
 * Fragment responsible for displaying the main event list on the home screen.
 *
 * <p>This component loads events from Firestore via {@link EventRepository}, displays
 * them in a {@link RecyclerView}, and provides search and filter capabilities.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *     <li>Displaying a scrollable list of events using {@link EventAdapter}</li>
 *     <li>Text-based search by event name or location</li>
 *     <li>Filtering by interests (tags) and date ranges through a filter dialog</li>
 *     <li>Conditional navigation to either event details or invitation response screen
 *         based on the user's registration status</li>
 *     <li>Profile menu for quick navigation to profile, inbox, settings, FAQ, and QR scanning</li>
 * </ul>
 *
 * <p>This fragment primarily serves as the View layer in an MVC-style design.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently.</p>
 *
 * @version 1.0
 * @since 2025-11-01
 */
public class HomeFragment extends Fragment {

    /**
     * RecyclerView used to display the list of events.
     */
    private RecyclerView eventsRecyclerView;

    /**
     * Adapter providing event data and view binding for the {@link #eventsRecyclerView}.
     */
    private EventAdapter eventAdapter;

    /**
     * Progress bar shown while events are being loaded from Firestore.
     */
    private ProgressBar loadingProgressBar;

    /**
     * View shown when there are no events to display or loading fails.
     */
    private View emptyStateLayout;

    /**
     * SearchView used to filter events by text query (name or location).
     */
    private SearchView searchView;

    /**
     * Button that opens a profile-related popup menu with navigation options.
     */
    private ImageButton profileButton;

    /**
     * Button that opens the filter dialog for interest and date filtering.
     */
    private ImageButton filterButton;

    /**
     * Repository used to retrieve event data from Firestore.
     */
    private EventRepository eventRepository;

    /**
     * In-memory list of all events retrieved from the repository.
     */
    private List<Event> allEvents;

    /**
     * ID of the currently active user.
     */
    private String currentUserId;

    /**
     * Model representing the current user, including registered event metadata.
     */
    private User currentUser;

    /**
     * Currently selected interest tags used for filtering the event list.
     */
    private Set<String> currentSelectedTags = new HashSet<>();

    /**
     * Currently selected date filter used for filtering the event list.
     */
    private DateFilter currentDateFilter = DateFilter.ALL_DATES;

    /**
     * Called when the fragment is being created.
     *
     * <p>Initializes the event repository and constructs the current user model
     * using the host context.</p>
     *
     * @param savedInstanceState previously saved state, if available
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize repository
        eventRepository = new EventRepository();

        currentUser = new User(requireContext());
        currentUserId = currentUser.getId();
    }

    /**
     * Inflates the layout for the home screen and initializes the UI and data loading.
     *
     * @param inflater LayoutInflater used to inflate the fragment's layout
     * @param container parent view that the fragment's UI will be attached to
     * @param savedInstanceState previously saved state, if available
     * @return the root view of the home fragment layout
     */
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

    /**
     * Binds UI elements from the layout to their corresponding fields and initializes
     * the backing event list.
     *
     * @param view the root fragment view
     */
    private void initializeViews(View view) {
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        searchView = view.findViewById(R.id.searchView);
        profileButton = view.findViewById(R.id.profileButton);
        filterButton = view.findViewById(R.id.filterButton);
        allEvents = new ArrayList<>();
    }

    /**
     * Configures the RecyclerView, attaches the {@link EventAdapter}, and sets up
     * the event click behavior to navigate to the appropriate detail or invitation screen.
     */
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        eventsRecyclerView.setLayoutManager(layoutManager);

        eventAdapter = new EventAdapter(allEvents, currentUserId);
        eventsRecyclerView.setAdapter(eventAdapter);

        // Set click listener for event cards
        eventAdapter.setOnEventClickListener(new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
                // Check if user is registered and has Invited status
                Status userStatus = getUserStatusForEvent(event.getId());

                Bundle bundle = new Bundle();
                bundle.putSerializable("event", event);
                NavController navController = NavHostFragment.findNavController(HomeFragment.this);

                // If user is invited and invitation hasn't expired then go to accept/decline screen
                if (userStatus == Status.Selected && !isInvitationExpired(event.getId())) {
                    navController.navigate(R.id.action_navigation_home_to_acceptDeclineInvitation, bundle);
                } else {
                    // default to regular event details screen
                    navController.navigate(R.id.action_navigation_home_to_eventDetails, bundle);
                }
            }
        });
    }

    /**
     * Retrieves the current user's status for a specific event.
     *
     * @param eventId the unique identifier of the event
     * @return the {@link Status} of the user for the given event, or {@code null}
     *         if the user is not registered or user data is unavailable
     */
    private Status getUserStatusForEvent(String eventId) {
        if (currentUser == null) {
            return null;
        }

        User.RegisteredEvent event = currentUser.getSingleRegEvent(eventId);
        if (event == null) {
            return null;
        }
        return event.getStatus();
    }

    /**
     * Determines whether the invitation for a given event has expired for the current user.
     *
     * <p>The invitation is considered expired if more than 24 hours have elapsed since the
     * {@code selectedDate} timestamp recorded for the event.</p>
     *
     * @param eventId the unique identifier of the event
     * @return {@code true} if the invitation has expired or user data is unavailable;
     *         {@code false} if the invitation is still valid or not applicable
     */
    private boolean isInvitationExpired(String eventId) {
        if (currentUser == null) {
            return true;
        }

        List<User.RegisteredEvent> regEvents = currentUser.getRegEvents();

        if (regEvents != null) {
            User.RegisteredEvent regEvent = currentUser.getSingleRegEvent(eventId);
            if (regEvent == null) return false;
            com.google.firebase.Timestamp selectedDate = regEvent.getSelectedDate();

            if (selectedDate != null) {
                long deadlineMillis = selectedDate.toDate().getTime() +
                        java.util.concurrent.TimeUnit.HOURS.toMillis(24);
                long currentMillis = System.currentTimeMillis();

                return currentMillis > deadlineMillis;
            }
        }


        return false;
    }

    /**
     * Attaches listeners to toolbar buttons and search view.
     *
     * <p>Includes:</p>
     * <ul>
     *     <li>Profile button popup menu navigation</li>
     *     <li>Filter dialog launch behavior</li>
     *     <li>SearchView query handling for live text filtering</li>
     * </ul>
     */
    private void setupListeners() {
        // Profile button dropdown menu
        profileButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.profile_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_profile) {
                    NavController navController = NavHostFragment.findNavController(HomeFragment.this);
                    navController.navigate(R.id.action_navigation_home_to_profileFragment);
                    return true;
                } else if (id == R.id.action_inbox) {
                    NavHostFragment.findNavController(HomeFragment.this)
                            .navigate(R.id.action_navigation_home_to_notificationsFragment);
                    return true;
                } else if (id == R.id.action_settings) {
                    NavHostFragment.findNavController(HomeFragment.this)
                            .navigate(R.id.action_navigation_home_to_settingsFragment);
                    return true;

                } else if (id == R.id.action_faq) {
                    NavHostFragment.findNavController(HomeFragment.this)
                            .navigate(R.id.action_navigation_home_to_faqFragment);
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

        // Filter button
        filterButton.setOnClickListener(v -> openFilterDialog());

        // Search bar functionality
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

    /**
     * Opens the filter dialog allowing the user to select interest tags and a date range,
     * applies the currently active filters as initial state, and receives updated filters
     * via a callback.
     */
    private void openFilterDialog(){
        FilterDialogFragment dialog = FilterDialogFragment.newInstance();

        dialog.setCurrentFilters(currentSelectedTags, currentDateFilter);
        dialog.setOnFilterAppliedListener((selectedTags, dateFilter) -> {
            applyFilters(selectedTags, dateFilter);
            String message = eventAdapter.getItemCount() + " event(s) found";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
        dialog.show(getParentFragmentManager(), "filter_dialog");
    }

    /**
     * Applies the specified interest tags and date filter to {@link #allEvents} and updates
     * the adapter with the filtered result set.
     *
     * @param selectedTags the set of selected interest tags
     * @param dateFilter   the {@link DateFilter} specifying the desired date range
     */
    private void applyFilters(Set<String> selectedTags, DateFilter dateFilter) {
        this.currentSelectedTags = new HashSet<>(selectedTags);
        this.currentDateFilter = dateFilter;

        if (allEvents == null || allEvents.isEmpty()) {
            return;
        }

        List<Event> filteredEvents = new ArrayList<>();
        for (Event event : allEvents) {
            boolean matchesTags = selectedTags.isEmpty() || matchesAnyTag(event, selectedTags);
            boolean matchesDate = dateFilter.matchesFilter(event.getEventDateTime());

            if (matchesTags && matchesDate) {
                filteredEvents.add(event);
            }
        }
        // Update the RecyclerView with filtered events
        eventAdapter.updateEvents(filteredEvents);
        // Show empty state if no results
        showEmptyState(filteredEvents.isEmpty());
    }

    /**
     * Checks whether the given event matches at least one of the selected interest tags.
     *
     * @param event        the event to evaluate
     * @param selectedTags the set of tags used for filtering
     * @return {@code true} if the event contains any of the selected tags; {@code false} otherwise
     */
    private boolean matchesAnyTag(Event event, Set<String> selectedTags) {
        ArrayList<String> eventTags = event.getTagList();

        if (eventTags == null || eventTags.isEmpty()) {
            return false;
        }

        //loop and check if the selected tags are in the arrayList of eventTags
        for (String tag : selectedTags) {
            if (eventTags.contains(tag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Clears all active filters, restores the full event list, and updates the empty state.
     * Displays a toast message to confirm that filters have been cleared.
     */
    private void clearFilters() {
        eventAdapter.updateEvents(allEvents);
        showEmptyState(allEvents.isEmpty());
        Toast.makeText(getContext(), "Filters cleared", Toast.LENGTH_SHORT).show();
    }

    /**
     * Loads all events from the repository, updates the adapter, and applies any
     * previously selected filters once loading completes.
     *
     * <p>Displays a loading indicator while the network/database call is in progress
     * and toggles an empty state view if no events are returned.</p>
     */
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

                applyFilters(currentSelectedTags, currentDateFilter);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(getContext(), "Error loading events: " + error, Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }

    /**
     * Filters the list of events based on a free-text query, matching against
     * the event name and location fields.
     *
     * @param query the search text entered by the user; if empty, restores the full list
     */
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

    /**
     * Toggles visibility of the loading indicator and hides or shows the main content
     * views as appropriate.
     *
     * @param show {@code true} to show the loading indicator; {@code false} to hide it
     */
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

    /**
     * Toggles visibility of the empty state layout and the event list depending on whether
     * there are any events to display.
     *
     * @param show {@code true} to display the empty state; {@code false} to display the list
     */
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
