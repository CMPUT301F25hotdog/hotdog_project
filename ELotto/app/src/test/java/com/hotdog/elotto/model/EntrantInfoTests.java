package com.hotdog.elotto.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;


public class EntrantInfoTests {

    private EntrantInfo entrantInfo;
    private TestUser testUser;
    private Date testDate;

    /**
     * Simple test stub for User class to avoid Firebase dependencies.
     */
    static class TestUser extends User {
        private String testName;
        private String testEmail;
        private String testId;

        public TestUser(String name, String email, String id) {
            super();
            this.testName = name;
            this.testEmail = email;
            this.testId = id;
        }

        @Override
        public String getName() {
            return testName;
        }

        @Override
        public String getEmail() {
            return testEmail;
        }

        @Override
        public String getId() {
            return testId;
        }
    }

    @BeforeEach
    void setup() {
        // Create a test user
        testUser = new TestUser("Funny Guy", "john@example.com", "user123");


        testDate = new Date(1700000000000L);

        entrantInfo = new EntrantInfo(testUser, testDate);
    }



    @Test
    void testConstructorSetsFields() {
        assertNotNull(entrantInfo.getUser());
        assertNotNull(entrantInfo.getJoinedDate());
        assertEquals(testUser, entrantInfo.getUser());
        assertEquals(testDate, entrantInfo.getJoinedDate());
    }

    @Test
    void testDefaultConstructor() {
        EntrantInfo emptyEntrantInfo = new EntrantInfo();
        assertNotNull(emptyEntrantInfo);
        assertNull(emptyEntrantInfo.getUser());
        assertNull(emptyEntrantInfo.getJoinedDate());
    }

    @Test
    void testConstructorWithNullUser() {
        EntrantInfo nullUserInfo = new EntrantInfo(null, testDate);
        assertNull(nullUserInfo.getUser());
        assertEquals(testDate, nullUserInfo.getJoinedDate());
    }

    @Test
    void testConstructorWithNullDate() {
        EntrantInfo nullDateInfo = new EntrantInfo(testUser, null);
        assertEquals(testUser, nullDateInfo.getUser());
        assertNull(nullDateInfo.getJoinedDate());
    }


    @Test
    void testSetAndGetUser() {
        TestUser anotherUser = new TestUser("Joe Joe", "joe@example.com", "user456");

        entrantInfo.setUser(anotherUser);
        assertEquals(anotherUser, entrantInfo.getUser());
        assertEquals("Joe Joe", entrantInfo.getName());
    }

    @Test
    void testSetAndGetJoinedDate() {
        Date newDate = new Date(1800000000000L);
        entrantInfo.setJoinedDate(newDate);
        assertEquals(newDate, entrantInfo.getJoinedDate());
    }

    @Test
    void testSetUserToNull() {
        entrantInfo.setUser(null);
        assertNull(entrantInfo.getUser());
    }

    @Test
    void testSetJoinedDateToNull() {
        entrantInfo.setJoinedDate(null);
        assertNull(entrantInfo.getJoinedDate());
    }


    @Test
    void testGetNameWithValidUser() {
        assertEquals("Funny Guy", entrantInfo.getName());
    }

    @Test
    void testGetNameWithNullUser() {
        entrantInfo.setUser(null);
        assertEquals("Unknown", entrantInfo.getName());
    }

    @Test
    void testGetEmailWithValidUser() {
        assertEquals("john@example.com", entrantInfo.getEmail());
    }

    @Test
    void testGetEmailWithNullUser() {
        entrantInfo.setUser(null);
        assertEquals("", entrantInfo.getEmail());
    }

    @Test
    void testGetUserIdWithValidUser() {
        assertEquals("user123", entrantInfo.getUserId());
    }

    @Test
    void testGetUserIdWithNullUser() {
        entrantInfo.setUser(null);
        assertEquals("", entrantInfo.getUserId());
    }


    @Test
    void testUserWithNullName() {
        TestUser nullNameUser = new TestUser(null, "test@example.com", "user789");
        entrantInfo.setUser(nullNameUser);

        assertNull(entrantInfo.getName());
    }

    @Test
    void testUserWithEmptyEmail() {
        TestUser emptyEmailUser = new TestUser("Test User", "", "user789");
        entrantInfo.setUser(emptyEmailUser);

        assertEquals("", entrantInfo.getEmail());
    }

    @Test
    void testUserWithEmptyId() {
        TestUser emptyIdUser = new TestUser("Test User", "test@example.com", "");
        entrantInfo.setUser(emptyIdUser);

        assertEquals("", entrantInfo.getUserId());
    }



    @Test
    void testMultipleDateChanges() {
        // Original date
        assertEquals(testDate, entrantInfo.getJoinedDate());

        // Change to new date
        Date newDate = new Date(2000000000000L);
        entrantInfo.setJoinedDate(newDate);
        assertEquals(newDate, entrantInfo.getJoinedDate());

        // Change to null
        entrantInfo.setJoinedDate(null);
        assertNull(entrantInfo.getJoinedDate());

        // Change back to original
        entrantInfo.setJoinedDate(testDate);
        assertEquals(testDate, entrantInfo.getJoinedDate());
    }

    @Test
    void testSameDateForMultipleEntrants() {
        EntrantInfo entrant1 = new EntrantInfo(testUser, testDate);
        EntrantInfo entrant2 = new EntrantInfo(testUser, testDate);

        // Same date instance can be shared
        assertSame(testDate, entrant1.getJoinedDate());
        assertSame(testDate, entrant2.getJoinedDate());
    }

    @Test
    void testIndependentEntrantInfoInstances() {
        TestUser user2 = new TestUser("User 2", "user2@example.com", "user2");
        Date date2 = new Date(2000000000000L);

        EntrantInfo entrant1 = new EntrantInfo(testUser, testDate);
        EntrantInfo entrant2 = new EntrantInfo(user2, date2);

        // Changing one shouldn't affect the other
        entrant1.setUser(null);
        assertNull(entrant1.getUser());
        assertNotNull(entrant2.getUser());
    }

    @Test
    void testConvenienceMethodsReturnCorrectDefaults() {
        EntrantInfo emptyInfo = new EntrantInfo(null, null);

        assertEquals("Unknown", emptyInfo.getName());
        assertEquals("", emptyInfo.getEmail());
        assertEquals("", emptyInfo.getUserId());
        assertNull(emptyInfo.getJoinedDate());
    }
}