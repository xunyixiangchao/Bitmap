package com.retrofit.bitmap;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;

import com.retrofit.bitmap.view.BigImageView;

import java.io.IOException;
import java.io.InputStream;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //屏幕
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //应用区域
        Rect outRect1 = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);

        //View绘制区域
        Rect outRect2 = new Rect();
        getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(outRect2);
        int viewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();//要用这种方法
        imageView.setTitleHeight(viewTop);
    }

    BigImageView imageView;

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
        InputStream open = null;
        imageView = findViewById(R.id.image);
        try {
            open = getResources().getAssets().open("big4.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setMode(BigImageView.MODE_VERTICAL);
        imageView.setImage(open);
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
