package com.retrofit.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.LruCache;

import com.retrofit.bitmap.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private static int DEFAULT_DISK_MEMORY = 10 * 1024 * 1024;

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

    DiskLruCache diskLruCache;

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

    /**
     * 图片放入磁盘缓存
     *
     * @param key
     * @param bitmap
     */
    public void putBitmap2Disk(String key, Bitmap bitmap) {
        DiskLruCache.Snapshot snapshot = null;
        OutputStream outputStream = null;
        try {
            snapshot = diskLruCache.get(key);
            if (snapshot == null) {
                DiskLruCache.Editor edit = diskLruCache.edit(key);
                if (edit != null) {
                    outputStream = edit.newOutputStream(0);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                    edit.commit();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 从磁盘缓存取图片
     *
     * @param key
     * @return
     */
    public Bitmap getBitmapFromDisk(String key, Bitmap reusable) {
        DiskLruCache.Snapshot snapshot = null;
        Bitmap bitmap = null;
        try {
            snapshot = diskLruCache.get(key);
            if (snapshot == null) {
                return null;
            }
            InputStream inputStream = snapshot.getInputStream(0);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inBitmap = reusable;
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (bitmap != null) {
                lruCache.put(key, bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
        return bitmap;
    }

    /**
     * 关闭磁盘缓存
     */
    public void closeDisk(){
        try {
            diskLruCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init(int memorySize, String dir) {
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
                    // 3.0 bitmap 缓存 native
                    // <8.0  bitmap 缓存 java
                    // 8.0 native
                    reusablePool.add(new WeakReference<Bitmap>(oldValue, getReferenceQueue()));
                } else {
                    oldValue.recycle();
                }
            }
        };
        //复用池初始化
        reusablePool = Collections.synchronizedSet(new HashSet<WeakReference<Bitmap>>());
        //磁盘缓存
        try {
            diskLruCache = DiskLruCache.open(new File(dir), BuildConfig.VERSION_CODE, 1,
                    DEFAULT_DISK_MEMORY);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!shutDown) {
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
                }
            }).start();
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
            return bitmap.getWidth() == width && bitmap.getHeight() == height &&
                   inSampleSize == 1;
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
