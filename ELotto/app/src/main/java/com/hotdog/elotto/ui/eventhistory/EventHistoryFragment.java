package com.hotdog.elotto.ui.eventhistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.EventHistoryAdapter;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.EventRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EventHistoryFragment extends Fragment {

    // UI
    private Toolbar toolbar;
    private RecyclerView drawnEventsRecyclerView;
    private RecyclerView pendingEventsRecyclerView;
    private TextView emptyDrawnEventsTextView;
    private TextView emptyPendingEventsTextView;
    private ProgressBar loadingProgressBar; // optional if you add it to XML

    // Adapters (history-specific)
    private EventHistoryAdapter drawnEventsAdapter;
    private EventHistoryAdapter pendingEventsAdapter;

    // Data
    private EventRepository eventRepository;
    private final List<Event> drawnEvents = new ArrayList<>();
    private final List<Event> pendingEvents = new ArrayList<>();
    private String currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventRepository = new EventRepository();

        // Device-ID identity (your existing approach)
        User currentUser = new User(requireContext(), false);
        currentUserId = currentUser.getId();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_history, container, false);
        initViews(view);
        setupToolbar();
        setupRecyclerViews();
        loadEvents();
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        drawnEventsRecyclerView = view.findViewById(R.id.drawnEventsRecyclerView);
        pendingEventsRecyclerView = view.findViewById(R.id.pendingEventsRecyclerView);
        emptyDrawnEventsTextView = view.findViewById(R.id.emptyDrawnEventsTextView);
        emptyPendingEventsTextView = view.findViewById(R.id.emptyPendingEventsTextView);
        // loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
    }

    private void setupToolbar() {
        // Safer back behavior: pop current screen
        toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );
    }

    private void setupRecyclerViews() {
        drawnEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        drawnEventsRecyclerView.setHasFixedSize(true);
        pendingEventsRecyclerView.setHasFixedSize(true);

        drawnEventsAdapter = new EventHistoryAdapter(
                drawnEvents, currentUserId, EventHistoryAdapter.Mode.DRAWN);
        pendingEventsAdapter = new EventHistoryAdapter(
                pendingEvents, currentUserId, EventHistoryAdapter.Mode.PENDING);

        drawnEventsRecyclerView.setAdapter(drawnEventsAdapter);
        pendingEventsRecyclerView.setAdapter(pendingEventsAdapter);

        drawnEventsAdapter.setOnEventClickListener(event ->
                Toast.makeText(getContext(),
                        "Event: " + event.getName() + " (Selected)", Toast.LENGTH_SHORT).show());

        pendingEventsAdapter.setOnEventClickListener(event ->
                Toast.makeText(getContext(),
                        "Event: " + event.getName() + " (Pending/Waitlisted)", Toast.LENGTH_SHORT).show());
    }

    private void loadEvents() {
        // showLoading(true);

        eventRepository.getAllEvents(new FirestoreListCallback<Event>() {
            @Override
            public void onSuccess(List<Event> events) {
                // showLoading(false);

                drawnEvents.clear();
                pendingEvents.clear();

                for (Event e : events) {
                    boolean inSelected = isUserInList(e.getSelectedEntrantIds());
                    boolean inAccepted = isUserInList(e.getAcceptedEntrantIds());
                    boolean inWaitlist = isUserInList(e.getWaitlistEntrantIds());

                    // History should only show events relevant to this user
                    if (inSelected || inAccepted) {
                        drawnEvents.add(e);          // “Selected” section
                    } else if (inWaitlist) {
                        pendingEvents.add(e);        // “Pending/Waitlisted” section
                    }
                }

                // Sort by event datetime (nulls last) for nicer display
                Comparator<Event> byDate = (a, b) -> {
                    if (a.getEventDateTime() == null && b.getEventDateTime() == null) return 0;
                    if (a.getEventDateTime() == null) return 1;
                    if (b.getEventDateTime() == null) return -1;
                    return a.getEventDateTime().compareTo(b.getEventDateTime());
                };
                Collections.sort(drawnEvents, byDate);
                Collections.sort(pendingEvents, byDate);

                drawnEventsAdapter.update(new ArrayList<>(drawnEvents));
                pendingEventsAdapter.update(new ArrayList<>(pendingEvents));
                updateEmptyStates();
            }

            @Override
            public void onError(String error) {
                // showLoading(false);
                Toast.makeText(getContext(), "Error loading events: " + error, Toast.LENGTH_SHORT).show();
                updateEmptyStates();
            }
        });
    }

    private boolean isUserInList(List<String> ids) {
        return ids != null && currentUserId != null && ids.contains(currentUserId);
    }

    private void updateEmptyStates() {
        emptyDrawnEventsTextView.setVisibility(drawnEvents.isEmpty() ? View.VISIBLE : View.GONE);
        drawnEventsRecyclerView.setVisibility(drawnEvents.isEmpty() ? View.GONE : View.VISIBLE);

        emptyPendingEventsTextView.setVisibility(pendingEvents.isEmpty() ? View.VISIBLE : View.GONE);
        pendingEventsRecyclerView.setVisibility(pendingEvents.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
