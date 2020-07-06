package com.retrofit.bitmap;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ImageView imageView = findViewById(R.id.image);
//        InputStream open = getResources().getAssets().open("big2.jpg");
//            Log.e("Bitmap",open.toString());
//        imageView.setImageBitmap(BitmapResize.resizeBitmapFromResource(this, R.mipmap.big2, 800, 600, true, null));
        ActivityManager service = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        int memory = service.getLargeMemoryClass() > 0 ? service.getLargeMemoryClass()
//                                                       : service.getMemoryClass();

        int memory = service.getMemoryClass();
        String dir = getExternalCacheDir() + "bitmap";
        //init方法最好写在application中
        BitmapCache.getInstance().init(memory / 8, dir);
        InputStream open = null;
        ImageView imageView = findViewById(R.id.image);
        try {
            open = getResources().getAssets().open("big2.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
//        imageView.setImageBitmap(BitmapResize.resizeBitmapFromStream(open, 800, 600, true, null));
        String key = "big2";
        //第一次
        //内存缓存
        Bitmap bitmap = BitmapCache.getInstance().getBitmapFromLru(key);
        Log.e("main", "使用内存缓存" + bitmap);
//        BitmapCache.getInstance().clearDisk();
        if (bitmap == null) {
            //复用缓存
            Bitmap reusable = BitmapCache.getInstance().getReusableBitmap(1000, 1000, 1);
            Log.e("main", "使用复用缓存" + reusable);
            //磁盘缓存
            bitmap = BitmapCache.getInstance().getBitmapFromDisk(key, reusable);
            Log.e("main", "使用磁盘缓存" + bitmap);
            if (bitmap == null) {
                // 网络获取
                bitmap = BitmapResize.resizeBitmapFromStream(open, 1000, 1000, true, reusable);
                //放入内存
                BitmapCache.getInstance().putBitmap2Lru(key, bitmap);
                //放入磁盘
                BitmapCache.getInstance().putBitmap2Disk(key, bitmap);
            }
        }
        imageView.setImageBitmap(bitmap);
        //第二次
        //内存缓存
        bitmap = BitmapCache.getInstance().getBitmapFromLru(key);
        Log.e("main", "使用内存缓存2" + bitmap);
        //复用缓存
        Bitmap reusable = BitmapCache.getInstance().getReusableBitmap(160, 160, 1);
        Log.e("main", "使用复用缓存2" + reusable);
        //磁盘缓存
        bitmap = BitmapCache.getInstance().getBitmapFromDisk(key, reusable);
        Log.e("main", "使用磁盘缓存2" + bitmap);
        imageView.setImageBitmap(bitmap);
        BitmapCache.getInstance().clearDisk();
        bitmap = BitmapCache.getInstance().getBitmapFromDisk(key, reusable);
        Log.e("main", "使用磁盘缓存3" + bitmap);
    }
}
