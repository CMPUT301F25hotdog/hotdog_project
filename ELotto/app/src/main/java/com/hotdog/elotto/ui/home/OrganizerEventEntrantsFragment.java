package com.hotdog.elotto.ui.home;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.EntrantAdapter;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.controller.OrganizerEventController;
import com.hotdog.elotto.model.EntrantInfo;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.repository.EventRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for organizers to manage event entrants.
 * Displays waiting list, selected, accepted, and cancelled entrants.
 * Allows organizers to run lottery draws and send notifications.
 *
 * <p>Implements the following user stories:</p>
 * <ul>
 *     <li>US 02.02.01: View list of entrants who joined event waiting list</li>
 *     <li>US 02.05.02: Set system to sample specified number of attendees</li>
 *     <li>US 02.07.01: Send notifications to all entrants on waiting list</li>
 * </ul>
 *
 * <p>View layer in MVC pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author [Your Name]
 * @version 1.0
 * @since 2025-11-24
 */
public class OrganizerEventEntrantsFragment extends Fragment {

    private static final String TAG = "OrganizerEventEntrants";
    private static final String ARG_EVENT_ID = "eventId";

    // UI Components
    private ImageButton btnBack;
    private Button btnEdit, btnMap;
    private ImageView ivEventImage;
    private TextView tvEventName, tvEventTime, tvEventLocation, tvEventSpots;
    private TabLayout tabLayout;
    private TextView tvWaitingHeader;
    private RecyclerView rvWaitingEntrants;
    private EditText etNumberToSelect;
    private Button btnRunLottery, btnDrawReplacements, btnSendNotification, btnExportCSV;
    private LinearLayout layoutNumberToSelect;
    // Data
    private String eventId;
    private Event currentEvent;
    private OrganizerEventController controller;
    private EntrantAdapter adapter;
    private List<EntrantInfo> currentEntrants;
    private String currentTab = "waiting";

    // Repository
    private EventRepository eventRepository;

    /**
     * Creates a new instance of the fragment with the event ID.
     *
     * @param eventId the ID of the event to manage
     * @return new fragment instance
     */
    public static OrganizerEventEntrantsFragment newInstance(String eventId) {
        OrganizerEventEntrantsFragment fragment = new OrganizerEventEntrantsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_event_entrants, container, false);

        // Get event ID from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }

        initializeViews(view);
        initializeControllers();
        setupRecyclerView();
        setupTabs();
        setupButtons();
        loadEventData();
        loadWaitingList();

        return view;
    }

    /**
     * Initializes all view components.
     */
    private void initializeViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnMap = view.findViewById(R.id.btnMap);
        ivEventImage = view.findViewById(R.id.ivEventImage);
        tvEventName = view.findViewById(R.id.tvEventName);
        tvEventTime = view.findViewById(R.id.tvEventTime);
        tvEventLocation = view.findViewById(R.id.tvEventLocation);
        tvEventSpots = view.findViewById(R.id.tvEventSpots);
        tabLayout = view.findViewById(R.id.tabLayout);
        tvWaitingHeader = view.findViewById(R.id.tvWaitingHeader);
        rvWaitingEntrants = view.findViewById(R.id.rvWaitingEntrants);
        etNumberToSelect = view.findViewById(R.id.etNumberToSelect);
        btnRunLottery = view.findViewById(R.id.btnRunLottery);
        btnSendNotification = view.findViewById(R.id.btnSendNotification);
        //new
        layoutNumberToSelect = view.findViewById(R.id.layoutNumberToSelect);
        btnDrawReplacements = view.findViewById(R.id.btnDrawReplacements);
        btnExportCSV = view.findViewById(R.id.btnExportCSV);
    }

    /**
     * Initializes controllers and repositories.
     */
    private void initializeControllers() {
        controller = new OrganizerEventController();
        eventRepository = new EventRepository();
        currentEntrants = new ArrayList<>();
    }

    /**
     * Sets up the RecyclerView with adapter and layout manager.
     */
    private void setupRecyclerView() {
        adapter = new EntrantAdapter(currentEntrants);
        rvWaitingEntrants.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWaitingEntrants.setAdapter(adapter);

        // Set cancel click listener
        adapter.setCancelClickListener((entrantInfo, position) -> {
            showCancelConfirmationDialog(entrantInfo, position);
        });
    }

    /**
     * Sets up tab selection listener.
     */
    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0: // Waiting
                        currentTab = "waiting";
                        adapter.setCurrentTab(currentTab);  // ← ADD THIS LINE
                        loadWaitingList();
                        showButtonsForTab(currentTab);
                        break;
                    case 1: // Selected
                        currentTab = "selected";
                        adapter.setCurrentTab(currentTab);  // ← ADD THIS LINE
                        loadSelectedList();
                        showButtonsForTab(currentTab);
                        break;

                    case 2: // Accepted
                        currentTab = "accepted";
                        adapter.setCurrentTab(currentTab);  // ← ADD THIS LINE
                        loadAcceptedList();
                        showButtonsForTab(currentTab);
                        break;

                    case 3: // Cancelled
                        currentTab = "cancelled";
                        adapter.setCurrentTab(currentTab);  // ← ADD THIS LINE
                        loadCancelledList();
                        showButtonsForTab(currentTab);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        // Start with first tab by default
        showButtonsForTab("waiting");
    }

    /**
     * Sets up button click listeners.
     */
    private void setupButtons() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnEdit.setOnClickListener(v -> {
            // TODO: Navigate to edit event screen
            Toast.makeText(getContext(), "Edit event - Coming soon", Toast.LENGTH_SHORT).show();
        });

        btnMap.setOnClickListener(v -> {
            // TODO: Navigate to map screen
            Toast.makeText(getContext(), "View map - Coming soon", Toast.LENGTH_SHORT).show();
        });

        btnRunLottery.setOnClickListener(v -> runLotteryDraw());
        btnDrawReplacements.setOnClickListener(v -> drawReplacements()); // new

        btnSendNotification.setOnClickListener(v -> showSendNotificationDialog());
        btnExportCSV.setOnClickListener(v -> exportToCSV());
    }

    private void showButtonsForTab(String tabType) {
        switch (tabType) {
            case "waiting":
                layoutNumberToSelect.setVisibility(View.VISIBLE);
                btnRunLottery.setVisibility(View.VISIBLE);
                btnDrawReplacements.setVisibility(View.GONE);
                btnExportCSV.setVisibility(View.GONE);
                break;
            case "selected":
                layoutNumberToSelect.setVisibility(View.GONE);
                btnRunLottery.setVisibility(View.GONE);
                btnDrawReplacements.setVisibility(View.VISIBLE);
                btnExportCSV.setVisibility(View.GONE);
                break;
            case "accepted":
                layoutNumberToSelect.setVisibility(View.GONE);
                btnRunLottery.setVisibility(View.GONE);
                btnDrawReplacements.setVisibility(View.GONE);
                btnExportCSV.setVisibility(View.VISIBLE);
                break;
            case "cancelled":
                layoutNumberToSelect.setVisibility(View.GONE);
                btnRunLottery.setVisibility(View.GONE);
                btnDrawReplacements.setVisibility(View.GONE);
                btnExportCSV.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * Loads event data for the header card.
     */
    private void loadEventData() {
        eventRepository.getEventById(eventId, new FirestoreCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                currentEvent = event;
                updateEventHeader(event);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Error loading event: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the event header card with event information.
     */
    private void updateEventHeader(Event event) {
        tvEventName.setText(event.getName());

        if (event.getEventDateTime() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, HH:mm", Locale.getDefault());
            tvEventTime.setText(dateFormat.format(event.getEventDateTime()));
        }

        tvEventLocation.setText(event.getLocation());
        tvEventSpots.setText(event.getMaxEntrants() + " Spots");

        // TODO: Load event image if available
        // For now, placeholder image is shown
    }

    /**
     * Loads the waiting list entrants.
     */
    private void loadWaitingList() {
        controller.loadWaitingListEntrants(eventId, new FirestoreListCallback<EntrantInfo>() {
            @Override
            public void onSuccess(List<EntrantInfo> entrants) {
                currentEntrants = entrants;
                adapter.updateList(currentEntrants);
                updateHeader("waiting", entrants.size());
                updateTabCount(0, entrants.size());
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Error loading waiting list: " + errorMessage, Toast.LENGTH_SHORT).show();
                currentEntrants = new ArrayList<>();
                adapter.updateList(currentEntrants);
            }
        });
    }

    /**
     * Loads the selected entrants.
     */
    private void loadSelectedList() {
        controller.loadSelectedEntrants(eventId, new FirestoreListCallback<EntrantInfo>() {
            @Override
            public void onSuccess(List<EntrantInfo> entrants) {
                currentEntrants = entrants;
                adapter.updateList(currentEntrants);
                updateHeader("selected", entrants.size());
                updateTabCount(1, entrants.size());
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Error loading selected list: " + errorMessage, Toast.LENGTH_SHORT).show();
                currentEntrants = new ArrayList<>();
                adapter.updateList(currentEntrants);
            }
        });
    }

    /**
     * Loads the accepted entrants.
     */
    private void loadAcceptedList() {
        controller.loadAcceptedEntrants(eventId, new FirestoreListCallback<EntrantInfo>() {
            @Override
            public void onSuccess(List<EntrantInfo> entrants) {
                currentEntrants = entrants;
                adapter.updateList(currentEntrants);
                updateHeader("accepted", entrants.size());
                updateTabCount(2, entrants.size());
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Error loading accepted list: " + errorMessage, Toast.LENGTH_SHORT).show();
                currentEntrants = new ArrayList<>();
                adapter.updateList(currentEntrants);
            }
        });
    }

    /**
     * Loads the cancelled entrants.
     */
    private void loadCancelledList() {
        controller.loadCancelledEntrants(eventId, new FirestoreListCallback<EntrantInfo>() {
            @Override
            public void onSuccess(List<EntrantInfo> entrants) {
                currentEntrants = entrants;
                adapter.updateList(currentEntrants);
                updateHeader("cancelled", entrants.size());
                updateTabCount(3, entrants.size());
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Error loading cancelled list: " + errorMessage, Toast.LENGTH_SHORT).show();
                currentEntrants = new ArrayList<>();
                adapter.updateList(currentEntrants);
            }
        });
    }

    /**
     * Updates the header text based on current tab and count.
     */
    private void updateHeader(String listType, int count) {
        String headerText = count + " entrants on " + listType + " list";
        tvWaitingHeader.setText(headerText);
    }

    /**
     * Updates the count in a specific tab.
     */
    private void updateTabCount(int tabIndex, int count) {
        TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
        if (tab != null) {
            String[] tabNames = {"Waiting", "Selected", "Accepted", "Cancelled"};
            tab.setText(tabNames[tabIndex] + " (" + count + ")");
        }
    }

    /**
     * Runs the lottery draw.
     */
    private void runLotteryDraw() {
        String numberText = etNumberToSelect.getText().toString().trim();

        if (numberText.isEmpty()) {
            Toast.makeText(getContext(), "Please enter number to select", Toast.LENGTH_SHORT).show();
            return;
        }

        int numberToSelect;
        try {
            numberToSelect = Integer.parseInt(numberText);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple clicks
        btnRunLottery.setEnabled(false);

        controller.runLotteryDraw(eventId, numberToSelect, new OperationCallback() {
            @Override
            public void onSuccess() {
                btnRunLottery.setEnabled(true);
                Toast.makeText(getContext(), "Lottery draw completed successfully!", Toast.LENGTH_LONG).show();
                etNumberToSelect.setText("");

                // Refresh both waiting and selected lists
                loadWaitingList();
                if (currentTab.equals("selected")) {
                    loadSelectedList();
                }
            }

            @Override
            public void onError(String errorMessage) {
                btnRunLottery.setEnabled(true);
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Shows dialog to send notification to current list.
     */
    private void showSendNotificationDialog() {
        if (currentEntrants == null || currentEntrants.isEmpty()) {
            Toast.makeText(getContext(), "No entrants to notify", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Send Notification");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("Enter notification message");
        input.setMinLines(3);
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String message = input.getText().toString().trim();
            if (!message.isEmpty()) {
                sendNotifications(message);
            } else {
                Toast.makeText(getContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Sends notifications to all entrants in the current list.
     */
    private void sendNotifications(String message) {
        List<String> userIds = new ArrayList<>();
        for (EntrantInfo entrantInfo : currentEntrants) {
            userIds.add(entrantInfo.getUserId());
        }

        controller.sendNotificationToEntrants(eventId, userIds, message, new OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Notifications sent to " + userIds.size() + " entrants", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Error sending notifications: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Exports accepted entrants list to CSV format.
     * US 02.06.05 implementation.
     */
    private void exportToCSV() {
        if (currentEntrants == null || currentEntrants.isEmpty()) {
            Toast.makeText(getContext(), "No entrants to export", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentEvent == null) {
            Toast.makeText(getContext(), "Event data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build CSV content
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("Name,Email,Status\n");  // CSV header

        for (EntrantInfo entrant : currentEntrants) {
            csvContent.append(entrant.getName()).append(",");
            csvContent.append(entrant.getEmail() != null ? entrant.getEmail() : "N/A").append(",");
            csvContent.append("Accepted\n");
        }

        // Create filename
        String fileName = currentEvent.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_accepted_entrants.csv";

        // Save file
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File csvFile = new File(downloadsDir, fileName);

            FileWriter writer = new FileWriter(csvFile);
            writer.write(csvContent.toString());
            writer.close();

            Toast.makeText(getContext(), "CSV saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();

            // Optional: Open share dialog
            shareCSVFile(csvFile);

        } catch (IOException e) {
            Toast.makeText(getContext(), "Error saving CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Opens share dialog for CSV file.
     */
    private void shareCSVFile(File file) {
        Uri fileUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                file
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Share CSV"));
    }

    /**
     * Draws replacement entrants from the waiting list.
     */
    private void drawReplacements() {
        if (currentEvent == null) {
            Toast.makeText(getContext(), "Event data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        int maxEntrants = currentEvent.getMaxEntrants();
        int currentAccepted = currentEvent.getAcceptedEntrantIds() != null ?
                currentEvent.getAcceptedEntrantIds().size() : 0;
        int spotsAvailable = maxEntrants - currentAccepted;

        if (spotsAvailable <= 0) {
            Toast.makeText(getContext(), "Event is full", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Draw Replacements");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Number of replacements (max " + spotsAvailable + ")");
        builder.setView(input);

        builder.setPositiveButton("Draw", (dialog, which) -> {
            String numberText = input.getText().toString().trim();
            if (!numberText.isEmpty()) {
                try {
                    int numberToSelect = Integer.parseInt(numberText);
                    if (numberToSelect > spotsAvailable) {
                        Toast.makeText(getContext(), "Cannot draw more than " + spotsAvailable, Toast.LENGTH_SHORT).show();
                    } else {
                        performReplacementDraw(numberToSelect);
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void performReplacementDraw(int numberToSelect) {
        btnDrawReplacements.setEnabled(false);

        controller.runLotteryDraw(eventId, numberToSelect, new OperationCallback() {
            @Override
            public void onSuccess() {
                btnDrawReplacements.setEnabled(true);
                Toast.makeText(getContext(), "Replacement draw completed!", Toast.LENGTH_LONG).show();
                loadWaitingList();
                loadSelectedList();
            }

            @Override
            public void onError(String errorMessage) {
                btnDrawReplacements.setEnabled(true);
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showCancelConfirmationDialog(EntrantInfo entrantInfo, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Entrant")
                .setMessage("Are you sure you want to cancel " + entrantInfo.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> cancelEntrant(entrantInfo, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelEntrant(EntrantInfo entrantInfo, int position) {
        List<String> userIds = new ArrayList<>();
        userIds.add(entrantInfo.getUserId());

        eventRepository.moveEntrantsToCancelled(eventId, userIds, new OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), entrantInfo.getName() + " cancelled", Toast.LENGTH_SHORT).show();
                currentEntrants.remove(position);
                adapter.notifyItemRemoved(position);
                updateTabCount(getCurrentTabIndex(), currentEntrants.size());
                updateHeader(currentTab, currentEntrants.size());
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getCurrentTabIndex() {
        switch (currentTab) {
            case "waiting": return 0;
            case "selected": return 1;
            case "accepted": return 2;
            case "cancelled": return 3;
            default: return 0;
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up references
        adapter = null;
        currentEntrants = null;
    }
}