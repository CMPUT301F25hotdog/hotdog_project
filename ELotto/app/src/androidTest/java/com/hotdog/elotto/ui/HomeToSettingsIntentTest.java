package com.hotdog.elotto.ui;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.intent.Intents;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.hotdog.elotto.MainActivity;
import com.hotdog.elotto.R;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;


@RunWith(AndroidJUnit4.class)
public class HomeToSettingsIntentTest
{
    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>( MainActivity.class );

    @Test
    public void navigateHomeToSettings( )
    {
        Intents.init( ) ;

        onView( withId( R.id.profileButton ) ).perform( click( ) );

        onView( withText( "Settings" ) )
                .perform( click( ) );

        onView( withId( R.id.switchOrganizer ) )
                .check( matches( isDisplayed( ) ) );

        Intents.release( );
    }

}
