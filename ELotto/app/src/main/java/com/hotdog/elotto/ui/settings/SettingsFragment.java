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
 * Handles the Settings screen — lets users manage notification preferences
 * for organizer and admin messages stored in Firestore.
 * Loads saved preferences and updates them when toggled.
 */
public class SettingsFragment extends Fragment {

    SwitchMaterial switchOrganizer, switchAdmin;
    ImageButton backButton;
    FirebaseFirestore db;
    String userId;
    DocumentReference ref;

    /**
     * Sets up the Settings screen UI, loads user preferences,
     * and manages toggle actions.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        switchOrganizer = v.findViewById(R.id.switchOrganizer);
        switchAdmin = v.findViewById(R.id.switchAdmin);
        backButton = v.findViewById(R.id.backButton);

        // ✅ Fixed navigation issue (use v instead of this)
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
     * If not found, sets default values (both false).
     */
    void loadPrefs() {
        ref.get().addOnSuccessListener(d -> {
            if (d.exists()) {
                Boolean o = d.getBoolean("organizerNotifications");
                Boolean a = d.getBoolean("adminNotifications");
                switchOrganizer.setChecked(o != null && o);
                switchAdmin.setChecked(a != null && a);
            } else {
                createDefaults();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to load preferences.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Saves a single preference to Firestore when a switch is toggled.
     * Updates happen instantly in Firestore.
     */
    void savePref(String field, boolean value) {
        ref.update(field, value)
                .addOnSuccessListener(x -> {
                    String type = field.equals("organizerNotifications") ? "Organizer" : "Admin";
                    Toast.makeText(getContext(), type + " " + (value ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to save setting.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Creates default notification fields if none exist.
     * Organizer and admin notifications are off by default.
     */
    void createDefaults() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("organizerNotifications", false);
        defaults.put("adminNotifications", false);

        ref.set(defaults, SetOptions.merge())
                .addOnSuccessListener(x ->
                        Toast.makeText(getContext(), "Default settings created.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to initialize defaults.", Toast.LENGTH_SHORT).show());
    }
}
