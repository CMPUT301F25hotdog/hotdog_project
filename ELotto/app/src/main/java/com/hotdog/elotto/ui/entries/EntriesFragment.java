package com.hotdog.elotto.ui.entries;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.EventAdapter;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.EventRepository;

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
        entriesView=view.findViewById(R.id.entriesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        entriesView.setLayoutManager(layoutManager);
        loadingProgressBar=view.findViewById(R.id.entriesProgressBar);
        emptyStateLayout=view.findViewById(R.id.entriesEmptyLayout);
        eventRepository=new EventRepository();
        curUser=new User(context, true);
        eventAdapter=new EventAdapter(new ArrayList<>(), curUser.getId());
        entriesView.setAdapter(eventAdapter);
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
