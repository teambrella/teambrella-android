package com.teambrella.android.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * QRCode Utils
 */
public class QRCodeUtils {

    public static Bitmap createBitmap(String content, int fillColor, int backgroundColor) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 256, 256);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? fillColor : backgroundColor);
                }
            }

            return bmp;
        } catch (WriterException e) {
        }
        return null;
    }

    public static Bitmap createBitmap(String content) {
        return createBitmap(content, Color.BLACK, Color.WHITE);
    }
}
