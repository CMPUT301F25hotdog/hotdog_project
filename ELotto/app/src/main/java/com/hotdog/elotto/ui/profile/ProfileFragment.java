package com.hotdog.elotto.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.firestore.*;
import com.hotdog.elotto.R;
import com.hotdog.elotto.model.User;
import java.util.*;

/**
 * Handles the Profile screen — lets user view, edit, and delete profile info stored in Firestore.
 * Connects text fields with Firestore and updates data in real time.
 * Also shows a confirmation before deleting the account.
 */
public class ProfileFragment extends Fragment {

    EditText inputName, inputEmail, inputPhone;
    TextView deviceIdText;
    Button btnSave, btnDelete, btnEdit;
    ImageButton backButton;
    FirebaseFirestore db;
    User currentUser;
    boolean isEditing = false;
    String currentUserId;

    /**
     * Sets up the Profile screen: binds UI, loads user data,
     * and connects buttons for edit, save, and delete actions.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        db = FirebaseFirestore.getInstance();
        inputName = v.findViewById(R.id.input_name);
        inputEmail = v.findViewById(R.id.input_email);
        inputPhone = v.findViewById(R.id.input_phone);
        deviceIdText = v.findViewById(R.id.device_id_text);
        btnSave = v.findViewById(R.id.btn_save);
        btnDelete = v.findViewById(R.id.btn_delete);
        backButton = v.findViewById(R.id.btn_back);
        btnEdit = v.findViewById(R.id.btn_edit);

        if(backButton != null)
            backButton.setOnClickListener(b -> NavHostFragment.findNavController(this).popBackStack());

        loadUserData();

        btnSave.setOnClickListener(s -> updateUserInfo());
        btnDelete.setOnClickListener(d -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account?")
                    .setMessage("Are you sure you want to permanently delete your account?")
                    .setPositiveButton("Yes", (dialog, w) -> deleteUserAccount())
                    .setNegativeButton("Cancel", (dialog, w) -> dialog.dismiss())
                    .show();
        });

        btnEdit.setOnClickListener(e -> {
            if (!isEditing) {
                setEnabled(true);
                isEditing = true;
                btnEdit.setText("Save");
            } else {
                updateUserInfo();
                setEnabled(false);
                isEditing = false;
                btnEdit.setText("Edit");
            }
        });

        setEnabled(false);
        return v;
    }

    /**
     * Enables or disables input fields for editing.
     * Used to toggle between view and edit mode.
     */
    void setEnabled(boolean b) {
        inputName.setEnabled(b);
        inputEmail.setEnabled(b);
        inputPhone.setEnabled(b);
    }

    /**
     * Fetches the current user’s data from Firestore and displays it.
     * If no record found, shows a short error message.
     */
    void loadUserData() {
        currentUser = new User(requireContext(), true);
        currentUserId = currentUser.getId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        inputName.setText(doc.getString("name"));
                        inputEmail.setText(doc.getString("email"));
                        inputPhone.setText(doc.getString("phone"));
                        deviceIdText.setText("Device ID: " + currentUserId);
                    } else {
                        Toast.makeText(getContext(), "No data", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Fail " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Updates the user’s Firestore data (name, email, phone).
     * Validates input before saving and merges data without overwriting.
     */
    void updateUserInfo() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(getContext(), "Missing user id", Toast.LENGTH_SHORT).show();
            return;
        }
        String n = inputName.getText().toString().trim();
        String e = inputEmail.getText().toString().trim();
        String p = inputPhone.getText().toString().trim();
        if (TextUtils.isEmpty(n) || TextUtils.isEmpty(e)) {
            Toast.makeText(getContext(), "Fill name and email", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", n);
        data.put("email", e);
        data.put("phone", p);

        db.collection("users").document(currentUserId).set(data, SetOptions.merge())
                .addOnSuccessListener(x -> Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(er ->
                        Toast.makeText(getContext(), "Fail " + er.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Deletes the user’s Firestore record after confirmation.
     * Ends the activity once the user is deleted.
     */
    void deleteUserAccount() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(getContext(), "Missing id", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("users").document(currentUserId).delete()
                .addOnSuccessListener(x -> {
                    Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                })
                .addOnFailureListener(er ->
                        Toast.makeText(getContext(), "Fail " + er.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
