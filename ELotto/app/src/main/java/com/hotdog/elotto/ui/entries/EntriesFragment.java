package com.hotdog.elotto.ui.entries;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.EventAdapter;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Fragment for displaying the user's registered event entries.
 *
 * <p>This fragment shows a list of all events that the current user has registered
 * for, with status indicators and navigation to event details. Provides access to
 * profile-related features through a popup menu including profile settings, inbox,
 * settings, FAQ, and QR scanning.</p>
 *
 * <p>Features include:</p>
 * <ul>
 *     <li>RecyclerView displaying all registered events</li>
 *     <li>Event cards showing status (pending, selected, accepted, waitlisted, etc.)</li>
 *     <li>Navigation to event details when cards are clicked</li>
 *     <li>Popup menu for accessing profile features</li>
 *     <li>Loading indicator during data fetch</li>
 *     <li>Empty state display when no registered events exist</li>
 * </ul>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author
 * @version 1.0
 * @since 2025-11-01
 */
public class EntriesFragment extends Fragment {

    /**
     * RecyclerView for displaying the list of registered events.
     */
    private RecyclerView entriesView;

    /**
     * Adapter for binding event data to the RecyclerView.
     */
    private EventAdapter eventAdapter;

    /**
     * ProgressBar shown while loading events from Firestore.
     */
    private ProgressBar loadingProgressBar;

    /**
     * View displayed when the user has no registered events.
     */
    private View emptyStateLayout;

    /**
     * Repository for event data access operations.
     */
    private EventRepository eventRepository;

    /**
     * Button for opening the profile menu.
     */
    private ImageButton profileButton;

    /**
     * The current user whose registered events are being displayed.
     */
    private User curUser;

    /**
     * Called to do initial creation of the fragment.
     *
     * @param savedInstanceState the previously saved state of the fragment
     */
    @Override
    public void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * <p>Inflates the fragment layout, initializes all views and repositories,
     * sets up the RecyclerView with adapter, configures event click listeners,
     * and sets up the profile button popup menu with navigation options.</p>
     *
     * @param inflater the LayoutInflater object that can be used to inflate views
     * @param container the parent view that the fragment's UI should be attached to
     * @param savedInstanceState the previously saved state of the fragment
     * @return the root View of the fragment's layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entries, container, false);

        init(view, getContext());

        profileButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.profile_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_profile) {
                    NavController navController = NavHostFragment.findNavController(EntriesFragment.this);
                    navController.navigate(R.id.action_navigation_entries_to_profileFragment);
                    return true;
                } else if (id == R.id.action_inbox) {
                    NavHostFragment.findNavController(EntriesFragment.this)
                            .navigate(R.id.action_navigation_entries_to_notificationsFragment);
                    return true;

                } else if (id == R.id.action_settings) {
                    NavHostFragment.findNavController(EntriesFragment.this)
                            .navigate(R.id.action_navigation_entries_to_settingsFragment);
                    return true;

                } else if (id == R.id.action_faq) {
                    NavHostFragment.findNavController(EntriesFragment.this)
                            .navigate(R.id.action_navigation_entries_to_faqFragment);
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

        return view;
    }

    /**
     * Loads the user's registered events from Firestore.
     *
     * <p>Shows a loading indicator while fetching event data. On success, updates
     * the adapter with the retrieved events and hides the empty state. On error,
     * displays the empty state and clears the event list. Logs the returned events
     * for debugging purposes.</p>
     */
    private void loadEvents() {
        loading(true);
        AtomicReference<List<Event>> regEvents = new AtomicReference<>();
        eventRepository.getEventsById(curUser.getRegEventIds(), new FirestoreCallback<>() {
            @Override
            public void onSuccess(List<Event> result) {
                loading(false);
                showEmptyState(false);
                regEvents.set(result);
                eventAdapter.updateEvents(regEvents.get());
                Log.e("RETURNED EVENTS", Arrays.toString(regEvents.get().toArray()));
            }

            @Override
            public void onError(String errorMessage) {
                loading(false);
                showEmptyState(true);
                regEvents.set(new ArrayList<>());
                eventAdapter.updateEvents(regEvents.get());
            }
        });
    }

    /**
     * Initializes all view components, repositories, and adapters.
     *
     * <p>Binds UI elements, sets up the RecyclerView with LinearLayoutManager,
     * creates the EventRepository and User instances, initializes the EventAdapter
     * with an empty list, and sets up the click listener for event cards to navigate
     * to event details. Finally, loads the user's registered events.</p>
     *
     * @param view the root view of the fragment
     * @param context the application context
     */
    private void init(View view, Context context) {
        entriesView = view.findViewById(R.id.entriesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        entriesView.setLayoutManager(layoutManager);
        loadingProgressBar = view.findViewById(R.id.entriesProgressBar);
        emptyStateLayout = view.findViewById(R.id.entriesEmptyLayout);
        eventRepository = new EventRepository();
        profileButton = view.findViewById(R.id.entriesProfileButton);
        curUser = new User(context);
        eventAdapter = new EventAdapter(new ArrayList<>(), curUser.getId());
        entriesView.setAdapter(eventAdapter);

        // Set click listener for event cards
        eventAdapter.setOnEventClickListener(new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("event", event);
                NavController navController = NavHostFragment.findNavController(EntriesFragment.this);
                navController.navigate(R.id.action_navigation_entries_to_eventDetailsFragment, bundle);
            }
        });
        loadEvents();
    }

    /**
     * Controls the visibility of loading, RecyclerView, and empty state views.
     *
     * <p>When loading is true, shows the progress bar and hides the RecyclerView
     * and empty state. When loading is false, hides the progress bar and shows
     * the RecyclerView (empty state remains hidden).</p>
     *
     * @param loading true to show loading indicator, false to hide it
     */
    private void loading(boolean loading) {
        loadingProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        entriesView.setVisibility(loading ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    /**
     * Controls the visibility of the empty state layout.
     *
     * <p>When show is true, displays the empty state message and hides the
     * RecyclerView. When show is false, hides the empty state and shows the
     * RecyclerView.</p>
     *
     * @param show true to show empty state, false to hide it
     */
    private void showEmptyState(boolean show) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        entriesView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}