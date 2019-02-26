package com.wcy.client12306.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wcy.client12306.R;
import com.wcy.client12306.http.Crawler;
import com.wcy.client12306.http.Session;
import com.wcy.client12306.util.MessageUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {
    String image_path;
    Bitmap bitmap;
    ImageView imageView;
    Intent intent;
    TextView skipButton;
    TextView logoText;
    Timer timer = new Timer();
    int waiting_time = 3;
    String userInfoPath;
    Session session=null;

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
                    if (waiting_time < 1) {
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
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide(); // 继承的是AppCompatActivity时
        }
//        requestWindowFeature(Window.FEATURE_NO_TITLE); // 继承的是Activity时
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
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
                nextActivity();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                image_path = getFilesDir().getAbsolutePath()+File.separator+"welcome.jpg";
                boolean exists = new File(image_path).exists();
                if (exists){
                    handler.sendEmptyMessage(2);
                }
                Crawler crawler = new Crawler();
                String imgUrl = crawler.getBaiduImageUrl();
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
        }).start();

        timer.schedule(task, 1000, 1000);
        handler.postDelayed(new Runnable(){

            @Override
            public void run() {
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

    public void load(){
        File file = new File(userInfoPath);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            session = (Session) ois.readObject();
            ois.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean check_user(){
        if (session!=null){
            final JSONObject[] result = new JSONObject[1];
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String url = "https://kyfw.12306.cn/passport/web/auth/uamtk";
                    HashMap<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("appid", "otn");
                    result[0] = (JSONObject) session.post(url,null,paramsMap);
                }
            });
            thread.start();
            try {
                thread.join(3000);
                if (result[0]!=null){
                    if (result[0].getInt("result_code")==0){
                        return true;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void gotoLogin(){
        intent = new Intent(this, LoginActivity.class);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WelcomeActivity.this,"请先登陆!",Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish(); // 销毁 Activit 禁止返回欢迎页
            }
        }, 500);
    }

    private void nextActivity(){
        userInfoPath = getFilesDir().getAbsolutePath()+File.separator+"userInfo.ser";
        boolean exists = new File(userInfoPath).exists();
        if (exists) {
            load();
            boolean isSuccessful = check_user();
            if (isSuccessful) {
                MessageUtil messageUtil = new MessageUtil();
                messageUtil.setMessStr("");
                intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("session", session);
                intent.putExtra("messageUtil", messageUtil);
                startActivity(intent);
                finish(); // 销毁 Activit 禁止返回欢迎页
            }else {
                gotoLogin();
            }
        }else {
            gotoLogin();
        }


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
