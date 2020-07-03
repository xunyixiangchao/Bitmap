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
}
