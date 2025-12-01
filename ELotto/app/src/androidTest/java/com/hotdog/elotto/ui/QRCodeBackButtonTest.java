package com.hotdog.elotto.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressKey;
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
import android.view.KeyEvent;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.hotdog.elotto.MainActivity;
import com.hotdog.elotto.R;

import org.junit.Rule;
import org.junit.Test;

public class QRCodeBackButtonTest {
    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>( MainActivity.class );
    @Test
    public void inputValuesToCreateEvent(){
        Intents.init();
        onView(withId(R.id.navigation_my_events)).perform(click());
        onView(withId(R.id.CreateNewEventButton)).check(matches(isDisplayed()));
        onView(withId(R.id.CreateNewEventButton)).perform(click());

        Uri fakeImageUri = Uri.parse("WalterPutItAway");
        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK,
                        new Intent().setData(fakeImageUri));

        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);

        onView(withId(R.id.Event_Poster_Input)).perform(click());
        onView(withId(R.id.EventNameInput)).perform(scrollTo(),typeText("Jesse We Need To Cook"), closeSoftKeyboard());
        onView(withId(R.id.Event_Description_Input)).perform(scrollTo(),typeText("Blue"), closeSoftKeyboard());
        onView(withId(R.id.EventPriceInput)).perform(scrollTo(),typeText("$99.1"), closeSoftKeyboard());
        onView(withId(R.id.EventAddressInput)).perform(scrollTo(),typeText("Walter White"), closeSoftKeyboard());
        onView(withId(R.id.EventTimeInput)).perform(scrollTo(),typeText("12:00"), closeSoftKeyboard());
        onView(withId(R.id.EventDateInput)).perform(scrollTo(),typeText("11/07/2025"), closeSoftKeyboard());
        onView(withId(R.id.EventOpensSelector)).perform(scrollTo(),typeText("11/01/2025"), closeSoftKeyboard());
        onView(withId(R.id.EventClosesSelector)).perform(scrollTo(),typeText("11/06/2025"), closeSoftKeyboard());
        onView(withId(R.id.MaxEntrantInput)).perform(scrollTo(),typeText("50"), closeSoftKeyboard());
        onView(withId(R.id.Tag_Button)).perform(scrollTo(), typeText("Outdoor"), pressKey(KeyEvent.KEYCODE_ENTER), closeSoftKeyboard());
        onView(withId(R.id.Confirm_Creation_Button)).perform(click());
        onView(withId(R.id.Go_Back_Button)).perform(click());
        onView(withText("My Events")).check(matches(isDisplayed()));

        Intents.release();
    }
}
