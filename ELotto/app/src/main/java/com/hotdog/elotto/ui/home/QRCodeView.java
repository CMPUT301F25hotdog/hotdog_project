package com.hotdog.elotto.ui.home;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hotdog.elotto.R;
import com.hotdog.elotto.controller.QRCodeController;
/**
 * The {@code QRCodeView} class is an {@link AppCompatActivity} that displays a generated QR code
 * corresponding to a specific event. It allows users to view, download, and share the event's QR code.
 * <p>
 * This activity is launched after an event has been successfully created, displaying the event name
 * and generating a QR code using the event ID.
 * </p>
 *
 * <p><b>Features:</b></p>
 * <ul>
 *   <li>Generates a QR code from the event ID using {@link QRCodeController#generateQRCode(String, int, int)}</li>
 *   <li>Displays the generated QR code image on screen</li>
 *   <li>Allows downloading the QR code to the device gallery via {@link QRCodeController#downloadQR(Bitmap, String)}</li>
 *   <li>Provides UI elements for navigation and sharing (sharing currently unimplemented)</li>
 * </ul>
 *
 * <p><b>Related Classes:</b></p>
 * <ul>
 *   <li>{@link com.hotdog.elotto.controller.QRCodeController}</li>
 *   <li>{@link com.hotdog.elotto.ui.home.EventCreationView}</li>
 * </ul>
 *
 * @author
 *   Hotdog eLotto Development Team
 * @version 1.0
 */
public class QRCodeView extends AppCompatActivity {
    private TextView eventName;
    private Button downloadButton;
    private Button shareButton;
    private Button goBackButton;
    private Bitmap qrBitmap;
    private ImageView qrImage;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code_activity);
        eventName = findViewById(R.id.qr_code_event_name);
        downloadButton = findViewById(R.id.download_qr_button);
        shareButton = findViewById(R.id.Share_QR_Button);
        goBackButton = findViewById(R.id.Go_Back_Button);
        qrImage = findViewById(R.id.qr_code);
        String name = getIntent().getStringExtra("EVENT_NAME");
        String eventId = getIntent().getStringExtra("EVENT_ID");
        qrImage.post(() -> {
            int width = qrImage.getWidth();
            int height = qrImage.getHeight();

            qrBitmap = QRCodeController.generateQRCode(eventId, width, height);
            qrImage.setImageBitmap(qrBitmap);
        });
        eventName.setText(name);
        assert name != null;
        String formatedName = name
                .replaceAll("[\\\\/:*?\"<>|]", "_")  // illegal characters
                .replaceAll("\\s+", "_");
        downloadButton.setOnClickListener(v -> {
           if(qrBitmap != null){
                QRCodeController controller = new QRCodeController(this);
                controller.downloadQR(qrBitmap,formatedName);
           }
        });
        //todo, implement the back button
    }
}
