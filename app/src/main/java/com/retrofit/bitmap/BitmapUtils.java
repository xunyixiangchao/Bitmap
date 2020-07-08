package com.retrofit.bitmap;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by lis on 2020/7/3.
 */
public class BitmapUtils {

    public static Bitmap rotate(Bitmap bitmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.setRotate(angle);
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return bitmap1;
    }

    public static int calculateInSameSize(int bitmapWidth, int bitmapHeight, int maxWidth, int maxHeight) {
        int inSameSize = 1;
        if (bitmapWidth > maxWidth && bitmapHeight > maxHeight) {
            inSameSize = 2;
            while (bitmapWidth / inSameSize > maxWidth && bitmapHeight / inSameSize > maxHeight) {
                inSameSize *= 2;
            }
        }
        return inSameSize;
    }

    public static int calculateInSameSize(float mScale) {
        float temp = 1.0f / mScale;
        int inSampleSize = 1;
        if (temp > 1) {
            inSampleSize = (int) Math.pow(2, (int) (temp));
        } else {
            inSampleSize = 1;
        }
        return inSampleSize;
    }
}
