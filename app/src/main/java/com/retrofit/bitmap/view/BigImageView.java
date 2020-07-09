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
    private int mode;
    public static final int MODE_VERTICAL = 0;
    public static final int MODE_HORIZONTAL = 1;
    public static final int MODE_ALL = 2;

    private Rect rect;
    private BitmapFactory.Options options;
    Scroller scroller;
    GestureDetector gestureDetector;
    int titleHeight;
    int statusBarHeight;

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
        //获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            Log.e(TAG, "statusBar:" + statusBarHeight);
        }
    }

    public void setMode(int mode) {
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
        rect.left = 0;
        rect.top = 0;
        switch (mode) {
            case MODE_VERTICAL:
                rect.right = viewWidth;
                //缩放因子
                scale = viewWidth / (float) imageWidth;
                if (scale < 1) {
                    rect.right = imageWidth;
                }
                Log.e(TAG, "MODE_VERTICAL-scale" + scale);
                // viewHeight/imageHeight=scale;
                // bottom=viewHeight/scale;
                rect.bottom = (int) ((viewHeight + statusBarHeight + titleHeight) / scale);
//                rect.bottom=imageHeight;
                Log.e(TAG, "rect.bottom:" + rect.bottom);
                break;
            case MODE_HORIZONTAL:
                rect.bottom = (viewHeight + statusBarHeight + titleHeight);
                scale = (viewHeight + statusBarHeight + titleHeight) / (float) imageHeight;
                if (scale < 1) {
                    rect.bottom = imageHeight;
                }
                Log.e(TAG, "MODE_HORIZONTAL-scale" + scale);
                rect.right = (int) (viewWidth / scale);
                break;
            case MODE_ALL:
                rect.right = viewWidth;
                rect.bottom = viewHeight;
                break;
        }
        //第一种
        options.inSampleSize = 1;
        options.inSampleSize = BitmapUtils.calculateInSameSize(imageWidth, imageHeight, viewWidth, viewHeight);
        //第二种
//        options.inSampleSize = BitmapUtils.calculateInSameSize(scale);
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
                    rect.bottom = rect.top + (int) (viewHeight);
                    break;
                case MODE_HORIZONTAL:
                    rect.left = scroller.getCurrX();
                    rect.right = rect.left + viewWidth;
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
                Log.e(TAG, "rect.bottom：" + rect.bottom + "imageHeight:" + imageHeight);
                //改变加载图片的区域
                rect.offset(0, (int) distanceY);

                //bottom大于图片高了， 或者 top小于0了
                Log.e(TAG, "rect.bottom：" + rect.bottom + "imageHeight:" + imageHeight);
                if (rect.bottom > imageHeight) {
                    rect.bottom = imageHeight;
                    rect.top = imageHeight - (int) (viewHeight + titleHeight + statusBarHeight);
                }
                Log.e(TAG, "rect.top：" + rect.top);
                if (rect.top < 0) {
                    rect.top = 0;
                    rect.bottom = (int) (viewHeight + titleHeight + statusBarHeight);
                }
                break;
            case MODE_HORIZONTAL:
                //改变加载图片的区域
                rect.offset((int) distanceX, 0);
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
            case MODE_ALL:
                //改变加载图片的区域
                rect.offset((int) distanceX, (int) distanceY);
                //bottom大于图片高了， 或者 top小于0了
                if (rect.bottom > imageHeight) {
                    rect.bottom = imageHeight;
                    rect.top = imageHeight - (int) (viewHeight);
                }
                if (rect.top < 0) {
                    rect.top = 0;
                    rect.bottom = (int) (viewHeight);
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
//        mScroller.fling(0, mRect.top, 0, (int) -velocityY, 0, 0,
//                0, mImageHeight - (int) (mViewHeight));
//        mScroller.fling(mRect.left, mRect.top, (int) -velocityX, (int) -velocityY, 0,
//                mImageWidth - mViewWidth,
//                0, mImageHeight - (int) (mViewHeight));
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

    public void setTitleHeight(int titleHeight) {
        this.titleHeight = titleHeight;
        Log.e(TAG, String.valueOf(titleHeight));
    }
}
