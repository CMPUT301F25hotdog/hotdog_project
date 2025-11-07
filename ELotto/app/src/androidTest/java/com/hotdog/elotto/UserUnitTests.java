package com.hotdog.elotto;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.hotdog.elotto.helpers.Status;
import com.hotdog.elotto.helpers.UserType;
import com.hotdog.elotto.model.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Instrumented JUnit 5 tests for User using da emulator.
 * We avoid Firebase by making a blank user
 * So we just go fast zoom zoom way with blank constructor.
 */
public class UserUnitTests {

    // To diddle the little private bits that we usually can't touch
    private static void setPrivate(Object target, String name, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Gimme that private shiat, please and thank you
    private static Object getPrivate(Object target, String name) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Same thing but we diddle the static instead
    private static void setStatic(Class<?> clazz, String fieldName, Object value) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(null, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Keepin that lil bitch SuperUser outta here so it don't fuck wit our tests cuz it all references the same damn user (why do I have to be so cool and awesome and make such unbreakable shit)
    @BeforeEach
    void clearSuperUserBefore() {
        setStatic(User.class, "SuperUser", null);
    }

    @AfterEach
    void clearSuperUserAfter() {
        setStatic(User.class, "SuperUser", null);
    }

    // Just make a user without repo to make sure the internals work
    @Test
    void constructor_withContext_readsDeviceId_andCopiesSuperUser_withoutRepo() {
        // Seed a SuperUser so the repo path is skipped.
        User superU = new User();
        superU.setName("Seed Name");
        superU.setEmail("seed@mail");
        superU.setPhone("555-0000");
        superU.setType(UserType.Entrant);
        superU.addRegEvent("Z1");

        // Diddle the privates
        setPrivate(superU, "exists", true);
        setStatic(User.class, "SuperUser", superU);

        // Should copy that slutty lil super user
        Context ctx = ApplicationProvider.getApplicationContext();
        User u = new User(ctx, true);

        String deviceId = u.getId();
        Assertions.assertNotNull(deviceId);
        Assertions.assertFalse(deviceId.isEmpty()); // Supposed to be real device ID I hope

        // Like I said before I hope to the lord above that I don't believe in that they copied that slutty lil super user
        Assertions.assertTrue(u.exists());
        Assertions.assertEquals("Seed Name", u.getName());
        Assertions.assertEquals("seed@mail", u.getEmail());
        Assertions.assertEquals("555-0000", u.getPhone());
        Assertions.assertEquals(UserType.Entrant, u.getType());
        Assertions.assertEquals(List.of("Z1"), u.getRegEvents());
    }

    // Bruh idk I got like no ideas for tests
    @Test
    void defaults_fromNoArgCtor_areEmptyOrNull() {
        User u = new User();
        Assertions.assertEquals("", u.getName());
        Assertions.assertEquals("", u.getEmail());
        Assertions.assertEquals("", u.getPhone());
        Assertions.assertNull(u.getType());
        Assertions.assertEquals(List.of(), u.getRegEvents());
        Assertions.assertEquals("", u.getId());
        Assertions.assertFalse(u.exists());
    }

    // Test firebase updates work
    @Test
    void setNameEmailPhoneType_changeState() {
        Context ctx = ApplicationProvider.getApplicationContext();
        // Creates the super user
        User u = new User(ctx, true);
        // Update the shit
        u.updateName("Alice");
        u.updateEmail("a@b.c");
        u.updatePhone("123");
        u.updateType(UserType.Organizer);

        // POV CMPUT 175 Comments: Clear the super user with clearSuperUserBefore();
        clearSuperUserBefore();

        // Try grabbin that new information (grabbin those balls)
        User balls = new User(ctx, true);

        Assertions.assertEquals("Alice", balls.getName());
        Assertions.assertEquals("a@b.c", balls.getEmail());
        Assertions.assertEquals("123", balls.getPhone());
        Assertions.assertEquals(UserType.Organizer, balls.getType());
    }

    @Test
    void addRegEvent_addsAndSorts_uniqueOnly() {
        User u = new User();

        Assertions.assertTrue(u.addRegEvent("Bongus"));
        Assertions.assertTrue(u.addRegEvent("Bingus"));
        Assertions.assertFalse(u.addRegEvent("Bongus")); // duplicate ignored

        // Sorted them stinkers
        Assertions.assertEquals(List.of("Bingus", "Bongus"), u.getRegEvents());
    }

    @Test
    void findRegEvent_trueIfPresent_falseOtherwise() {
        User u = new User();
        u.addRegEvent("Bingus");
        Assertions.assertTrue(u.findRegEvent("Bingus"));
        Assertions.assertFalse(u.findRegEvent("Bongus"));
    }

    @Test
    void removeRegEvent_removesWhenPresent_andFalseWhenMissing() {
        User u = new User();
        u.addRegEvent("E1");
        u.addRegEvent("E2");

        Assertions.assertTrue(u.removeRegEvent("E1"));
        Assertions.assertFalse(u.findRegEvent("E1"));
        Assertions.assertFalse(u.removeRegEvent("nope"));
        Assertions.assertEquals(List.of("E2"), u.getRegEvents());
    }

    @Test
    void setRegEventStatus_updatesStatus() throws Exception {
        User u = new User();
        u.addRegEvent("R1");
        u.addRegEvent("RDR2");
        u.setRegEventStatus("R1", Status.Invited);

        // Cuz we can't access registeredEvent :(
        List<?> regEvents = (List<?>) getPrivate(u, "regEvents");
        Object first = regEvents.get(0);
        Field status = first.getClass().getDeclaredField("status");
        status.setAccessible(true);
        Assertions.assertEquals(Status.Invited, status.get(first));
    }

    @Test
    void setRegEventStatus_throwsIfMissing() {
        User u = new User();
        Assertions.assertThrows(NoSuchFieldException.class,
                () -> u.setRegEventStatus("missing", Status.Waitlisted));
    }

    @Test
    void setRegEvents_acceptsPreMadeList() throws Exception {
        User u = new User();

        // This shit wild, I had no clue any of this existed before my sleep deprived deep dive
        Class<?> regEventClass = null;
        for (Class<?> c : User.class.getDeclaredClasses()) {
            if ("RegisteredEvent".equals(c.getSimpleName())) {
                regEventClass = c;
                break;
            }
        }
        Assertions.assertNotNull(regEventClass);

        // Use constructor to make regevent instances outside of User class
        Constructor<?> constructor = regEventClass.getDeclaredConstructor(User.class, String.class);
        constructor.setAccessible(true);
        Object e1 = constructor.newInstance(u, "B");
        Object e2 = constructor.newInstance(u, "A");

        // Make a list of em like we would pull from firebase
        List<Object> list = new ArrayList<>();
        list.add(e1);
        list.add(e2);

        User.class.getDeclaredMethod("setRegEvents", List.class).invoke(u, list);

        // Shouldn't sort it since pulling from firebase would assume it was sorted
        Assertions.assertEquals(List.of("B", "A"), u.getRegEvents());
    }


    @Test
    void sharedString_getSet_reflective() throws Exception {
        User u = new User();

        Class<?> ssClass = null;
        for (Class<?> c : User.class.getDeclaredClasses()) {
            if ("SharedString".equals(c.getSimpleName())) {
                ssClass = c; break;
            }
        }
        Assertions.assertNotNull(ssClass);

        Constructor<?> constructor = ssClass.getDeclaredConstructor(User.class, String.class);
        constructor.setAccessible(true);
        Object shared = constructor.newInstance(u, "init");

        Method get = ssClass.getDeclaredMethod("get");
        Method set = ssClass.getDeclaredMethod("set", String.class);
        get.setAccessible(true);
        set.setAccessible(true);

        Assertions.assertEquals("init", get.invoke(shared));
        set.invoke(shared, "next");
        Assertions.assertEquals("next", get.invoke(shared));
    }
}
