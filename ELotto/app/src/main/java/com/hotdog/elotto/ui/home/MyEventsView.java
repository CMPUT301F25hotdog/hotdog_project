package com.hotdog.elotto.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

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

public class MyEventsView extends AppCompatActivity {
    private FloatingActionButton createEventButton;
    private EventAdapter eventAdapter;
    private EventRepository eventRepository;
    private ArrayList<Event> orgEvents;
    private List<String> eventIds;
    private ActivityResultLauncher<Intent> createEventLauncher;
    private Organizer organizer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_events);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        organizer = new Organizer(this);
        String currentOrg = organizer.getId();
        orgEvents = new ArrayList();
        eventAdapter = new EventAdapter(orgEvents,currentOrg);
        eventRepository = new EventRepository();

        RecyclerView recyclerView = findViewById(R.id.OrganizerEvents);
        recyclerView.setAdapter(eventAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        createEventLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadEvents();
                    }
                }
        );

        createEventButton = findViewById(R.id.CreateNewEventButton);
        createEventButton.setOnClickListener(v->{
            Intent intent = new Intent(MyEventsView.this, EventCreationView.class);
            createEventLauncher.launch(intent);
        });

        loadEvents();

    }
    private void loadEvents(){
        Organizer organizer = new Organizer(this);
        List<String> eventIds = organizer.getMyEvents();
        orgEvents.clear();
        if(eventIds!=null){
            for(String eventId : eventIds){
                eventRepository.getEventById(eventId, new FirestoreCallback<Event>() {
                    @Override
                    public void onSuccess(Event result) {
                        orgEvents.add(result);
                        eventAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onError(String errorMessage) {
                        Log.e("MyEvents", "Failed to fetch event: " + errorMessage);
                    }
                });
            }
        }

    }
}
