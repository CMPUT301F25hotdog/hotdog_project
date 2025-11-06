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
 * Displays QRCode Screen with all its buttons such as the share button, download button etc
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
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_");
        downloadButton.setOnClickListener(v -> {
           if(qrBitmap != null){
                QRCodeController controller = new QRCodeController(this);
                controller.downloadQR(qrBitmap,formatedName);
           }
        });
        goBackButton.setOnClickListener(v ->{
            finish();
        });
    }
}
