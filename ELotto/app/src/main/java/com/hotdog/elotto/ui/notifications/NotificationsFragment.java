package com.hotdog.elotto.ui.notifications;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.NotificationAdapter;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.controller.NotificationController;
import com.hotdog.elotto.databinding.FragmentInboxBinding;
import com.hotdog.elotto.model.Notification;

import java.util.List;

/**
 * Fragment responsible for displaying the user's notifications.
 *
 * @author Layne Pitman
 * @version 1.0
 * @since 2025-11-26
 */
public class NotificationsFragment extends Fragment {

    private FragmentInboxBinding binding;
    private NotificationController notificationController;
    private NotificationAdapter adapter;
    private String userId;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentInboxBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize Controller
        notificationController = new NotificationController();

        // Get User ID (Device ID)
        userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Setup RecyclerView
        RecyclerView recyclerView = binding.recyclerViewNotifications;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter();
        recyclerView.setAdapter(adapter);

        // Initialize Repository for navigation
        com.hotdog.elotto.repository.EventRepository eventRepository = new com.hotdog.elotto.repository.EventRepository();

        // Setup Click Listener
        adapter.setOnNotificationClickListener(notification -> {
            if (!notification.isRead()) {
                notificationController.markAsRead(userId, notification.getUuid(), new OperationCallback() {
                    @Override
                    public void onSuccess() {
                        notification.setRead(true);
                        adapter.notifyDataSetChanged(); // Refresh UI to show as read
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e("NotificationsFragment", "Error marking as read: " + errorMessage);
                    }
                });
            }

            // Navigate to event details if eventId is present
            if (notification.getEventId() != null && !notification.getEventId().isEmpty()) {
                eventRepository.getEventById(notification.getEventId(),
                        new com.hotdog.elotto.callback.FirestoreCallback<com.hotdog.elotto.model.Event>() {
                            @Override
                            public void onSuccess(com.hotdog.elotto.model.Event event) {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("event", event);
                                androidx.navigation.Navigation.findNavController(requireView())
                                        .navigate(R.id.action_notificationsFragment_to_eventDetailsFragment, bundle);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.e("NotificationsFragment", "Error fetching event: " + errorMessage);
                                Toast.makeText(getContext(), "Event details not found", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Setup Back Button
        binding.inboxBackButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        // Load Notifications
        notificationController.loadNotifications(userId, new FirestoreListCallback<Notification>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                if (notifications.isEmpty()) {
                    binding.textNotifications.setVisibility(View.VISIBLE);
                    binding.textNotifications.setText("No notifications");
                    binding.recyclerViewNotifications.setVisibility(View.GONE);
                } else {
                    binding.textNotifications.setVisibility(View.GONE);
                    binding.recyclerViewNotifications.setVisibility(View.VISIBLE);
                    adapter.setNotifications(notifications);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("NotificationsFragment", "Error loading notifications: " + errorMessage);
                Toast.makeText(getContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}