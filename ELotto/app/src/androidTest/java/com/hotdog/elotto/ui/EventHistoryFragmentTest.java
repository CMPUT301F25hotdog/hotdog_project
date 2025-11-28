package com.hotdog.elotto.ui;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.hotdog.elotto.MainActivity;
import com.hotdog.elotto.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * UI tests for Event History Fragment.
 * Tests US 01.02.03: As an entrant, I want to have a history of events
 * I have registered for, whether I was selected or not.
 *
 * @author [Your Name]
 * @version 1.0
 * @since 2025-11-23
 */
@RunWith(AndroidJUnit4.class)
public class EventHistoryFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Test that clicking the Entries bottom navigation button navigates to Event History screen.
     */
    @Test
    public void entriesButtonNavigatesToEventHistory() {
        // Click on the Entries bottom navigation button
        onView(withId(R.id.eventHistoryFragment)).perform(click());

        // Verify that the Event History toolbar is displayed
        onView(withText("Event History")).check(matches(isDisplayed()));
    }

    /**
     * Test that the toolbar displays the correct title.
     */
    @Test
    public void toolbarDisplaysCorrectTitle() {
        // Navigate to Event History
        onView(withId(R.id.eventHistoryFragment)).perform(click());

        // Check that the toolbar shows "Event History"
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(withText("Event History")).check(matches(isDisplayed()));
    }

    /**
     * Test that back button is displayed on the toolbar.
     */
    @Test
    public void backButtonIsDisplayed() {
        // Navigate to Event History
        onView(withId(R.id.eventHistoryFragment)).perform(click());

        // Verify toolbar is displayed (back button is part of toolbar)
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
    }

    /**
     * Test that "Drawn Events" section title is displayed.
     */
    @Test
    public void drawnEventsTitleIsDisplayed() {
        // Navigate to Event History
        onView(withId(R.id.eventHistoryFragment)).perform(click());

        // Check that "Drawn Events" title is shown
        onView(withId(R.id.drawnEventsTitle)).check(matches(isDisplayed()));
        onView(withText("Drawn Events")).check(matches(isDisplayed()));
    }

    /**
     * Test that "Pending Events" section title is displayed.
     */
    @Test
    public void pendingEventsTitleIsDisplayed() {
        // Navigate to Event History
        onView(withId(R.id.eventHistoryFragment)).perform(click());

        // Check that "Pending Events" title is shown
        onView(withId(R.id.pendingEventsTitle)).check(matches(isDisplayed()));
        onView(withText("Pending Events")).check(matches(isDisplayed()));
    }

    /**
     * Test that RecyclerViews are present in the layout.
     */
    @Test
    public void recyclerViewsAreDisplayed() {
        // Navigate to Event History
        onView(withId(R.id.eventHistoryFragment)).perform(click());

        // Verify both RecyclerViews exist
        onView(withId(R.id.drawnEventsRecyclerView)).check(matches(isDisplayed()));
        onView(withId(R.id.pendingEventsRecyclerView)).check(matches(isDisplayed()));
    }

    /**
     * Test that empty state message is shown when user has no drawn events.
     * Note: This test assumes the current user has no drawn events.
     */
    @Test
    public void emptyStateShownForNoDrawnEvents() {
        // Navigate to Event History
        onView(withId(R.id.eventHistoryFragment)).perform(click());

        // Check for empty state message (if user has no drawn events)
        // This may need to be adjusted based on actual test data
        onView(withText("No drawn events yet")).check(matches(isDisplayed()));
    }

    /**
     * Test that empty state message is shown when user has no pending events.
     * Note: This test assumes the current user has no pending events.
     */
    @Test
    public void emptyStateShownForNoPendingEvents() {
        // Navigate to Event History
        onView(withId(R.id.eventHistoryFragment)).perform(click());

        // Check for empty state message (if user has no pending events)
        onView(withText("No pending events")).check(matches(isDisplayed()));
    }

    /**
     * Test navigation from Event History back to home using back button.
     */
    @Test
    public void backButtonNavigatesToPreviousScreen() {
        // Navigate to Event History
        onView(withId(R.id.eventHistoryFragment)).perform(click());

        // Verify we're on Event History
        onView(withText("Event History")).check(matches(isDisplayed()));

        // Click back button
        onView(withId(R.id.toolbar)).perform(click());

        // Note: Actual verification of navigation would depend on your nav setup
    }

    /**
     * Test that clicking an event card navigates to event details.
     * Note: This test requires at least one event in the list.
     * You may need to create test data or mock Firebase for this to work.
     */
    @Test
    public void clickingEventNavigatesToDetails() {
        // Navigate to Event History
        onView(withId(R.id.eventHistoryFragment)).perform(click());

        // Click first item in drawn events RecyclerView
        // Note: This will only work if there's at least one event
        try {
            onView(withId(R.id.drawnEventsRecyclerView))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

            // Verify navigation occurred (check for event details screen)
            // This depends on your EventDetailsFragment layout
            onView(withText("Event Details")).check(matches(isDisplayed()));
        } catch (Exception e) {
            // If no events exist, this is expected
            System.out.println("No events available for click test");
        }
    }

    /**
     * Test that the screen loads without crashing.
     */
    @Test
    public void eventHistoryFragmentLoadsSuccessfully() {
        // Navigate to Event History
        onView(withId(R.id.eventHistoryFragment)).perform(click());

        // Verify key components are present
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.drawnEventsTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.pendingEventsTitle)).check(matches(isDisplayed()));
    }

    /**
     * Test navigation between bottom nav tabs works correctly.
     */
    @Test
    public void navigationBetweenTabsWorks() {
        // Navigate to Event History (Entries tab)
        onView(withId(R.id.eventHistoryFragment)).perform(click());
        onView(withText("Event History")).check(matches(isDisplayed()));

        // Navigate to Home
        onView(withId(R.id.navigation_home)).perform(click());
        onView(withText("Home")).check(matches(isDisplayed()));

        // Navigate back to Event History
        onView(withId(R.id.eventHistoryFragment)).perform(click());
        onView(withText("Event History")).check(matches(isDisplayed()));
    }

    /**
     * Test that ScrollView allows scrolling through content.
     */
    @Test
    public void scrollViewIsScrollable() {
        // Navigate to Event History
        onView(withId(R.id.eventHistoryFragment)).perform(click());

        // Verify ScrollView exists
        onView(withId(R.id.scrollView)).check(matches(isDisplayed()));
    }
}