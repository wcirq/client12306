package com.wcy.client12306.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;

public class GraphicsCodeView extends View {
    private Paint mPaint;
    private Paint mBitmapPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;

    private Path mPath;
    private float mLastX, mLastY;//上次的坐标
    private LinkedList<PathBean> undoList;

    public GraphicsCodeView(Context context) {
        super(context);
//        init();
    }

    private void init() {
        setBackgroundColor(Color.WHITE);//设置白色背景
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setStrokeWidth(4f);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);//使画笔更加圆润
        mPaint.setStrokeCap(Paint.Cap.ROUND);//同上
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        //保存签名的画布
        post(new Runnable() {//拿到控件的宽和高
            @Override
            public void run() {
                //获取PaintView的宽和高
                //由于橡皮擦使用的是 Color.TRANSPARENT ,不能使用RGB-565
                mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);
                mCanvas = new Canvas(mBitmap);
                //抗锯齿
                mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                //背景色
                mCanvas.drawColor(Color.WHITE);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //路径
                mPath = new Path();
                mLastX = x;
                mLastY = y;
                mPath.moveTo(mLastX, mLastY);
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - mLastX);
                float dy = Math.abs(y - mLastY);
                if (dx >= 3 || dy >= 3) {//绘制的最小距离 3px
                    //利用二阶贝塞尔曲线，使绘制路径更加圆滑
                    mPath.quadTo(mLastX, mLastY, (mLastX + x) / 2, (mLastY + y) / 2);
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                mCanvas.drawPath(mPath, mPaint);//将路径绘制在mBitmap上
                Path path = new Path(mPath);//复制出一份mPath
                Paint paint = new Paint(mPaint);
                PathBean pb = new PathBean(path, paint);
                undoList.add(pb);//将路径对象存入集合
                mPath.reset();
                mPath = null;
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);//将mBitmap绘制在canvas上,最终的显示
        }
    }

    /**
     * 测量
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (wSpecMode == MeasureSpec.EXACTLY && hSpecMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        } else if (wSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(200, hSpecSize);
        } else if (hSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(wSpecSize, 200);
        }
    }
    class PathBean {
        Path path;
        Paint paint;

        PathBean(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }
    }

}
