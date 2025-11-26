package com.hotdog.elotto.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.hotdog.elotto.MainActivity;
import com.hotdog.elotto.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for the filter dialog functionality
 * Tests opening the dialog, selecting/deselecting options, and applying filters.
 */
@RunWith(AndroidJUnit4.class)
public class FilterDialogUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testFilterButtonOpensDialog() {

        sleep(2000);


        onView(withId(R.id.filterButton)).perform(click());


        onView(withText("Filter Events")).check(matches(isDisplayed()));


        onView(withText("Interests")).check(matches(isDisplayed()));

        onView(withText("Date")).check(matches(isDisplayed()));
    }

    @Test
    public void testSelectAndDeselectInterestCheckbox() {

        sleep(2000);
        onView(withId(R.id.filterButton)).perform(click());


        onView(withText("Music"))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()))
                .perform(click());


        onView(withText("Music")).check(matches(isChecked()));


        onView(withText("Music")).perform(click());


        onView(withText("Music")).check(matches(isNotChecked()));
    }

    @Test
    public void testSelectMultipleInterests() {

        sleep(2000);
        onView(withId(R.id.filterButton)).perform(click());


        onView(withText("Music")).perform(click());

        onView(withText("Sports")).perform(click());

        onView(withText("Technology")).perform(click());


        onView(withText("Music")).check(matches(isChecked()));

        onView(withText("Sports")).check(matches(isChecked()));

        onView(withText("Technology")).check(matches(isChecked()));
    }

    @Test
    public void testSwitchToDateTab() {

        sleep(2000);
        onView(withId(R.id.filterButton)).perform(click());


        onView(withText("Date")).perform(click());


        onView(withText("Today")).check(matches(isDisplayed()));

        onView(withText("Tomorrow")).check(matches(isDisplayed()));

        onView(withText("Within 7 days")).check(matches(isDisplayed()));

        onView(withText("Within 14 Days")).check(matches(isDisplayed()));

        onView(withText("This Month")).check(matches(isDisplayed()));

        onView(withText("All Dates")).check(matches(isDisplayed()));
    }

    @Test
    public void testSelectDateFilter() {

        sleep(2000);
        onView(withId(R.id.filterButton)).perform(click());


        onView(withText("Date")).perform(click());


        onView(withId(R.id.radioAllDates)).check(matches(isChecked()));


        onView(withText("Within 7 days")).perform(click());


        onView(withId(R.id.radioWithin7Days)).check(matches(isChecked()));


        onView(withId(R.id.radioAllDates)).check(matches(isNotChecked()));
    }


    @Test
    public void testClearFiltersButton() {

        sleep(2000);
        onView(withId(R.id.filterButton)).perform(click());


        onView(withText("Music")).perform(click());

        onView(withText("Sports")).perform(click());


        onView(withId(R.id.clearFiltersButton)).perform(click());


        onView(withText("Music")).check(matches(isNotChecked()));

        onView(withText("Sports")).check(matches(isNotChecked()));
    }

    @Test
    public void testApplyInterestAndDateFilters() {

        sleep(2000);
        onView(withId(R.id.filterButton)).perform(click());


        onView(withText("Technology")).perform(click());


        onView(withText("Date")).perform(click());


        onView(withText("Within 14 Days")).perform(click());


        onView(withId(R.id.applyFiltersButton)).perform(click());


        sleep(1000);


        onView(withId(R.id.filterButton)).check(matches(isDisplayed()));
    }



    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}