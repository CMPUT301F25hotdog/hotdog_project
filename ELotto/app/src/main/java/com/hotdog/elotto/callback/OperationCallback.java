package com.hotdog.elotto.callback;

/**
 * Callback interface for Firestore operations that don't return data.
 * Used for create, update, and delete operations that only indicate success or failure.
 * URL: https://www.geeksforgeeks.org/firebase/firebase-detaching-callbacks/
 */
public interface OperationCallback {
    void onSuccess();
    void onError(String errorMessage);
}