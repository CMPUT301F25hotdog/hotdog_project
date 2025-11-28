package com.hotdog.elotto.ui.qr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.zxing.ResultPoint;
import com.hotdog.elotto.R;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.repository.EventRepository;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.Date;
import java.util.List;

/**
 * Fragment that lets entrants scan an event QR code and opens the event details page.
 */
public class QRScannerFragment extends Fragment {

    private static final int REQ_CAMERA = 123;
    private static final String TAG = "QRScannerFragment";

    private DecoratedBarcodeView barcodeView;
    private TextView statusTextView;
    private EventRepository eventRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_qr_scanner, container, false);

        barcodeView = root.findViewById(R.id.barcode_scanner);
        statusTextView = root.findViewById(R.id.tv_status);
        eventRepository = new EventRepository();

        // Back button behaviour
        root.findViewById(R.id.btn_back).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        checkCameraPermissionAndStart();
        return root;
    }


    private void checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
        }
    }

    private void startScanning() {
        statusTextView.setText("Scanning for event QR code...");

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result == null || result.getText() == null) return;

                // Avoid multiple triggers while we process one result
                barcodeView.pause();
                handleScanResult(result.getText());
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Not needed
            }
        });

        barcodeView.resume();
    }


    /**
     * Handle a scanned QR string.
     * 1) Try to load event by qrCodeData field
     * 2) If that fails, treat it as (or containing) an eventId.
     */
    private void handleScanResult(String rawValue) {
        String qrCodeData = rawValue != null ? rawValue.trim() : "";
        Log.d(TAG, "Scanned value: " + qrCodeData);

        if (qrCodeData.isEmpty()) {
            showErrorAndResume("Invalid QR code");
            return;
        }

        statusTextView.setText("Looking up event...");

        // First try: events where qrCodeData matches exactly
        eventRepository.getEventByQrCodeData(qrCodeData, new FirestoreCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                if (event != null) {
                    openEventIfValid(event);
                } else {
                    // No direct match → assume QR encodes an event ID
                    tryLookupByEventId(qrCodeData);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.w(TAG, "getEventByQrCodeData failed: " + errorMessage);
                tryLookupByEventId(qrCodeData);
            }
        });
    }

    /**
     * Fallback: assume the QR text *is* or *contains* the event ID.
     * Supports both "event:<id>" and plain "<id>".
     */
    private void tryLookupByEventId(String qrCodeData) {
        String eventId = qrCodeData;

        // If your generator uses a prefix like "event:<id>", strip it here
        final String PREFIX = "event:";
        if (eventId.startsWith(PREFIX)) {
            eventId = eventId.substring(PREFIX.length());
        }

        if (eventId.isEmpty()) {
            showErrorAndResume("Invalid QR code");
            return;
        }

        Log.d(TAG, "Fallback lookup by eventId: " + eventId);

        eventRepository.getEventById(eventId, new FirestoreCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                if (event == null) {
                    showErrorAndResume("No event matches this QR code.");
                } else {
                    openEventIfValid(event);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading event by ID: " + errorMessage);
                showErrorAndResume("Invalid or expired QR code");
            }
        });
    }

    /**
     * Apply basic rules (registration open, not in past, status OPEN),
     * then navigate to EventDetailsFragment.
     */
    private void openEventIfValid(Event event) {
        boolean registrationOpen = event.isRegistrationOpen();
        String status = event.getStatus();      // "OPEN", "CLOSED", "FULL", "COMPLETED"
        Date eventDate = event.getEventDateTime();
        boolean inPast = (eventDate != null && eventDate.before(new Date()));

        if (!"OPEN".equals(status) || !registrationOpen || inPast) {
            showErrorAndResume("This event is no longer available.");
            return;
        }

        statusTextView.setText("Event found! Opening details...");

        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);

        NavHostFragment.findNavController(QRScannerFragment.this)
                .navigate(R.id.action_qrScannerFragment_to_eventDetailsFragment, bundle);
    }

    private void showErrorAndResume(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        statusTextView.setText(message + "  Try again.");
        barcodeView.resume();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            Toast.makeText(getContext(),
                    "Camera permission is required to scan QR codes.",
                    Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (barcodeView != null) barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeView != null) barcodeView.pause();
    }
}
