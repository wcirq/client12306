package com.wcy.client12306;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.wcy.client12306.http.HttpUtil;
import com.wcy.client12306.ui.PaintView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class LoginActivity extends AppCompatActivity {
    private ImageView imageView;
    private Bitmap bitmap;

    private static class MyHandler extends Handler {
        private final WeakReference<LoginActivity> mTarget;

        MyHandler(LoginActivity target) {
            mTarget = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginActivity activity = mTarget.get();
            if (msg.what==1){
                activity.imageView.setImageBitmap(activity.bitmap);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final MyHandler handler = new MyHandler(LoginActivity.this);
        imageView = findViewById(R.id.code);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpUtil networkUtil = new HttpUtil();
                String url = "https://kyfw.12306.cn/passport/captcha/captcha-image?login_site=E&module=login&rand=sjrand&0.6523880813900003";
                bitmap = (Bitmap) networkUtil.doGet(url, null);
                ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
                for (int i=0;i<9;i++){
                    Bitmap bmp = null;
                    if (i==0){
                        bmp = Bitmap.createBitmap(bitmap, 0, 0, 293, 28);
                    }else {
                        int j = i-1;
                        int col = j%4;
                        int row = (int) j/4;
                        bmp = Bitmap.createBitmap(bitmap, row*71, col*71+28, row*71+71, col*71+28+71);
                    }
                    bitmaps.add(bmp);
                }
                handler.sendEmptyMessage(1);
            }
        }).start();

    }
//    private void initView() {
//        paintView = (PaintView) findViewById(R.id.activity_paint_pv);
//    }
//
//    private void initMenu() {
//        //撤销
//        menuItemSelected(R.id.activity_paint_undo, new MenuSelectedListener() {
//            @Override
//            public void onMenuSelected() {
////                paintView.undo();
//            }
//        });
//        //恢复
//        menuItemSelected(R.id.activity_paint_redo, new MenuSelectedListener() {
//            @Override
//            public void onMenuSelected() {
////                paintView.redo();
//            }
//        });
//
//        //颜色
//        menuItemSelected(R.id.activity_paint_color, new MenuSelectedListener() {
//            @Override
//            public void onMenuSelected() {
////                paintView.setPaintColor(Color.RED);
//            }
//        });
//        //清空
//        menuItemSelected(R.id.activity_paint_clear, new MenuSelectedListener() {
//            @Override
//            public void onMenuSelected() {
////                paintView.clearAll();
//            }
//        });
//
//        //橡皮擦
//        menuItemSelected(R.id.activity_paint_eraser, new MenuSelectedListener() {
//            @Override
//            public void onMenuSelected() {
////                paintView.setEraserModel(true);
//            }
//        });
//
//        //保存
//        menuItemSelected(R.id.activity_paint_save, new MenuSelectedListener() {
//            @Override
//            public void onMenuSelected() {
//                String path = Environment.getExternalStorageDirectory().getPath()
//                        + File.separator + "image";
//                String imgName = "paint.jpg";
////                if (paintView.saveImg(path, imgName)) {
////                    ToastUtils.show(PaintViewActivity.this, "保存成功");
////                }
//            }
//        });
//    }
//
//    /**
//     * 选中底部 Menu 菜单项
//     */
//    private void menuItemSelected(int viewId, final MenuSelectedListener listener) {
//        findViewById(viewId).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                listener.onMenuSelected();
//            }
//        });
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }
//
//    interface MenuSelectedListener {
//        void onMenuSelected();
//    }

}
