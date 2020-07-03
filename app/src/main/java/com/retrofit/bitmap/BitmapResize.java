package com.retrofit.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lis on 2020/7/2.
 */
public class BitmapResize {

    private volatile static BitmapResize instance;

    public static BitmapResize getInstance() {
        if (instance == null) {
            synchronized (BitmapResize.class) {
                if (instance == null) {
                    instance = new BitmapResize();
                }
            }
        }
        return instance;
    }

    /**
     * 通过文件路径获取图片
     *
     * @param filePath
     * @param maxWidth
     * @param maxHeight
     * @param hasAlpha
     * @param reusable
     * @return
     */
    public static Bitmap resizeBitmapFromFile(String filePath, int maxWidth, int maxHeight, boolean hasAlpha, Bitmap reusable) {
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            if (is != null) {
                return resizeBitmapFromStream(is, maxWidth, maxHeight, hasAlpha, reusable);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 通过文件获取图片
     *
     * @param file
     * @param maxWidth
     * @param maxHeight
     * @param hasAlpha
     * @param reusable
     * @return
     */
    public static Bitmap resizeBitmapFromFile(File file, int maxWidth, int maxHeight, boolean hasAlpha, Bitmap reusable) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            if (is != null) {
                return resizeBitmapFromStream(is, maxWidth, maxHeight, hasAlpha, reusable);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * resource资源加载图片
     *
     * @param context
     * @param id
     * @param maxWidth
     * @param maxHeight
     * @param hasAlpha
     * @param reusable
     * @return
     */
    public static Bitmap resizeBitmapFromResource(Context context, int id, int maxWidth, int maxHeight, boolean hasAlpha, Bitmap reusable) {
        InputStream is = context.getResources().openRawResource(id);
        if (is != null) {
            return resizeBitmapFromStream(is, maxWidth, maxHeight, hasAlpha, reusable);
        }
        return null;
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        //
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(context.getResources(), id, options);
//        int bitmapWidth = options.outWidth;
//        int bitmapHeight = options.outHeight;
//        options.inSampleSize = calculateInSameSize(bitmapWidth, bitmapHeight, maxWidth, maxHeight);
//
//        //没有透明度的话，设置成rgb_565
//        if (!hasAlpha) {
//            options.inPreferredConfig = Bitmap.Config.RGB_565;
//        }
//        //true,表示易变。可复用bitmap内存
//        options.inMutable = true;
//        options.inBitmap = reusable;
//
//        options.inJustDecodeBounds = false;
//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id, options);
//        return bitmap;

    }


    /**
     * 通过流加载图片
     *
     * @param is
     * @param maxWidth
     * @param maxHeight
     * @param hasAlpha
     * @param reusable
     * @return
     */
    public static Bitmap resizeBitmapFromStream(InputStream is, int maxWidth, int maxHeight, boolean hasAlpha, Bitmap reusable) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //
        options.inJustDecodeBounds = true;
        Bitmap bitmap2 = BitmapFactory.decodeStream(is, null, options);
        Log.e("Bitmap",
                "resizeBitmapFromStream" + (bitmap2 == null ? " null" : bitmap2.toString()));
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;
        options.inSampleSize = calculateInSameSize(bitmapWidth, bitmapHeight, maxWidth, maxHeight);

        //没有透明度的话，设置成rgb_565
        if (!hasAlpha) {
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        }
        //true,表示易变。可复用bitmap内存
        options.inMutable = true;
        options.inBitmap = reusable;

        options.inJustDecodeBounds = false;
        Bitmap bitmap = null;
        try {
            //流操作以后会移位，再次操作会返回null,需要重置
            is.reset();
            bitmap = BitmapFactory.decodeStream(is, null, options);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**
     * 计算缩放因子
     *
     * @param bitmapWidth
     * @param bitmapHeight
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    private static int calculateInSameSize(int bitmapWidth, int bitmapHeight, int maxWidth, int maxHeight) {
        int inSameSize = 1;
        if (bitmapWidth > maxWidth && bitmapHeight > maxHeight) {
            inSameSize = 2;
            while (bitmapWidth / inSameSize > maxWidth && bitmapHeight / inSameSize > maxHeight) {
                inSameSize *= 2;
            }
        }
        return inSameSize;
    }
}
