package com.hotdog.elotto.ui.home;

import static android.app.Activity.RESULT_OK;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hotdog.elotto.MainActivity;
import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.EventAdapter;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.Organizer;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.repository.OrganizerRepository;


import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;
/**
 * MyEventsView is a Fragment that displays a list of events created
 * by an organizer and allows them to create new events.
 */

public class MyEventsView extends Fragment {
    private FloatingActionButton createEventButton;
    private EventAdapter eventAdapter;
    private ProgressBar loadingProgressBar;
    private ActivityResultLauncher<Intent> createEventLauncher;
    private Organizer organizer;
    /**
     * Called when the fragment is first created.
     * Initializes components like Organizer} and EventAdapter,
     * registers the activity result launcher, and loads the organizer’s events.
     *
     * @param savedInstanceState the previously saved state of the fragment, or null if none exists
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Safe to init non-view stuff here
        organizer = new Organizer(requireContext());

        eventAdapter = new EventAdapter(new ArrayList<>(), organizer.getId());
        this.loadEvents();

        // Register the launcher in onCreate (per docs)
        createEventLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadEvents(); // refresh after creating a new event
                    }
                }
        );
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

        createEventButton = view.findViewById(R.id.CreateNewEventButton);
        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EventCreationView.class);
            createEventLauncher.launch(intent);
        });

        // Initial load
        loadEvents();
    }
    /**
     * Called when the fragment becomes visible again.
     * Ensures the event list is refreshed in case new events have been created
     */
    @Override
    public void onResume() {
        super.onResume();
        this.loadEvents();
    }
    /**
     * Loads the list of events belonging to the organizer and updates the adapter.
     * Uses a FirestoreCallback to handle asynchronous data loading.
     */
    private void loadEvents(){
        organizer.getEventList(new FirestoreCallback<>() {
            @Override
            public void onSuccess(List<Event> result) {
                eventAdapter.updateEvents(result);
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }
}
