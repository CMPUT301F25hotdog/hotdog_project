package com.hotdog.elotto.callback;

/**
 * This is the basic callback interface for Firestore operations that return a single object.
 * Used for asynchronous retrieval of individual documents from Firebase.
 * URL: https://www.geeksforgeeks.org/firebase/firebase-detaching-callbacks/
 */
public interface FirestoreCallback<T> {
    void onSuccess(T result);
    void onError(String errorMessage);
}