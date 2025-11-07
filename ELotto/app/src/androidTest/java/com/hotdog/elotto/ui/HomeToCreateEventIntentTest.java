package com.hotdog.elotto.ui;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.hotdog.elotto.MainActivity;
import com.hotdog.elotto.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

public class HomeToCreateEventIntentTest {
    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>( MainActivity.class );
    @Test
    public void navigateToCreateEvent(){
        onView(withId(R.id.CreateEventButton)).perform(click());
        onView(withText("Create Event")).check(matches(isDisplayed()));
    }
}
