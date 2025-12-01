package com.hotdog.elotto.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.EventAdapter;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.helpers.Status;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.Organizer;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.ui.home.MyEventsView;
import com.hotdog.elotto.ui.profile.EventHistoryFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment that shows the entrant-facing calendar screen.
 * <p>
 * This screen combines three main pieces:
 * <ul>
 *     <li>A {@link CalendarView} that lets the user pick a day.</li>
 *     <li>A title row that switches between "All Events" and
 *         "Events on &lt;date&gt;".</li>
 *     <li>A {@link RecyclerView} that displays the list of events as
 *         event cards for either all days or the selected day.</li>
 * </ul>
 *
 * <p>The fragment is intentionally fairly "dumb": it delegates all data
 * access to {@link EventRepository} and all item rendering to
 * {@link CalendarEventAdapter}. Its responsibility is to:
 *
 * <ol>
 *     <li>Wire up the views in {@code fragment_calendar.xml}.</li>
 *     <li>Load events once from Firestore.</li>
 *     <li>Filter the in-memory list when the user changes the selected date.</li>
 * </ol>
 *
 * <p>This keeps the UI logic small and easy to reason about, while still
 * matching the Figma design of a calendar + list view.
 */
public class CalendarFragment extends Fragment {

    /** Native Android calendar widget used to select a day. */
    private CalendarView calendarView;

    /** Title above the list ("All Events" or "Events on March 15"). */
    private TextView tvEventsTitle;

    /** RecyclerView that shows event cards for the current filter. */
    private RecyclerView rvEvents;

    /** Adapter that binds {@link Event} objects into event cards. */
    private CalendarEventAdapter eventAdapter;
    /** Organizer instance that allows us to check if the user is an organizer for an event */
    private Organizer organizer;

    /** Repository responsible for loading events from Firestore. */
    private final EventRepository eventRepository = new EventRepository();

    /**
     * Full list of events returned from Firestore.
     * This list is never filtered; it acts as the single source of truth.
     */
    private final List<Event> allEvents = new ArrayList<>();

    /**
     * Currently visible subset of {@link #allEvents} based on the calendar
     * selection. When no day is selected, this simply contains all events.
     */
    private final List<Event> visibleEvents = new ArrayList<>();

    /** User reference */
    private User user;

    /**
     * Formatter used to render the "Events on March 15" header text.
     * Only month and day are shown – the year is implied by the calendar.
     */
    private final SimpleDateFormat headerFormat =
            new SimpleDateFormat("MMMM d", Locale.getDefault());

    /**
     * Inflates the layout for the calendar screen.
     *
     * <p>No heavy work should be done here – we just inflate the XML and
     * let {@link #onViewCreated(View, Bundle)} take care of wiring things up.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    /**
     * Called after the view hierarchy has been created.
     *
     * <p>This method:
     * <ol>
     *     <li>Finds all relevant views from the inflated layout.</li>
     *     <li>Sets up the {@link RecyclerView} and its adapter.</li>
     *     <li>Configures the {@link CalendarView} listener so that changing
     *         the date filters the list of events.</li>
     *     <li>Triggers the initial load of events from Firestore.</li>
     * </ol>
     *
     * @param view       the root view returned by {@link #onCreateView}
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        calendarView = view.findViewById(R.id.calendar_view);
        tvEventsTitle = view.findViewById(R.id.tv_events_title);
        rvEvents = view.findViewById(R.id.rv_events);
        organizer = new Organizer(requireContext());
        user = new User(requireContext(), (user) -> {
            eventAdapter = new CalendarEventAdapter(requireContext(), visibleEvents);
            // Set click listener for event cards
            eventAdapter.setOnEventClickListener(new EventAdapter.OnEventClickListener() {
                @Override
                public void onEventClick(Event event) {
                    // Check if user is registered and has Invited status
                    Status userStatus = getUserStatusForEvent(event.getId(), user);

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("event", event);
                    NavController navController = NavHostFragment.findNavController(CalendarFragment.this);

                    // If user is invited and invitation hasn't expired then go to accept/decline screen
                    if (userStatus == Status.Selected) {
                        navController.navigate(R.id.action_navigation_calendar_to_acceptDeclineInvitationFragment, bundle);
                    } else {
                        // default to regular event details screen
                        navController.navigate(R.id.action_navigation_calendar_to_eventDetailsFragment, bundle);
                    }
                }
            });
        });

        // Set up RecyclerView
        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEvents.setAdapter(eventAdapter);

        // Start the calendar on "today"
        calendarView.setDate(System.currentTimeMillis(), false, true);

        // When the user taps a different day, filter the events list
        calendarView.setOnDateChangeListener((cv, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);
            filterForDate(selected.getTime());
        });

        // Load events from Firestore
        loadEvents();
    }

    /**
     * Get the user's status for a specific event.
     *
     * @param eventId The ID of the event
     * @return The user's Status for this event, or null if not found
     */
    private Status getUserStatusForEvent(String eventId, User user) {
        User.RegisteredEvent event = user.getSingleRegEvent(eventId);
        if(event!=null) return event.getStatus();

        return null;
    }

    /**
     * Loads all events from the backend using {@link EventRepository}.
     *
     * <p>On success, this method:
     * <ul>
     *     <li>Replaces the contents of {@link #allEvents}.</li>
     *     <li>Shows the "All Events" view by default.</li>
     * </ul>
     *
     * <p>On error, the current implementation quietly ignores the failure,
     * but it is trivial to add a Toast or a Snackbar inside {@link #onError(String)}
     * if we want user-facing feedback.
     */
    private void loadEvents() {
        eventRepository.getAllEvents(new FirestoreListCallback<Event>() {
            @Override
            public void onSuccess(List<Event> events) {
                allEvents.clear();
                allEvents.addAll(events);
                showAllEvents();
            }

            @Override
            public void onError(String errorMessage) {
                // Could log or show an error message here if desired
                // e.g., Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Resets the view to show every event, regardless of date.
     *
     * <p>This is used:
     * <ul>
     *     <li>Right after events are initially loaded.</li>
     *     <li>If we later decide to add a "Show all" button or behaviour.</li>
     * </ul>
     */
    private void showAllEvents() {
        tvEventsTitle.setText("All Events");
        visibleEvents.clear();
        visibleEvents.addAll(allEvents);
        eventAdapter.setEvents(visibleEvents);
    }

    /**
     * Filters {@link #allEvents} down to only the events occurring on the
     * specified calendar day and updates the RecyclerView.
     *
     * <p>Time-of-day is ignored for the comparison: if an event happens at
     * 07:00 or 19:00 on March 15, it is treated as "March 15" either way.
     *
     * @param date the day the user selected in the {@link CalendarView}
     */
    private void filterForDate(Date date) {
        Calendar target = Calendar.getInstance();
        target.setTime(date);
        int tYear = target.get(Calendar.YEAR);
        int tMonth = target.get(Calendar.MONTH);
        int tDay = target.get(Calendar.DAY_OF_MONTH);

        visibleEvents.clear();

        for (Event e : allEvents) {
            Date eventDate = getEventDate(e);
            if (eventDate == null) {
                continue;
            }

            Calendar c = Calendar.getInstance();
            c.setTime(eventDate);

            if (c.get(Calendar.YEAR) == tYear &&
                    c.get(Calendar.MONTH) == tMonth &&
                    c.get(Calendar.DAY_OF_MONTH) == tDay) {
                visibleEvents.add(e);
            }
        }

        String header = "Events on " + headerFormat.format(date);
        tvEventsTitle.setText(header);
        eventAdapter.setEvents(visibleEvents);
    }

    /**
     * Helper method to read the date that should be used for calendar matching
     * from an {@link Event}.
     *
     * <p>Right now this simply returns {@link Event#getEventDateTime()}, but
     * having it as a separate method makes it easy to change the behaviour
     * later (for example, if we ever want to group by registration dates
     * instead of event dates).
     *
     * @param event the event whose date we care about
     * @return the {@link Date} used for calendar matching, or {@code null}
     * if the event has no event date set
     */
    private Date getEventDate(Event event) {
        return event.getEventDateTime();
    }
}
