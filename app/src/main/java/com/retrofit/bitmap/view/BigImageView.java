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
    private int mode;
    public static int MODE_VERTICAL = 0;
    public static int MODE_HORIZONTAL = 1;
    public static int MODE_ALL = 2;

    private Rect rect;
    private BitmapFactory.Options options;
    Scroller scroller;
    GestureDetector gestureDetector;

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
    float scale = 0;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取控件宽高
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();
        if (bitmapRegionDecoder == null) {
            return;
        }
        rect.left = 0;
        rect.top = 0;

        rect.right = viewWidth;
        //缩放因子
        scale = viewWidth / (float)imageWidth;
        // viewHeight/imageHeight=scale;
        // bottom=viewHeight/scale;
        rect.bottom = (int) (viewHeight / scale);
        //第一种
        options.inSampleSize = BitmapUtils.calculateInSameSize(imageWidth, imageHeight, viewWidth, viewHeight);
        //第二种
//        options.inSampleSize = BitmapUtils.calculateInSameSize(scale);
        Log.e("Big", String.valueOf(options.inSampleSize));
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
        matrix.setScale(scale, scale);
//        matrix.setScale( options.inSampleSize,  options.inSampleSize);
        canvas.drawBitmap(bitmap, matrix, null);
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
            rect.top = scroller.getCurrY();
            rect.bottom = rect.top + (int) (viewHeight);
            //// TODO: 2020/7/7
//            mRect.left = mScroller.getCurrX();
//            mRect.right = mRect.left + mViewWidth;
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
        //改变加载图片的区域
        rect.offset(0, (int) distanceY);

        //bottom大于图片高了， 或者 top小于0了
        if (rect.bottom > imageHeight) {
            rect.bottom = imageHeight;
            rect.top = imageHeight - (int) (viewHeight);
        }
        if (rect.top < 0) {
            rect.top = 0;
            rect.bottom = (int) (viewHeight);
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
