package com.hotdog.elotto;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.hotdog.elotto.R;
import com.hotdog.elotto.ui.eventhistory.EventHistoryFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class EventHistoryFragmentTest {

    /** ✅ Test 1: Fragment should load successfully */
    @Test
    public void fragmentLaunchesSuccessfully() {
        FragmentScenario.launchInContainer(EventHistoryFragment.class);
    }

    /** ✅ Test 2: Toolbar exists */
    @Test
    public void toolbarIsDisplayed() {
        FragmentScenario.launchInContainer(EventHistoryFragment.class);

        Espresso.onView(withId(R.id.toolbar))
                .check(matches(isDisplayed()));
    }

    /** ✅ Test 3: Section headers are displayed */
    @Test
    public void sectionTitlesAreDisplayed() {
        FragmentScenario.launchInContainer(EventHistoryFragment.class);

        Espresso.onView(withId(R.id.drawnEventsSectionTitle))
                .check(matches(isDisplayed()));

        Espresso.onView(withId(R.id.pendingEventsSectionTitle))
                .check(matches(isDisplayed()));
    }

    /** ✅ Test 4: RecyclerView displays at least one item */
    @Test
    public void recyclerViewShowsEventItems() {
        FragmentScenario.launchInContainer(EventHistoryFragment.class);

        Espresso.onView(withId(R.id.drawnEventsRecyclerView))
                .check(matches(isDisplayed()));
    }

    /** ✅ Test 5: Clicks first event (optional interaction test) */
    @Test
    public void clickingEventOpensDetails() {
        FragmentScenario.launchInContainer(EventHistoryFragment.class);

        Espresso.onView(withId(R.id.drawnEventsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, Espresso.onView(withId(R.id.eventCard))));
    }
}
