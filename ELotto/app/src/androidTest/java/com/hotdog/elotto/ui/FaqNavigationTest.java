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

@RunWith(AndroidJUnit4.class)
public class FaqNavigationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void navigateHomeToFaq() {
        onView(withId(R.id.profileButton)).perform(click());

        onView(withText("FAQ")).perform(click());

        onView(withText("Frequently Asked Questions"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void toggleFaqCard() {
        onView(withId(R.id.profileButton)).perform(click());
        onView(withText("FAQ")).perform(click());

        onView(withId(R.id.faqCard)).perform(click());
        onView(withId(R.id.faqSubtext))
                .check(matches(withText("The lottery system randomly selects entrants based on available spots. Re-draws may happen if winners don't confirm, ensuring fairness for all entrants.")));

        onView(withId(R.id.faqCard)).perform(click());
        onView(withId(R.id.faqSubtext))
                .check(matches(withText("Tap to learn about the selection criteria")));
    }
}
