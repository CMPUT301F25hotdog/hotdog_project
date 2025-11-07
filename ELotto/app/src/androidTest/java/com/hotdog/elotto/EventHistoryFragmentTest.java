package com.hotdog.elotto;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.espresso.Espresso;

import com.hotdog.elotto.ui.eventhistory.EventHistoryFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class EventHistoryFragmentTest {

    /** Launch MainActivity so fragment can be attached */
    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class, true, true);

    /** Helper method to add the fragment to the activity */
    private void launchFragment() {
        activityRule.getActivity().runOnUiThread(() -> {
            EventHistoryFragment fragment = new EventHistoryFragment();

            activityRule.getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, fragment)
                    .commitNow(); // make sure fragment is active immediately
        });
    }

    /** ✅ Test 1: Fragment loads without crashing */
    @Test
    public void fragmentLaunches() {
        launchFragment();
    }

    /** ✅ Test 2: Toolbar must exist and be visible */
    @Test
    public void toolbarIsVisible() {
        launchFragment();

        Espresso.onView(withId(R.id.toolbar))
                .check(matches(isDisplayed()));
    }

}
