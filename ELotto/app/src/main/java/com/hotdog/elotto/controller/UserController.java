package com.hotdog.elotto.controller;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.UserRepository;

/**
 * A controller class to update the UserRepository model for a single user.
 * @author Layne Pitman
 * @version 1.0.0
 */
public class UserController {
    /**
     * Reference to the user of this controller that will be used in the model updating.
     */
    User user;

    /**
     * Instantiate the controller with a single user.
     * @param user The user to be referenced by this controller.
     */
    public UserController(User user) {
        this.user=user;
    }

    /**
     * Updates the information of the current user in firebase.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateUser() {
        UserRepository.getInstance().updateUser(user,
                new OperationCallback() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onError(String errorMessage) {}
                });
    }


}
