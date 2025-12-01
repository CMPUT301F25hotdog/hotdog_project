package com.hotdog.elotto.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import com.hotdog.elotto.R;
import com.hotdog.elotto.ui.home.EventCreationView;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CreateEventInputIntentTest {

    @Test
    public void inputValuesToCreateEvent() {
        Intents.init();

        // Launch EventCreationView with TEST_MODE enabled
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                EventCreationView.class);
        intent.putExtra("TEST_MODE", true); // enables controller test mode
        intent.putExtra("ORGANIZER_NAME", "Test Organizer");

        ActivityScenario<EventCreationView> scenario = ActivityScenario.launch(intent);

        // Mock image picking
        Uri fakeImageUri = Uri.parse("WalterPutItAway");
        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK,
                        new Intent().setData(fakeImageUri));
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);

        // Fill in event fields
        onView(withId(R.id.Event_Poster_Input)).perform(click());
        onView(withId(R.id.EventNameInput)).perform(scrollTo(), typeText("Jesse We Need To Cook"), closeSoftKeyboard());
        onView(withId(R.id.Event_Description_Input)).perform(scrollTo(), typeText("Blue"), closeSoftKeyboard());
        onView(withId(R.id.EventPriceInput)).perform(scrollTo(), typeText("99.1"), closeSoftKeyboard());
        onView(withId(R.id.EventAddressInput)).perform(scrollTo(), typeText("Walter White"), closeSoftKeyboard());

        // Click date/time layouts instead of typing
        onView(withId(R.id.EventDateInput)).perform(scrollTo(), click());
        onView(withId(R.id.EventTimeInput)).perform(scrollTo(), click());

        // Click registration period selectors
        onView(withId(R.id.EventOpensSelector)).perform(scrollTo(), click());
        onView(withId(R.id.EventClosesSelector)).perform(scrollTo(), click());

        onView(withId(R.id.MaxEntrantInput)).perform(scrollTo(), typeText("50"), closeSoftKeyboard());

        // Confirm creation
        onView(withId(R.id.Confirm_Creation_Button)).perform(click());

        // Verify event creation Toast or message
        onView(withText("Event Created")).check(matches(isDisplayed()));

        Intents.release();
    }
}
