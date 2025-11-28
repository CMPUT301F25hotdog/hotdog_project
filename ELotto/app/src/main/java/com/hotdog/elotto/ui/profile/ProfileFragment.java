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
 * Profile screen — View + Edit basic user info.
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
        btnEdit = v.findViewById(R.id.btn_edit);
        backButton = v.findViewById(R.id.btn_back);

        if (backButton != null) {
            backButton.setOnClickListener(
                    b -> NavHostFragment.findNavController(this).popBackStack()
            );
        }

        loadUserData();

        // BOTTOM save button (actual saving)
        btnSave.setOnClickListener(s -> {
            updateUserInfo();
            setEditingMode(false);
        });

        btnDelete.setOnClickListener(d -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account?")
                    .setMessage("Are you sure you want to permanently delete your account?")
                    .setPositiveButton("Yes", (dialog, w) -> deleteUserAccount())
                    .setNegativeButton("Cancel", (dialog, w) -> dialog.dismiss())
                    .show();
        });

        // TOP edit button (toggles edit mode, NO saving)
        btnEdit.setOnClickListener(e -> {
            if (!isEditing) {
                setEditingMode(true);
            }
        });

        // start view-mode
        setEditingMode(false);

        return v;
    }

    /**
     * Enable/disable editing mode: fields focusable + edit button disabled while editing.
     */
    void setEditingMode(boolean enable) {
        isEditing = enable;

        setEditable(inputName, enable);
        setEditable(inputEmail, enable);
        setEditable(inputPhone, enable);

        if (enable) {
            btnEdit.setEnabled(false);   // goes "hard"
            btnEdit.setAlpha(0.4f);
        } else {
            btnEdit.setEnabled(true);
            btnEdit.setAlpha(1f);
        }
    }

    private void setEditable(EditText field, boolean enable) {
        field.setEnabled(enable);
        field.setFocusable(enable);
        field.setFocusableInTouchMode(enable);
    }

    /**
     * Load user data.
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
     * Save user changes.
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

        db.collection("users").document(currentUserId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(x ->
                        Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(er ->
                        Toast.makeText(getContext(), "Fail " + er.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Remove account from Firestore.
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