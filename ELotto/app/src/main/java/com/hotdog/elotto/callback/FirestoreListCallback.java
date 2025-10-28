package com.hotdog.elotto.callback;

import java.util.List;

/**
 * The callback interface for Firestore operations that return a list of objects.
 * Used for asynchronous retrieval of multiple documents from Firebase.
 * URL: https://www.geeksforgeeks.org/firebase/firebase-detaching-callbacks/
 */
public interface FirestoreListCallback<T> {
    void onSuccess(List<T> results);
    void onError(String errorMessage);
}