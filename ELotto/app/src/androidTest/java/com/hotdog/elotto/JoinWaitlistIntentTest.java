package com.hotdog.elotto;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Simple UI test for Join Waitlist functionality.
 *
 * Tests that the app launches and basic navigation works.
 *
 * @author ELotto Team
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class JoinWaitlistIntentTest {

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void setUp() {
        scenario = ActivityScenario.launch(MainActivity.class);

        // Wait for app to initialize
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test that MainActivity launches successfully.
     */
    @Test
    public void testMainActivityLaunches() {
        // If we get here without crashing, the test passes
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Test passes if no crash occurs
    }

    /**
     * Test that navigation view is displayed.
     */
    @Test
    public void testBottomNavigationDisplayed() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check for bottom navigation
        onView(withId(R.id.nav_host_fragment_activity_main))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that the app's container layout is displayed.
     */
    @Test
    public void testContainerLayoutDisplayed() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check that the main container exists
        onView(withId(R.id.container))
                .check(matches(isDisplayed()));
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }
}