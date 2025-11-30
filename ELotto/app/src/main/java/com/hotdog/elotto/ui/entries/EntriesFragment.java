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

public class EntriesFragment extends Fragment {

    private RecyclerView entriesView;
    private EventAdapter eventAdapter;
    private ProgressBar loadingProgressBar;
    private View emptyStateLayout;
    private EventRepository eventRepository;
    private ImageButton profileButton;
    private User curUser;

    @Override
    public void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

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

    private void loading(boolean loading) {
        loadingProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        entriesView.setVisibility(loading ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean show) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        entriesView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
