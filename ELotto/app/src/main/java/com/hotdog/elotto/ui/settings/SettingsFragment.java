package com.hotdog.elotto.ui.settings;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.*;
import com.hotdog.elotto.R;
import com.hotdog.elotto.model.User;
import java.util.*;

/**
 * Manages the Settings screen — handles notification preferences
 * for organizer and admin updates stored in Firestore.
 * Loads saved values and updates them when switches are toggled.
 */
public class SettingsFragment extends Fragment {

    SwitchMaterial switchOrganizer, switchAdmin;
    ImageButton backButton;
    FirebaseFirestore db;
    String userId;
    DocumentReference ref;

    /**
     * Sets up the Settings screen UI, gets the user ID,
     * loads existing preferences, and handles switch toggles.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        switchOrganizer = v.findViewById(R.id.switchOrganizer);
        switchAdmin = v.findViewById(R.id.switchAdmin);
        backButton = v.findViewById(R.id.backButton);

        backButton.setOnClickListener(x -> NavHostFragment.findNavController(this).popBackStack());

        db = FirebaseFirestore.getInstance();
        User u = new User(requireContext(), true);
        userId = u.getId();

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            return v;
        }

        ref = db.collection("users").document(userId);
        loadPrefs();

        switchOrganizer.setOnCheckedChangeListener((b, c) -> savePref("organizerNotifications", c));
        switchAdmin.setOnCheckedChangeListener((b, c) -> savePref("adminNotifications", c));
        return v;
    }

    /**
     * Loads the current user's notification preferences from Firestore.
     * If none exist, creates default values (false for both).
     */
    void loadPrefs() {
        ref.get().addOnSuccessListener(d -> {
            if (d.exists()) {
                Boolean o = d.getBoolean("organizerNotifications");
                Boolean a = d.getBoolean("adminNotifications");
                switchOrganizer.setChecked(o != null && o);
                switchAdmin.setChecked(a != null && a);
            } else createDefaults();
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Load fail", Toast.LENGTH_SHORT).show());
    }

    /**
     * Saves a single preference field to Firestore when toggled.
     * Updates the backend in real time and shows a toast.
     */
    void savePref(String f, boolean v) {
        ref.update(f, v)
                .addOnSuccessListener(x -> {
                    String t = f.equals("organizerNotifications") ? "Organizer" : "Admin";
                    Toast.makeText(getContext(), t + " " + (v ? "on" : "off"), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Save fail", Toast.LENGTH_SHORT).show());
    }

    /**
     * Creates default preference fields if the user document doesn’t exist yet.
     * Both organizer and admin notifications are set to false by default.
     */
    void createDefaults() {
        Map<String, Object> m = new HashMap<>();
        m.put("organizerNotifications", false);
        m.put("adminNotifications", false);
        ref.set(m, SetOptions.merge())
                .addOnSuccessListener(x ->
                        Toast.makeText(getContext(), "Defaults made", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Init fail", Toast.LENGTH_SHORT).show());
    }
}
