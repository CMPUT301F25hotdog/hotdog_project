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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.repository.OrganizerRepository;
import com.hotdog.elotto.ui.entries.EntriesFragment;


import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;

public class MyEventsView extends Fragment {
    private FloatingActionButton createEventButton;
    private EventAdapter eventAdapter;
    private ProgressBar loadingProgressBar;
    private ActivityResultLauncher<Intent> createEventLauncher;
    private Organizer organizer;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate your fragment layout
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

        loadingProgressBar = view.findViewById(R.id.myEventsLoadingProgressBar);

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
                }
                else if (id == R.id.action_inbox) {
                    Toast.makeText(requireContext(), "Inbox clicked", Toast.LENGTH_SHORT).show();
                    return true;

                } else if (id == R.id.action_settings) {
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_navigation_my_events_to_settingsFragment);
                    return true;

                } else if (id == R.id.action_faq) {
                    Toast.makeText(requireContext(), "FAQ clicked", Toast.LENGTH_SHORT).show();
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
                bundle.putSerializable("event", event);
                NavController navController = NavHostFragment.findNavController(MyEventsView.this);
                navController.navigate(R.id.action_navigation_my_events_to_eventDetailsFragment, bundle);
            }
        });

        createEventButton = view.findViewById(R.id.CreateNewEventButton);
        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EventCreationView.class);
            createEventLauncher.launch(intent);
        });

        // Initial load
        loadEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.loadEvents();
    }

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
