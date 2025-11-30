package com.hotdog.elotto.controller;

import static java.lang.Thread.sleep;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.helpers.UserStatus;
import com.hotdog.elotto.model.Organizer;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.OrganizerRepository;
import com.hotdog.elotto.repository.UserRepository;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A controller class to update the UserRepository model for a single org.
 * @author Layne Pitman
 * @version 1.0.0
 */
public class OrganizerController {
    /**
     * Reference to the org of this controller that will be used in the model updating.
     */
    Organizer org;
    Executor updateThread = Executors.newSingleThreadExecutor();
    CountDownLatch updateLatch = new CountDownLatch(1);
    Boolean doUpdate = false;
    Boolean updateSuccess = false;
    /**
     * Instantiate the controller with a single org.
     * @param org The organizer to be referenced by this controller.
     */
    public OrganizerController(Organizer org) {
        this.org=org;
    }

    /**
     * Updates the information of the current org in firebase.
     */
    public void updateOrganizer() {
        this.updateThread.execute(this::attemptUpdate);
    }

    /**
     * WARNING: This <b>WILL</b> block whatever thread it is in, and is intended only to be used internally by the UserController
     * This method will attempt confirm whether or not the org exists if we were unable to get their information prior, it will then attempt to update the org in the case that we were able to confirm whether or not they exist in Firestore. If either of these tasks fail, the thread will block for 1 minute and try again.
     * This is to maintain data integrity in the case that we encounter an error in fetching the org info and the org actually existed.
     */
    private void attemptUpdate() {
        while (!updateSuccess) {// If the org was never able to be properly fetched, we retry to make sure we aren't overriding data.
            if (this.org.exists() == UserStatus.Error)
                OrganizerRepository.getInstance().getOrganizerById(org.getId(), new FirestoreCallback<>() {
                    @Override
                    public void onSuccess(Organizer result) {
                        org.Merge(result);
                        try {
                            // Status should only ever be accessed publicly here
                            Field field = User.class.getDeclaredField("status");
                            field.setAccessible(true);
                            field.set(org, UserStatus.Existent);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                        doUpdate = true;
                        updateLatch.countDown();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // If the db was accessed and org wasn't there, update, otherwise an error occurred and we wait.
                        doUpdate = errorMessage.toLowerCase().contains("org not found");
                        updateLatch.countDown();
                    }
                });
            try {
                updateLatch.await();
            } catch (InterruptedException e) {
                // If we are interrupted before getting our result we play it safe
                updateSuccess = false;
                continue;
            }

            // If we hit an error and can't confirm whether the org existed or not, we return
            if (!doUpdate) return;
            updateLatch = new CountDownLatch(1);
            OrganizerRepository.getInstance().updateOrganizer(org, new OperationCallback() {
                @Override
                public void onSuccess() {
                    // Update the org status to existent if we successfully set their info
                    if (org.exists() == UserStatus.Nonexistent) {
                        try {
                            Field field = User.class.getDeclaredField("status");
                            field.setAccessible(true);
                            field.set(org, UserStatus.Existent);
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