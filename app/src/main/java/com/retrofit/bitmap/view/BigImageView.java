package com.retrofit.bitmap.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.retrofit.bitmap.BitmapUtils;

import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.Nullable;

/**
 * Created by lis on 2020/7/7.
 */
public class BigImageView extends View implements View.OnTouchListener, GestureDetector.OnGestureListener {
    private static String TAG = BigImageView.class.getSimpleName();
    private int mode = 0;
    public static final int MODE_VERTICAL = 0;
    public static final int MODE_HORIZONTAL = 1;
    public static final int MODE_ALL = 2;

    private Rect rect;
    private BitmapFactory.Options options;
    Scroller scroller;
    GestureDetector gestureDetector;
    private boolean isAutoMode = true;

    public BigImageView(Context context) {
        this(context, null);
    }

    public BigImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnTouchListener(this);
        rect = new Rect();
        options = new BitmapFactory.Options();
        scroller = new Scroller(context);
        gestureDetector = new GestureDetector(context, this);
    }

    public void setMode(int mode) {
        isAutoMode = false;
        this.mode = mode;
    }

    private int imageWidth = 0;
    private int imageHeight = 0;
    BitmapRegionDecoder bitmapRegionDecoder;

    /**
     * 设置图片流
     *
     * @param image
     */
    public void setImage(InputStream image) {
        resetConfig();
        //获取图片宽高
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(image, null, options);
        imageWidth = options.outWidth;
        imageHeight = options.outHeight;
        Log.e(TAG, "image宽：" + imageWidth + " 高：" + imageHeight);
        //可修改
        options.inMutable = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        //
        options.inJustDecodeBounds = false;
        try {
            bitmapRegionDecoder = BitmapRegionDecoder.newInstance(image, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestLayout();
    }

    private void resetConfig() {
        rect = new Rect();
        options = new BitmapFactory.Options();
        bitmap = null;
        scale = 1;
    }

    int viewWidth = 0;
    int viewHeight = 0;
    float scale = 1;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取控件宽高
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();
        Log.e(TAG, "view,宽：" + viewWidth + " 高：" + viewHeight);
        if (bitmapRegionDecoder == null) {
            return;
        }
        if (isAutoMode) {
            mode = getAutoMode();
        }
        rect.left = 0;
        rect.top = 0;
        switch (mode) {
            case MODE_VERTICAL:
                rect.right = imageWidth;
                //缩放因子
                scale = viewWidth / (float) imageWidth;
                Log.e(TAG, "MODE_VERTICAL-scale:" + scale);
                // viewHeight/imageHeight=scale;
                // bottom=viewHeight/scale;
                rect.bottom = (int) (viewHeight / scale);
                break;
            case MODE_HORIZONTAL:
                rect.bottom = imageHeight;
                scale = viewHeight / (float) imageHeight;
                rect.right = (int) (viewWidth / scale);
                break;
            case MODE_ALL:
                rect.right = viewWidth;
                rect.bottom = viewHeight;
                break;
        }
        //第一种
        options.inSampleSize = BitmapUtils.calculateInSameSize(imageWidth, imageHeight, viewWidth, viewHeight);
        //第二种
//        options.inSampleSize = BitmapUtils.calculateInSameSize(scale);
    }

    private int getAutoMode() {
        if (imageHeight > viewHeight && imageWidth > viewWidth) {
            return MODE_ALL;
        } else if (imageWidth > imageHeight) {
            return MODE_HORIZONTAL;
        } else {
            return MODE_VERTICAL;
        }
    }

    Bitmap bitmap = null;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmapRegionDecoder == null) {
            return;
        }
        options.inBitmap = bitmap;
        bitmap = bitmapRegionDecoder.decodeRegion(rect, options);

        Matrix matrix = new Matrix();
//        matrix.setScale(scale, scale);
        matrix.setScale(scale * options.inSampleSize, scale * options.inSampleSize);
        canvas.drawBitmap(bitmap, matrix, null);
        Log.e(TAG, "bitmap,width: " + bitmap.getWidth() * scale + " height: " +
                bitmap.getHeight() * scale);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // 如果滑动还没有停止 强制停止
        if (!scroller.isFinished()) {
            scroller.forceFinished(true);
        }
        //继续接收后续事件
        return true;
    }

    @Override
    public void computeScroll() {
        //已经计算结束 return
        if (scroller.isFinished()) {
            return;
        }
        //true 表示当前动画未结束
        if (scroller.computeScrollOffset()) {
            switch (mode) {
                case MODE_VERTICAL:
                    rect.top = scroller.getCurrY();
                    rect.bottom = rect.top + (int) (viewHeight / scale);
                    break;
                case MODE_HORIZONTAL:
                    rect.left = scroller.getCurrX();
                    rect.right = (int) (rect.left + (viewWidth / scale));
                    break;
                case MODE_ALL:
                    rect.top = scroller.getCurrY();
                    rect.bottom = rect.top + (int) (viewHeight);
                    rect.left = scroller.getCurrX();
                    rect.right = rect.left + viewWidth;
                    break;
            }
            invalidate();
        }
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        switch (mode) {
            case MODE_VERTICAL:
                //改变加载图片的区域
                rect.offset(0, (int) (distanceY));

                Log.e(TAG, "bottom-top:" + (rect.bottom - rect.top));
                //bottom大于图片高了， 或者 top小于0了
                Log.e(TAG, "rect.bottom：" + rect.bottom + " imageHeight:" + imageHeight);
                if (rect.bottom > imageHeight) {
                    rect.bottom = imageHeight;
                    rect.top = imageHeight - (int) (viewHeight / scale);
                }
                Log.e(TAG, "rect.top：" + rect.top);
                if (rect.top < 0) {
                    rect.top = 0;
                    rect.bottom = (int) (viewHeight / scale);
                }
                break;
            case MODE_HORIZONTAL:
                //改变加载图片的区域
                rect.offset((int) distanceX, 0);
                //最右
                if (rect.right > imageWidth) {
                    rect.right = imageWidth;
                    rect.left = (int) (imageWidth - (viewWidth / scale));
                }
                //最左
                if (rect.left < 0) {
                    rect.left = 0;
                    rect.right = (int) (viewWidth / scale);
                }
                break;
            case MODE_ALL:
                //改变加载图片的区域
                rect.offset((int) distanceX, (int) distanceY);
                //bottom大于图片高了， 或者 top小于0了
                if (rect.bottom > imageHeight) {
                    rect.bottom = imageHeight;
                    rect.top = imageHeight - (viewHeight);
                }
                if (rect.top < 0) {
                    rect.top = 0;
                    rect.bottom = (viewHeight);
                }
                //最右
                if (rect.right > imageWidth) {
                    rect.right = imageWidth;
                    rect.left = imageWidth - viewWidth;
                }
                //最左
                if (rect.left < 0) {
                    rect.left = 0;
                    rect.right = viewWidth;
                }
                break;
        }
        // 重绘
        invalidate();
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        /**
         * startX: 滑动开始的x坐标
         * velocityX: 以每秒像素为单位测量的初始速度
         * minX: x方向滚动的最小值
         * maxX: x方向滚动的最大值
         */
        switch (mode) {
            case MODE_VERTICAL:
                scroller.fling(0, rect.top, 0, (int) -velocityY, 0, 0,
                        0, (int) (imageHeight - (viewHeight / scale)));
                break;
            case MODE_HORIZONTAL:
                scroller.fling(rect.left, 0, (int) -velocityX, 0, 0,
                        (int) (imageWidth - (viewWidth / scale)), 0, 0);
                break;
            case MODE_ALL:
                scroller.fling(rect.left, rect.top, (int) -velocityX, (int) -velocityY, 0,
                        imageWidth - viewWidth,
                        0, imageHeight - (viewHeight));
                break;
        }
        return false;
    }

    /**
     * onTouch
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 事件交给手势处理
        return gestureDetector.onTouchEvent(event);
    }

}
