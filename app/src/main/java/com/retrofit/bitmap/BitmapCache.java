package com.retrofit.bitmap;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by lis on 2020/7/3.
 */
public class BitmapCache {

    private volatile static BitmapCache instance;

    public static BitmapCache getInstance() {
        if (instance == null) {
            synchronized (BitmapCache.class) {
                if (instance == null) {
                    instance = new BitmapCache();
                }
            }
        }
        return instance;
    }

    //内存缓存
    LruCache<String, Bitmap> lruCache;
    //复用池
    Set<WeakReference<Bitmap>> reusablePool;

    /**
     * 放入内存缓存
     *
     * @param key
     * @param bitmap
     */
    public void putBitmap2Lru(String key, Bitmap bitmap) {
        lruCache.put(key, bitmap);
    }

    /**
     * 从内存中获取
     *
     * @param key
     * @return
     */
    public Bitmap getBitmapFromLru(String key) {
        return lruCache.get(key);
    }

    /**
     * 清理内存
     */
    public void clearMemory() {
        lruCache.evictAll();
    }

    public void init(int memorySize) {
        //内存初始化
        lruCache = new LruCache<String, Bitmap>(memorySize * 1024 * 1024) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    return value.getAllocationByteCount();
                }
                return value.getByteCount();
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if (oldValue.isMutable()) {
                    reusablePool.add(new WeakReference<Bitmap>(oldValue, getReferenceQueue()));
                } else {
                    oldValue.recycle();
                }
            }
        };
        //复用池初始化
        reusablePool = Collections.synchronizedSet(new HashSet<WeakReference<Bitmap>>());


    }

    private ReferenceQueue<Bitmap> referenceQueue;

    boolean shutDown;

    public void setShutDown(boolean shutDown) {
        this.shutDown = shutDown;
    }

    public boolean isShutDown() {
        return shutDown;
    }

    /**
     * 阻塞队列
     *
     * @return
     */
    private ReferenceQueue<Bitmap> getReferenceQueue() {
        if (referenceQueue == null) {
            referenceQueue = new ReferenceQueue<>();
            while (!shutDown) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Reference<? extends Bitmap> remove = referenceQueue.remove();
                            Bitmap bitmap = remove.get();
                            if (bitmap != null && !bitmap.isRecycled()) {
                                bitmap.recycle();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
        return referenceQueue;
    }


    /**
     * 获取复用Bitmap
     * 3以下不支持复用
     * 3-4.4宽高一样，inSampleSize=1
     *
     * @return
     */
    public Bitmap getReusableBitmap(int width, int height, int inSampleSize) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return null;
        }
        Bitmap reusable = null;
        Iterator<WeakReference<Bitmap>> iterator = reusablePool.iterator();
        while (iterator.hasNext()) {
            WeakReference<Bitmap> next = iterator.next();
            Bitmap bitmap = next.get();
            if (bitmap != null) {
                if (checkInBitmap(bitmap, width, height, inSampleSize)) {
                    reusable = bitmap;
                    iterator.remove();
                    break;
                }
            } else {
                iterator.remove();
            }
        }
        return reusable;
    }

    private boolean checkInBitmap(Bitmap bitmap, int width, int height, int inSampleSize) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return bitmap.getWidth() == width && bitmap.getHeight() == height && inSampleSize == 1;
        }
        if (inSampleSize > 1) {
            width /= inSampleSize;
            height /= inSampleSize;
        }
        int byteCount = width * height * getBitmapPixel(bitmap);
        //bitmap.getAllocationByteCount()系统分配的内存  byteCount图片内存
        return bitmap.getAllocationByteCount() >= byteCount;
    }

    /**
     * 通过像素格式计算每一个像素占用多少字节
     * 默认RGB_565，2
     *
     * @param bitmap
     * @return
     */
    private int getBitmapPixel(Bitmap bitmap) {
        switch (bitmap.getConfig()) {
            case ALPHA_8:
                return 1;
            case RGB_565:
                return 2;
            case ARGB_8888:
                return 4;
            case RGBA_F16:
                return 8;
        }
        return 2;
    }
}
