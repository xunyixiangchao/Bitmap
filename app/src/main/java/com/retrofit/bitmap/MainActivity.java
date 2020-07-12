package com.retrofit.bitmap;

import android.os.Bundle;
import android.view.View;

import com.retrofit.bitmap.view.BigImageView;

import java.io.IOException;
import java.io.InputStream;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    BigImageView imageView;
    InputStream open = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ImageView imageView = findViewById(R.id.image);
//        InputStream open = getResources().getAssets().open("big2.jpg");
//            Log.e("Bitmap",open.toString());
//        imageView.setImageBitmap(BitmapResize.resizeBitmapFromResource(this, R.mipmap.big2, 800, 600, true, null));
//        ActivityManager service = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        int memory = service.getLargeMemoryClass() > 0 ? service.getLargeMemoryClass()
//                                                       : service.getMemoryClass();

//        int memory = service.getMemoryClass();
//        String dir = getExternalCacheDir() + "bitmap";
//        //init方法最好写在application中
//        BitmapCache.getInstance().init(memory / 8, dir);

        imageView = findViewById(R.id.image);
        findViewById(R.id.width_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    open = getResources().getAssets().open("timg.jpg");
                    imageView.setImage(open);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.height_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    open = getResources().getAssets().open("big4.jpg");
                    imageView.setImage(open);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.big_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    open = getResources().getAssets().open("big2.jpg");
                    imageView.setImage(open);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

//        imageView.setMode(BigImageView.MODE_ALL);

//        imageView.setImageBitmap(BitmapResize.resizeBitmapFromStream(open, 800, 600, true, null));
//        String key = "big2";
//        //第一次
//        //内存缓存
//        Bitmap bitmap = BitmapCache.getInstance().getBitmapFromLru(key);
//        Log.e("main", "使用内存缓存" + bitmap);
////        BitmapCache.getInstance().clearDisk();
//        if (bitmap == null) {
//            //复用缓存
//            Bitmap reusable = BitmapCache.getInstance().getReusableBitmap(1000, 1000, 1);
//            Log.e("main", "使用复用缓存" + reusable);
//            //磁盘缓存
//            bitmap = BitmapCache.getInstance().getBitmapFromDisk(key, reusable);
//            Log.e("main", "使用磁盘缓存" + bitmap);
//            if (bitmap == null) {
//                // 网络获取
//                bitmap = BitmapResize.resizeBitmapFromStream(open, 1000, 1000, true, reusable);
//                //放入内存
//                BitmapCache.getInstance().putBitmap2Lru(key, bitmap);
//                //放入磁盘
//                BitmapCache.getInstance().putBitmap2Disk(key, bitmap);
//            }
//        }
//        imageView.setImageBitmap(bitmap);
//        //第二次
//        //内存缓存
//        bitmap = BitmapCache.getInstance().getBitmapFromLru(key);
//        Log.e("main", "使用内存缓存2" + bitmap);
//        //复用缓存
//        Bitmap reusable = BitmapCache.getInstance().getReusableBitmap(160, 160, 1);
//        Log.e("main", "使用复用缓存2" + reusable);
//        //磁盘缓存
//        bitmap = BitmapCache.getInstance().getBitmapFromDisk(key, reusable);
//        Log.e("main", "使用磁盘缓存2" + bitmap);
//        imageView.setImageBitmap(bitmap);
//        bitmap = BitmapCache.getInstance().getBitmapFromDisk(key, reusable);
//        Log.e("main", "使用磁盘缓存3" + bitmap);
    }
}
