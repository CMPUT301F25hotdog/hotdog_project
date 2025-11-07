package com.hotdog.elotto;

import static org.junit.jupiter.api.Assertions.*;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.test.core.app.ApplicationProvider;

import com.hotdog.elotto.controller.EventCreationController;
import com.hotdog.elotto.controller.QRCodeController;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.repository.EventRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;

public class QRCodeControllerTests {
    private Context context;
    private QRCodeController controller;
    @BeforeEach
    void setUp() {
        context = ApplicationProvider.getApplicationContext();
        controller = new QRCodeController(context);
    }
    @Test
    void testGenerateQRCode(){
        String data = "hola";
        Bitmap qr = QRCodeController.generateQRCode(data,100,100);
        assertNotNull(qr);
        assertEquals(100,qr.getHeight());
        assertEquals(100,qr.getWidth());
    }
    @Test
    void testDownloadQRCode(){
        Bitmap qr = QRCodeController.generateQRCode("https://example.com", 100, 100);
        boolean result = controller.downloadQR(qr, "test_qr");
        assertTrue(result, "downloadQR should return true if QR code was 'saved'");
    }
}
