package com.hotdog.elotto;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.AdminNotificationAdapter;
import com.hotdog.elotto.model.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * View notification logs from Firebase (READ ONLY)
 */
public class BrowseNotificationLogsActivity extends AppCompatActivity {

    private static final String TAG = "BrowseNotifications";

    private ImageView btnBack;
    private EditText searchNotifications;
    private TextView textTotalNotificationsCount;
    private RecyclerView recyclerNotifications;
    private LinearLayout emptyState;

    private AdminNotificationAdapter notificationAdapter;
    private List<Notification> allNotifications;
    private List<Notification> filteredNotifications;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_notification_logs);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        loadNotifications();
        setupSearchListener();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        searchNotifications = findViewById(R.id.search_notifications);
        textTotalNotificationsCount = findViewById(R.id.text_total_notifications_count);
        recyclerNotifications = findViewById(R.id.recycler_notifications);
        emptyState = findViewById(R.id.empty_state);
    }

    private void setupRecyclerView() {
        allNotifications = new ArrayList<>();
        filteredNotifications = new ArrayList<>();

        notificationAdapter = new AdminNotificationAdapter(this, filteredNotifications, notification -> {
            Toast.makeText(BrowseNotificationLogsActivity.this,
                    "Notification: " + notification.getMessage(), Toast.LENGTH_SHORT).show();
        });

        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotifications.setAdapter(notificationAdapter);
    }

    private void loadNotifications() {
        db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allNotifications.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Map<String, Object> data = doc.getData();

                            Notification notification = new Notification();
                            notification.setId(doc.getId());

                            if (data.containsKey("eventId")) {
                                notification.setEventId((String) data.get("eventId"));
                            }

                            if (data.containsKey("message")) {
                                notification.setMessage((String) data.get("message"));
                            }

                            if (data.containsKey("read")) {
                                notification.setRead((Boolean) data.get("read"));
                            }

                            if (data.containsKey("timestamp")) {
                                Object timestampObj = data.get("timestamp");
                                if (timestampObj instanceof Timestamp) {
                                    notification.setTimestamp(((Timestamp) timestampObj).toDate().getTime());
                                } else if (timestampObj instanceof Long) {
                                    notification.setTimestamp((Long) timestampObj);
                                }
                            }

                            if (data.containsKey("userId")) {
                                notification.setUserId((String) data.get("userId"));
                            }

                            allNotifications.add(notification);

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing notification: " + doc.getId(), e);
                        }
                    }
                    filteredNotifications.clear();
                    filteredNotifications.addAll(allNotifications);
                    updateUI();
                    Log.d(TAG, "Loaded " + allNotifications.size() + " notifications");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading notifications", e);
                    Toast.makeText(this, "Error loading notifications", Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    private void setupSearchListener() {
        searchNotifications.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotifications(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterNotifications(String query) {
        filteredNotifications.clear();

        if (query.isEmpty()) {
            filteredNotifications.addAll(allNotifications);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Notification notification : allNotifications) {
                String message = notification.getMessage() != null ?
                        notification.getMessage().toLowerCase() : "";
                String eventId = notification.getEventId() != null ?
                        notification.getEventId().toLowerCase() : "";

                if (message.contains(lowerCaseQuery) || eventId.contains(lowerCaseQuery)) {
                    filteredNotifications.add(notification);
                }
            }
        }

        updateUI();
    }

    private void updateUI() {
        textTotalNotificationsCount.setText(filteredNotifications.size() + " total notifications");

        if (filteredNotifications.isEmpty()) {
            recyclerNotifications.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerNotifications.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }

        notificationAdapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }
}