package com.hotdog.elotto.controller;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.OutputStream;

/**
 * Controller class responsible for generating and downloading QR codes.
 * <p>
 * This class provides two main functionalities:
 * <ul>
 *     <li>Generating a QR code bitmap from a given string of data.</li>
 *     <li>Saving a generated QR code bitmap to the device gallery.</li>
 * </ul>
 * It uses the ZXing library to encode data into a QR code format.
 */
public class QRCodeController {

    /** The encoded string used to generate the QR code (if applicable). */
    public static String encodedString;

    /** Context used for accessing system services such as {@link android.content.ContentResolver}. */
    private final Context context;

    /**
     * Constructs a {@code QRCodeController} with the provided context.
     *
     * @param context the context used to access content resolvers and system directories.
     */
    public QRCodeController(Context context) {
        this.context = context;
    }

    /**
     * Generates a QR code bitmap from the given data.
     * <
     * The QR code is generated using the ZXing library, which encodes the input string into a bitMatrix then to a bitmap
     *
     *
     * @param data   the string we want the qrcode to point to
     * @param width  the width of the generated bitmap.
     * @param height the height of the generated bitmap.
     * @return a bitmap representing the QR code, or null if the generation fails.
     *
     * https://stackoverflow.com/questions/8800919/how-to-generate-a-qr-code-for-an-android-application
     * used this to create QRCodes
     */
    public static Bitmap generateQRCode(String data, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Downloads and saves the provided QR code bitmap to the device’s gallery.
     *
     * The image is saved  and made visible to the gallery automatically using MediaStore. The image will be
     * stored in PNG format.
     *
     *
     * @param qrBitmap the QR code bitmap to save.
     * @param fileName the name of the file
     *
     * https://stackoverflow.com/questions/71729415/saving-an-image-and-displaying-it-in-gallery
     * used this to figure out how to use MediaStore to save images
     */
    public void downloadQR(Bitmap qrBitmap, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyAppQRs");

        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
