package com.retrofit.bitmap;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
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
        BitmapCache.getInstance().init(memory / 8);
        InputStream open = null;
        try {
            ImageView imageView = findViewById(R.id.image);
            open = getResources().getAssets().open("guimie.jpg");
//            Log.e("Bitmap",open.toString());
            imageView.setImageBitmap(BitmapResize.resizeBitmapFromStream(open, 800, 600, true, null));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (open != null) {
                try {
                    open.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
