package com.hotdog.elotto.ui.home;

import static android.app.Activity.RESULT_OK;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hotdog.elotto.MainActivity;
import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.EventAdapter;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.Organizer;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.repository.OrganizerRepository;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;

/**
 * Fragment that displays a list of events created by the current organizer.
 *
 * <p>This view allows organizers to:</p>
 * <ul>
 *     <li>See all events they have created</li>
 *     <li>Navigate to an event's entrant/management view</li>
 *     <li>Create new events via a dedicated event creation activity</li>
 * </ul>
 *
 * <p>Events are loaded asynchronously from Firestore through the {@link Organizer} model,
 * and rendered using an {@link EventAdapter} in a {@link RecyclerView}. Empty and loading
 * states are managed via overlay views.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently.</p>
 *
 * @version 1.0
 * @since 2025-11-01
 */
public class MyEventsView extends Fragment {

    /**
     * Floating action button used to navigate to the event creation screen.
     */
    private FloatingActionButton createEventButton;

    /**
     * Adapter used to display the organizer's events in a RecyclerView.
     */
    private EventAdapter eventAdapter;

    /**
     * Launcher used to start the event creation activity and receive its result.
     */
    private ActivityResultLauncher<Intent> createEventLauncher;

    /**
     * Model representing the currently logged-in organizer, including their events.
     */
    private Organizer organizer;
    private User user;

    /**
     * Cover layout that overlays the content when loading or empty states are shown.
     */
    private ConstraintLayout myEventsCover;

    /**
     * Progress bar displayed while events are being loaded.
     */
    private ProgressBar loadingProgressBar;

    /**
     * TextView displayed when there are no events to show in the organizer's list.
     */
    private TextView myEventsEmpty;

    /**
     * Called when the fragment is first created.
     * Initializes components like {@link Organizer} and {@link EventAdapter},
     * registers the activity result launcher, and loads the organizer’s events.
     *
     * @param savedInstanceState the previously saved state of the fragment, or null if none exists
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Safe to init non-view stuff here
        organizer = new Organizer(requireContext());
        user = new User(requireContext(), () -> {});
        eventAdapter = new EventAdapter(new ArrayList<>(), organizer.getId());
        this.loadEvents();

        // Register the launcher in onCreate (per docs)
        createEventLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadEvents(); // refresh after creating a new event
                    }
                });
    }

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater  LayoutInflater object to inflate views
     * @param container The parent view the fragment's UI should attach to
     * @param savedInstanceState the previously saved instance state, or null
     * @return The root View for the fragment’s layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate your fragment layout
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

        loadingProgressBar = view.findViewById(R.id.myEventsLoadingProgressBar);
        myEventsEmpty = view.findViewById(R.id.MyEventsEmpty);
        myEventsCover = view.findViewById(R.id.MyEventsCover);

        loading(true);

        view.findViewById(R.id.profileButtonMyEvents).setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.profile_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_profile) {
                    NavController navController = NavHostFragment.findNavController(this);
                    navController.navigate(R.id.action_navigation_my_events_to_profileFragment);
                    return true;
                } else if (id == R.id.action_inbox) {
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_navigation_my_events_to_notificationsFragment);
                    return true;

                } else if (id == R.id.action_settings) {
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_navigation_my_events_to_settingsFragment);
                    return true;

                } else if (id == R.id.action_faq) {
                    NavHostFragment.findNavController(MyEventsView.this)
                            .navigate(R.id.action_navigation_my_events_to_faqFragment);
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
     * Called after the view hierarchy has been created.
     * Sets up the RecyclerView, configures the adapter,
     * and attaches the listener to the “Create New Event” button.
     *
     * @param view The root view of the fragment’s layout
     * @param savedInstanceState the previously saved instance state, or {@code null}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Now the view exists—do all findViewById / listeners here
        RecyclerView recyclerView = view.findViewById(R.id.OrganizerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(eventAdapter);

        // Set click listener for event cards
        eventAdapter.setOnEventClickListener(new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
                Bundle bundle = new Bundle();
                NavController navController = NavHostFragment.findNavController(MyEventsView.this);

                // Check if current user is the organizer of this event
                if (event.getOrganizerId() != null && event.getOrganizerId().equals(organizer.getId())) {
                    // Navigate to Organizer View
                    bundle.putString("eventId", event.getId());
                    navController.navigate(R.id.action_navigation_my_events_to_organizerEventEntrantsFragment, bundle);
                } else {
                    // Navigate to Entrant View (Event Details)
                    bundle.putSerializable("event", event);
                    navController.navigate(R.id.action_navigation_my_events_to_eventDetailsFragment, bundle);
                }
            }
        });

        createEventButton = view.findViewById(R.id.CreateNewEventButton);
        createEventButton.setOnClickListener(v -> {
            user.atomicReload(() -> {
                Intent intent = new Intent(requireContext(), EventCreationView.class);
                intent.putExtra("ORGANIZER_NAME", user.getName()); // now guaranteed loaded
                createEventLauncher.launch(intent);
            });
        });

        // Initial load
        loadEvents();
    }

    /**
     * Called when the fragment becomes visible again.
     * Ensures the event list is refreshed in case new events have been created.
     */
    @Override
    public void onResume() {
        super.onResume();
        this.loadEvents();
    }

    /**
     * Loads the list of events belonging to the organizer and updates the adapter.
     * Uses a {@link FirestoreCallback} to handle asynchronous data loading and
     * manages loading and empty states based on the results.
     */
    private void loadEvents(){
        organizer.getEventList(new FirestoreCallback<>() {
            @Override
            public void onSuccess(List<Event> result) {
                eventAdapter.updateEvents(result);
                loading(false);
                empty(result.isEmpty());
            }

            @Override
            public void onError(String errorMessage) {
                empty(true);
            }
        });
    }

    /**
     * Toggles the empty state overlay and related views based on whether
     * the organizer has any events to display.
     *
     * @param value {@code true} to show the empty state, {@code false} to hide it
     */
    private void empty(boolean value) {
        this.myEventsCover.setVisibility(value ? View.VISIBLE : View.GONE);
        this.loadingProgressBar.setVisibility(value ? View.GONE : this.loadingProgressBar.getVisibility());
        this.myEventsEmpty.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    /**
     * Toggles the loading state overlay and related views while events
     * are being fetched from the backend.
     *
     * @param value {@code true} to show the loading indicator, {@code false} to hide it
     */
    private void loading(boolean value) {
        this.myEventsCover.setVisibility(value ? View.VISIBLE : View.GONE);
        this.myEventsEmpty.setVisibility(value ? View.GONE: this.myEventsEmpty.getVisibility());
        this.loadingProgressBar.setVisibility(value ? View.VISIBLE : View.GONE);
    }
}
