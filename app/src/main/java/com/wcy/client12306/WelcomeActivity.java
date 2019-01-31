package com.wcy.client12306;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wcy.client12306.http.Crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends Activity {
    String image_path;
    Bitmap bitmap;
    ImageView imageView;
    Intent intent;
    TextView skipButton;
    TextView logoText;
    Timer timer = new Timer();
    int waiting_time = 5;

    private static class MyHandler extends Handler{
        private final WeakReference<WelcomeActivity> mTarget;

        MyHandler(WelcomeActivity target) {
            mTarget = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            WelcomeActivity activity = mTarget.get();
            if (msg.what==1){
                activity.imageView.setImageBitmap(activity.bitmap);
            }else if (msg.what==2){
                try {
                    FileInputStream fis = new FileInputStream(activity.image_path);
                    Bitmap bmp  = BitmapFactory.decodeStream(fis);
                    activity.imageView.setImageBitmap(bmp);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    MyHandler handler = new MyHandler(this);

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    waiting_time--;
                    skipButton.setText(String.format(getResources().getString(R.string.skip), waiting_time));
                    if (waiting_time < 0) {
                        timer.cancel();
                        skipButton.setVisibility(View.GONE);//倒计时到0隐藏字体
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide(); // 继承的是AppCompatActivity时
         requestWindowFeature(Window.FEATURE_NO_TITLE); // 继承的是Activity时
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        intent = new Intent(this, LoginActivity.class);
        imageView = findViewById(R.id.imageView);
        Resources res = WelcomeActivity.this.getResources();
        BitmapDrawable bitmapDrawable = (BitmapDrawable) res.getDrawable(R.drawable.welcome);
        Bitmap bmp = bitmapDrawable.getBitmap();
        imageView.setImageBitmap(bmp);

        logoText = findViewById(R.id.logo);
        skipButton = findViewById(R.id.skipButton);
        skipButton.setText(String.format(getResources().getString(R.string.skip), waiting_time));
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipButton.setTextColor(Color.argb(50, 255,0,0));
                handler.removeMessages(0); // 移除所有Messages
                Log.d("哈哈","skipButton");
                nextActivity();
            }
        });

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                image_path = getFilesDir().getAbsolutePath()+File.separator+"welcome.jpg";
                boolean exists = new File(image_path).exists();
                if (exists){
                    handler.sendEmptyMessage(2);
                }
                Crawler crawler = new Crawler();
                String imgUrl = crawler.getUrl();
                if (imgUrl!=null){
                    try {
                        URL url = new URL(imgUrl);
                        InputStream inputStream = url.openStream();
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        saveImage(bitmap);
//                        handler.sendEmptyMessage(1);
                    } catch (IOException e) {
                        Log.e("IOException", "连不上网络");
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
        timer.schedule(task, 1000, 1000);
        handler.postDelayed(new Runnable(){

            @Override
            public void run() {
                Log.d("哈哈","postDelayed");
                nextActivity();
            }
        }, waiting_time*1000);
    }

    private void saveImage(Bitmap bmp){
        File file = new File(image_path);
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
            if (bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush();
                out.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void nextActivity(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WelcomeActivity.this,"欢迎来购票!",Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish(); // 销毁 Activit 禁止返回欢迎页
            }
        }, 500);

    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode== KeyEvent.KEYCODE_BACK){
            handler.removeMessages(0);
            finish();
            return true;//不执行父类点击事件
        }
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }
}
