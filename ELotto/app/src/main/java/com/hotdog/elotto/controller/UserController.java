package com.hotdog.elotto.controller;

import static java.lang.Thread.sleep;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.helpers.UserStatus;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.UserRepository;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for managing user update operations with retry logic.
 *
 * <p>This controller handles updating user data in Firestore with built-in error
 * handling and retry mechanisms. It ensures data integrity by verifying user existence
 * before updates and automatically retrying failed operations until successful.</p>
 *
 * <p>The controller uses background threads to perform updates asynchronously and
 * implements a retry mechanism that waits 60 seconds between failed attempts.</p>
 *
 * <p>Controller layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Layne Pitman
 * @version 1.0.0
 */
public class UserController {
    /**
     * The user object to be updated in the repository.
     */
    User user;

    /**
     * Executor for running update operations on a background thread.
     */
    Executor updateThread = Executors.newSingleThreadExecutor();

    /**
     * Latch for synchronizing update operations.
     */
    CountDownLatch updateLatch = new CountDownLatch(1);

    /**
     * Flag indicating whether an update should be performed.
     */
    Boolean doUpdate = true;

    /**
     * Flag indicating whether the update was successful.
     */
    Boolean updateSuccess = false;

    /**
     * Constructs a new UserController for the specified user.
     *
     * @param user the user to be managed by this controller
     */
    public UserController(User user) {
        this.user=user;
    }

    /**
     * Starts the asynchronous update cycle for the user.
     *
     * <p>This method executes the update operation on a background thread. In the event
     * that updating the user results in an error, the update will continue to retry
     * automatically until successful or the app closes.</p>
     *
     * <p><b>Note:</b> This method returns immediately and performs updates asynchronously.</p>
     */
    public void updateUser() {
        updateThread.execute(this::attemptUpdate);
    }

    /**
     * Attempts to update the user with automatic retry logic.
     *
     * <p><b>WARNING:</b> This method <b>WILL</b> block the thread it runs on and is
     * intended only for internal use by the UserController on the background thread.</p>
     *
     * <p>This method performs the following operations:</p>
     * <ol>
     *     <li>If user existence is unknown, attempts to fetch user from Firestore</li>
     *     <li>Merges fetched data with local user object if found</li>
     *     <li>Attempts to update user in Firestore</li>
     *     <li>On failure, waits 60 seconds and retries</li>
     *     <li>Continues retry loop until successful</li>
     * </ol>
     *
     * <p>This retry mechanism maintains data integrity by ensuring the user actually
     * exists before overwriting data, and guarantees eventual consistency by retrying
     * failed operations.</p>
     *
     * <p>The method uses reflection to update the user's status field, which is
     * otherwise private to maintain encapsulation.</p>
     */
    private void attemptUpdate() {
        while (!updateSuccess) {// If the user was never able to be properly fetched, we retry to make sure we aren't overriding data.
            if (this.user.exists() == UserStatus.Error) {
                UserRepository.getInstance().getUserById(user.getId(), new FirestoreCallback<User>() {
                    @Override
                    public void onSuccess(User result) {
                        user.Merge(result);
                        try {
                            // Status should only ever be accessed publicly here
                            Field field = User.class.getDeclaredField("status");
                            field.setAccessible(true);
                            field.set(user, UserStatus.Existent);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                        doUpdate = true;
                        updateLatch.countDown();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // If the db was accessed and user wasn't there, update, otherwise an error occurred and we wait.
                        doUpdate = errorMessage.toLowerCase().contains("user not found");
                        updateLatch.countDown();
                    }
                });
                // Wait for result of user fetch
                try {
                    updateLatch.await();
                } catch (InterruptedException e) {
                    // If we are interrupted before getting our result we play it safe
                    updateSuccess = false;
                    continue;
                }
            }

            // If we hit an error and can't confirm whether the user existed or not, we return
            // Assume we are updating
            if (!doUpdate) return;
            updateLatch = new CountDownLatch(1);
            UserRepository.getInstance().updateUser(user, new OperationCallback() {
                @Override
                public void onSuccess() {
                    // Update the user status to existent if we successfully set their info
                    if (user.exists() == UserStatus.Nonexistent) {
                        try {
                            Field field = User.class.getDeclaredField("status");
                            field.setAccessible(true);
                            field.set(user, UserStatus.Existent);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    updateSuccess = true;
                    updateLatch.countDown();
                }

                @Override
                public void onError(String errorMessage) {
                    // We don't finish this thread and continue to attempt periodically until we are successful
                    updateSuccess = false;
                }
            });
            // Again wait for confirmation of update
            try {
                updateLatch.await();
            } catch (InterruptedException e) {
                // If we are interrupted before getting our result we play it safe
                updateSuccess = false;
                continue;
            }

            if(!updateSuccess) {
                // If we are interrupted here then we can just try again
                try {
                    sleep(60000);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}