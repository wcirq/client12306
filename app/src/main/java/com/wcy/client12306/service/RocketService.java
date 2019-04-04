package com.wcy.client12306.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptGroup;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wcy.client12306.R;
import com.wcy.client12306.activity.SmokeBackActivity;
import com.wcy.client12306.inter.OnLocationListener;
import com.wcy.client12306.util.ImageUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class RocketService extends Service {
    OnLocationListener onLocationListener;

    // 手机窗体布局的管理者
    private WindowManager mWindowManager;
    // 手机窗体的布局
    private WindowManager.LayoutParams mParams;
    private WindowManager.LayoutParams mLogParams;
    // 展示小火箭的自定义布局
    private View mToastRocketView;
    private LinearLayout mToastLogView;
    // 展示小火箭的ImageView
    private ImageView mRocketImage;
    // 手机窗体的宽度
    private int mWindowWidth;
    // 手机窗体的高度
    private int mWindowHeight;
    private Camera mCamera;

    public void setOnLocationListener(OnLocationListener onLocationListener){
        this.onLocationListener = onLocationListener;
    }

    // 消息传递机制
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mParams.y = (Integer) msg.obj;
            onLocationListener.onLocation(mParams);
            mWindowManager.updateViewLayout(mToastRocketView, mParams);
        }
    };

    public class RocketBinder extends Binder{
        public RocketService getService(){
            return RocketService.this;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        // 获取手机屏幕的宽高值
        mWindowWidth = mWindowManager.getDefaultDisplay().getWidth();
        mWindowHeight = mWindowManager.getDefaultDisplay().getHeight();
        mParams = new WindowManager.LayoutParams();
        mLogParams = new WindowManager.LayoutParams();
        // 服务启动，打开自定义Toast的控件
        showRocketView();
        // 拖拽小火箭到任意位置
        dragRocket();
        dragLog();
        initCameras();
    }

    private void initCameras() {
        int numberOfCameras = Camera.getNumberOfCameras();
        if(numberOfCameras<1) {
            Toast.makeText(this, "没有相机", Toast.LENGTH_SHORT).show();
        }else {
            int n = mToastLogView.getChildCount();
            if (n>1) {
                // 打开相机 0后置 1前置
                mCamera = Camera.open(1);
                TextureView textureView = (TextureView) mToastLogView.getChildAt(1);
                textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){

                    @Override
                    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                        if (mCamera != null) {
                            // 设置相机预览宽高，此处设置为TextureView宽高
                            Camera.Parameters params = mCamera.getParameters();

                            // 选择合适的预览尺寸
                            List<Camera.Size> sizeList = params.getSupportedPreviewSizes();

                            // 如果sizeList只有一个我们也没有必要做什么了，因为就他一个别无选择
                            if (sizeList.size() > 1) {
                                Iterator<Camera.Size> itor = sizeList.iterator();
                                while (itor.hasNext()) {
                                    Camera.Size cur = itor.next();
                                    if (cur.width >= width
                                            && cur.height >= height) {
                                        width = cur.width;
                                        height = cur.height;
                                        break;
                                    }
                                }
                            }

                            params.setPreviewSize(width, height);
                            // 设置自动对焦模式
                            List<String> focusModes = params.getSupportedFocusModes();
                            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                                mCamera.setParameters(params);
                            }
                            try {
                                mCamera.setDisplayOrientation(90);// 设置预览角度，并不改变获取到的原始数据方向
                                // 绑定相机和预览的View
                                mCamera.setPreviewTexture(surface);
                                // 开始预览
                                mCamera.startPreview();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                    }

                    @Override
                    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                        mCamera.stopPreview();
                        mCamera.release();
                        return false;
                    }

                    @Override
                    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                    }
                });
                if (mCamera!=null){
                    mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                        Bitmap bitmap=null;
                        @Override
                        public void onPreviewFrame(final byte[] data, Camera camera) {
                            final Camera.Size size = camera.getParameters().getPreviewSize();
                            if (ImageUtil.isRun){
                                Runnable runable = new Runnable() {
                                    @Override
                                    public void run() {
                                        bitmap = ImageUtil.convertYUV420SPToARGB4444(data, size.width, size.height);
//                                        bitmap = ImageUtil.rotateBitmap(bitmap, 45f);
                                    }
                                };
                                runable.run();
                            }
                            if (bitmap!=null){
                                BitmapDrawable drawable= new BitmapDrawable(getResources(), bitmap);
                                ImageView imageView = new ImageView(getApplicationContext());

                                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                params.width = WindowManager.LayoutParams.WRAP_CONTENT;;
                                imageView.setLayoutParams(params);
                                imageView.setBackground(drawable);
                                mToastLogView.addView(imageView);
                            }
                        }
                    });
                }
            }
        }
    }

    private void dragLog() {
        mToastLogView.setOnTouchListener(new View.OnTouchListener() {
            private int startX;
            private int startY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int moveX = (int) event.getRawX();
                        int moveY = (int) event.getRawY();
                        // 两个方向上所移动的距离值
                        int disX = moveX - startX;
                        int disY = moveY - startY;

                        mLogParams.x = mLogParams.x + disX;
                        mLogParams.y = mLogParams.y + disY;

                        if (mLogParams.x < 0) {
                            mLogParams.x = 0;
                        }

                        if (mLogParams.y < 0) {
                            mLogParams.y = 0;
                        }

                        if (mLogParams.x > mWindowManager.getDefaultDisplay().getWidth() - v.getWidth()) {
                            mLogParams.x = mWindowManager.getDefaultDisplay().getWidth() - v.getWidth();
                        }

                        if (mLogParams.y > mWindowManager.getDefaultDisplay().getHeight() - 21 - v.getHeight()) {
                            mLogParams.y = mWindowManager.getDefaultDisplay().getHeight() - 21 - v.getHeight();
                        }
                        printLog(mLogParams);
                        onLocationListener.onLocation(mLogParams);
                        // 更新小火箭的坐标位置X和Y值
                        mWindowManager.updateViewLayout(mToastLogView, mLogParams);

                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();

                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });
    }

    private void printLog(WindowManager.LayoutParams mParams){
        int n = mToastLogView.getChildCount();
        if (n>0) {
            TextView textView = (TextView) mToastLogView.getChildAt(0);
            textView.setText(String.format("坐标: %d, %d", mParams.x, mParams.y));
        }
    }

    /**
     * 拖拽小火箭到任意位置
     */
    private void dragRocket() {
        mToastRocketView.setOnTouchListener(new View.OnTouchListener() {
            private int startX;
            private int startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int moveX = (int) event.getRawX();
                        int moveY = (int) event.getRawY();
                        // 两个方向上所移动的距离值
                        int disX = moveX - startX;
                        int disY = moveY - startY;

                        mParams.x = mParams.x + disX;
                        mParams.y = mParams.y + disY;

                        if (mParams.x < 0) {
                            mParams.x = 0;
                        }

                        if (mParams.y < 0) {
                            mParams.y = 0;
                        }

                        if (mParams.x > mWindowManager.getDefaultDisplay().getWidth() - v.getWidth()) {
                            mParams.x = mWindowManager.getDefaultDisplay().getWidth() - v.getWidth();
                        }

                        if (mParams.y > mWindowManager.getDefaultDisplay().getHeight() - 21 - v.getHeight()) {
                            mParams.y = mWindowManager.getDefaultDisplay().getHeight() - 21 - v.getHeight();
                        }
                        onLocationListener.onLocation(mParams);
                        // 更新小火箭的坐标位置X和Y值
                        mWindowManager.updateViewLayout(mToastRocketView, mParams);

                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();

                        break;
                    case MotionEvent.ACTION_UP:
                        // 小火箭拖拽到手机屏幕下方的中间时，触发小火箭发射
                        if (mParams.x > mWindowWidth / 2 - 150 && mParams.x < mWindowWidth / 2 - mToastRocketView.getWidth() / 2 + 50
                                && mParams.y > mWindowHeight - mToastRocketView.getHeight() - 25) {
                            // 小火箭发射升空
                            launchRocket();
                            Intent intent = new Intent(RocketService.this, SmokeBackActivity.class);
                            // 服务中开启activity，需要设置任务栈
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            startActivity(intent);
                        }
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 小火箭发射升空
     */
    private void launchRocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int disY = mWindowHeight / 5;
                for (int i = 0; i < 6; i++) {
                    int height = mWindowHeight - i * disY;
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 子线程不能改变主线程中的UI的变化，因此，由消息机制告知主线程进行改变，并携带相应的值
                    Message msg = Message.obtain();
                    msg.obj = height;
                    mHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    /**
     * 显示小火箭的自定义View
     */
    private void showRocketView() {
        // 自定义Toast
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        // 修改完左上角对齐
        mParams.gravity = Gravity.LEFT + Gravity.TOP;
//        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        //设置可以显示在状态栏上
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        // 加载ToastRocketView显示效果的布局文件
        mToastRocketView = View.inflate(this, R.layout.toast_rocket_view, null);
        // 窗体布局中加入自定义的展示小火箭的View
        mWindowManager.addView(mToastRocketView, mParams);
        mRocketImage = (ImageView) mToastRocketView.findViewById(R.id.rocket_image);
        // 获取动画，并开启动画
        AnimationDrawable animDraw = (AnimationDrawable) mRocketImage.getBackground();
        animDraw.start();

        mLogParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mLogParams.gravity = Gravity.LEFT + Gravity.TOP;
        mLogParams.format = PixelFormat.RGBA_8888;
        //设置可以显示在状态栏上
        mLogParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        //设置悬浮窗口长宽数据
        mLogParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLogParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mToastLogView = (LinearLayout) View.inflate(this, R.layout.toast_log_view, null);
        mWindowManager.addView(mToastLogView, mLogParams);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new RocketBinder();
    }

    @Override
    public void onDestroy() {
        if (mWindowManager != null && mToastRocketView != null) {
            mWindowManager.removeView(mToastRocketView);
        }
        super.onDestroy();
    }
}
