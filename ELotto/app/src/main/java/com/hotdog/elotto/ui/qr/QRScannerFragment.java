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

import java.util.List;

/**
 * Fragment that lets a user scan an event QR code and then automatically
 * opens the corresponding event details screen.
 *
 * <p>The flow is:
 * <ol>
 *     <li>Request camera permission if needed.</li>
 *     <li>Continuously scan QR codes using {@link DecoratedBarcodeView}.</li>
 *     <li>Treat the scanned text as a Firestore event ID (or {@code event:<id>}).</li>
 *     <li>Load the {@link Event} from Firestore via {@link EventRepository}.</li>
 *     <li>Navigate to {@code EventDetailsFragment} with that Event.</li>
 * </ol>
 */

public class QRScannerFragment extends Fragment {

    private static final int REQ_CAMERA = 123;
    private static final String TAG = "QRScannerFragment";

    private DecoratedBarcodeView barcodeView;
    private TextView statusTextView;
    private EventRepository eventRepository;

    /**
     * Inflates the QR scanner layout and sets up basic view references.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_qr_scanner, container, false);

        barcodeView = root.findViewById(R.id.barcode_scanner);
        statusTextView = root.findViewById(R.id.tv_status);
        eventRepository = new EventRepository();

        root.findViewById(R.id.btn_back).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        checkCameraPermissionAndStart();
        return root;
    }

    /**
     * Checks if the camera permission is granted. If yes, starts scanning.
     * Otherwise, requests the permission from the user.
     */
    private void checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
        }
    }

    /**
     * Configures the barcode view for continuous decoding and updates the
     * status text while waiting for a QR code.
     */
    private void startScanning() {
        statusTextView.setText("Scanning for event QR code...");

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result == null || result.getText() == null) return;

                barcodeView.pause();

                String rawValue = result.getText().trim();
                Log.d(TAG, "Scanned value: " + rawValue);

                handleScanResult(rawValue);
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
            }
        });

        barcodeView.resume();
    }

    /**
     * Handles a scanned QR string by interpreting it as an event ID (or {@code event:<id>}),
     * then loading that event from Firestore and opening the event details.
     *
     * @param rawValue the raw text encoded in the QR code
     */
    private void handleScanResult(String rawValue) {
        if (rawValue == null || rawValue.isEmpty()) {
            showErrorAndResume("Invalid QR code");
            return;
        }

        final String PREFIX = "event:";
        String eventId = rawValue.startsWith(PREFIX)
                ? rawValue.substring(PREFIX.length())
                : rawValue;

        if (eventId.isEmpty()) {
            showErrorAndResume("Invalid QR code");
            return;
        }

        statusTextView.setText("Looking up event...");

        eventRepository.getEventById(eventId, new FirestoreCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                if (!isAdded()) return;

                if (event == null) {
                    showErrorAndResume("No event matches this QR code.");
                    return;
                }

                openEventScreen(event);
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;

                Log.e(TAG, "Error loading event by ID: " + errorMessage);
                showErrorAndResume("Invalid or expired QR code");
            }
        });
    }

    /**
     * Navigates to {@code EventDetailsFragment} with the given Event
     * passed as a Serializable argument.
     *
     * @param event the event that was resolved from the scanned QR code
     */
    private void openEventScreen(Event event) {
        statusTextView.setText("Event found! Opening details...");

        Bundle bundle = new Bundle();
        // "event" MUST match the argument name in nav_graph for eventDetailsFragment
        bundle.putSerializable("event", event);

        NavHostFragment.findNavController(QRScannerFragment.this)
                .navigate(R.id.action_qrScannerFragment_to_eventDetailsFragment, bundle);
    }

    /**
     * Shows a short error message and resumes scanning so the user
     * can try another QR code.
     *
     * @param message the error text to show
     */
    private void showErrorAndResume(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        statusTextView.setText(message + "  Try again.");
        barcodeView.resume();
    }

    /**
     * Handles the result of the camera permission request.
     */
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
