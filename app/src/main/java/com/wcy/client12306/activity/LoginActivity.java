package com.wcy.client12306.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wcy.client12306.R;
import com.wcy.client12306.db.DBHelper;
import com.wcy.client12306.http.Session;
import com.wcy.client12306.service.FloatVideoWindowService;
import com.wcy.client12306.ui.SuperEditTextView;
import com.wcy.client12306.util.MessageUtil;
import com.wcy.client12306.util.SystemUtil;
import com.wcy.client12306.util.ImageUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class LoginActivity extends AppCompatActivity{
    private boolean isFrist = false;
    DBHelper dbHelper;
    private MyHandler handler = null;
    private ImageView imageView;
    private ArrayList<Bitmap> bitmaps;
    private Bitmap bitmap;
    private LinearLayout linearLayoutImageCode;
    private int choose[] = new int[8];
    private JSONObject jsonObject = null;
    Session session = null;
    TextView message;
    String userInfoPath;
    ServiceConnection mVideoServiceConnection;
    Intent intentService;

    public LoginActivity() {
    }

    private static class MyHandler extends Handler {
        private final WeakReference<LoginActivity> mTarget;

        MyHandler(LoginActivity target) {
            mTarget = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginActivity activity = mTarget.get();
            if (msg.what == 1) {
                for (int i=0;i<activity.choose.length;i++){
                    activity.choose[i]=0;
                }
                activity.linearLayoutImageCode.setVisibility(View.VISIBLE);
                int imageViews[] = {R.id.question, R.id.answer1, R.id.answer2, R.id.answer3, R.id.answer4, R.id.answer5, R.id.answer6, R.id.answer7, R.id.answer8};
                int i = 0;
                for (Bitmap bmp : activity.bitmaps) {
                    switch (i) {
                        case 0:
                            ImageView imageView0 = activity.findViewById(R.id.question);
                            imageView0.setImageBitmap(bmp);
                            break;
                        default:
                            ImageView imageView = activity.findViewById(imageViews[i]);
                            imageView.setImageBitmap(bmp);
                            imageView.setPadding(0, 0, 0, 0);
                            imageView.setAlpha(1f);
                            break;
                    }
                    i++;
                }
            } else if (msg.what == 2) {
                if (activity.jsonObject != null) {
                    if (activity.jsonObject.has("result_message")){
                        try {
                            Log.d("result", activity.jsonObject.toString());
                            activity.message.setText(activity.jsonObject.getString("result_message"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            activity.message.setText("");
                        }
                    }
                } else {
                    activity.message.setText("");
                }
            }
        }
    }

    private void loadBackground(){
        LinearLayout linearLayout = findViewById(R.id.background);
        String image_path = getFilesDir().getAbsolutePath()+File.separator+"welcome.jpg";
        try {
            FileInputStream fis = new FileInputStream(image_path);
            Bitmap bmp  = BitmapFactory.decodeStream(fis);
            bmp = ImageUtil.getAlplaBitmap(bmp, 6);
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bmp);
            linearLayout.setBackground(bitmapDrawable);
        } catch (FileNotFoundException e) {
            Log.d("加载背景", "失败！没有图片！");
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SystemUtil.setStatusBarColor(this, R.color.colorDeafult);
        userInfoPath = getFilesDir().getAbsolutePath()+File.separator+"userInfo.ser";
        session = new Session();
        dbHelper = new DBHelper(getApplicationContext());
        SuperEditTextView userName = findViewById(R.id.userName);
        SuperEditTextView password = findViewById(R.id.password);
        HashMap<String, String> result = dbHelper.find(1);
        if (result!=null) {
            userName.setText(result.get("name"));
            password.setText(result.get("password"));
        }
        loadBackground();
        message = findViewById(R.id.message);
        linearLayoutImageCode = findViewById(R.id.imageCode);
        handler = new MyHandler(LoginActivity.this);
        initMenu();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getCaptcha();
            }
        }).start();

        mVideoServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // 获取服务的操作对象
                FloatVideoWindowService.MyBinder binder = (FloatVideoWindowService.MyBinder) service;
                FloatVideoWindowService floatVideoWindowService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("", "");
            }
        };
        if (isFrist){
            startVideoService();
        }
    }

    public void startVideoService() {
        moveTaskToBack(true);//最小化Activity
        intentService = new Intent(this, FloatVideoWindowService.class);//开启服务显示悬浮框
        bindService(intentService, mVideoServiceConnection, Context.BIND_AUTO_CREATE);
    }


    /**
     * 获取验证码
     */
    private void getCaptcha() {
        String url = "https://kyfw.12306.cn/passport/captcha/captcha-image?login_site=E&module=login&rand=sjrand&0.6523880813900003";
        try {
            bitmap = (Bitmap) session.get(url, null);
            if (bitmap != null) {
                bitmaps = new ArrayList<Bitmap>();
                for (int i = 0; i < 9; i++) {
                    Bitmap bmp = null;
                    if (i == 0) {
                        bmp = Bitmap.createBitmap(bitmap, 0, 0, 293, 28);
                    } else {
                        int j = i - 1;
                        int col = j % 4;
                        int row = (int) j / 4;
                        int x = col * 72 + 3;
                        int y = row * 72 + 39;
                        int w = 72;
                        int h = 72;
                        bmp = Bitmap.createBitmap(bitmap, x, y, w, h);
                        Log.d("", "");
                    }
                    bitmaps.add(bmp);
                }
                handler.sendEmptyMessage(1);
            } else {
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "无法连接网络！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }catch (ClassCastException e){
            try {
                Thread.sleep((long) 1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            getCaptcha();
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.login) {
            message.setText("");
            String url = "https://kyfw.12306.cn/passport/captcha/captcha-check?answer=%s&rand=sjrand&login_site=E";
            String[] data = {"35,35", "105,35", "175,35", "245,35", "35,105", "105,105", "175,105", "245,105"};
            StringBuilder answer = new StringBuilder();
            for (int i = 0; i < choose.length; i++) {
                if (choose[i] == 1) {
                    answer.append(",").append(data[i]);
                }
            }
            if (!answer.toString().equals("")) {
                //去除第一个字符
                answer.deleteCharAt(0);
                String answerStr = answer.toString().replace(",", "%2C");
                url = String.format(url, answerStr);
                final String finalUrl = url;

                final HashMap<String, String> paramsMap = new HashMap<>();
                SuperEditTextView userName = findViewById(R.id.userName);
                paramsMap.put("username", Objects.requireNonNull(userName.getText()).toString());
                SuperEditTextView password = findViewById(R.id.password);
                paramsMap.put("password", Objects.requireNonNull(password.getText()).toString());
                paramsMap.put("appid", "otn");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            jsonObject = (JSONObject) session.get(finalUrl, null);
                            if (jsonObject.getInt("result_code")==4){
                                String loginUrl = "https://kyfw.12306.cn/passport/web/login";
                                jsonObject = (JSONObject) session.post(loginUrl, null, paramsMap);
                                try {
                                    if (jsonObject.getInt("result_code")==0){
                                        dbHelper.delete(1);
                                        dbHelper.insert(paramsMap.get("username"), paramsMap.get("password"));
                                        Session.dump(session, userInfoPath);
                                        MessageUtil messageUtil = new MessageUtil();
                                        messageUtil.setMessStr(jsonObject.getString("uamtk"));
                                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                        intent.putExtra("session", session);
                                        intent.putExtra("messageUtil", messageUtil);
                                        startActivity(intent);
                                        finish();
                                    }else {
                                        if (jsonObject.getInt("result_code")==5){
                                            //验证码校验失败
                                            getCaptcha();
                                        }
                                        Log.d("jsonObject", jsonObject.toString());
                                        handler.sendEmptyMessage(2);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else {
                                getCaptcha();
                                handler.sendEmptyMessage(2);
                            }

                        }catch (ClassCastException e){
                            try {
                                Thread.sleep(1000);
                            }catch (InterruptedException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                            getCaptcha();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e){
                            e.printStackTrace();
                            Looper.prepare();
                            Toast.makeText(getApplicationContext(), "无法连接网络！", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                }).start();
            }else {
                Toast.makeText(getApplicationContext(), "请选择图片！", Toast.LENGTH_SHORT).show();
            }
        }else if (view.getId() == R.id.refresh) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getCaptcha();
                }
            }).start();
        }

    }

    public void changeImageViewAlpha(int[] viewId) {
        ImageView imageView = findViewById(viewId[0]);
        float alpha = imageView.getAlpha();
        if (alpha == 0.5) {
            alpha = 1f;
            choose[viewId[1]] = 0;
            imageView.setPadding(0, 0, 0, 0);
        } else {
            alpha = 0.5f;
            choose[viewId[1]] = 1;
            imageView.setPadding(10, 10, 10, 10);
        }
        imageView.setAlpha(alpha);
//        message.setText(Arrays.toString(choose));
    }

    private void initMenu() {
        int viewIds[][] = {{R.id.answer1, 0}, {R.id.answer2, 1}, {R.id.answer3, 2}, {R.id.answer4, 3}, {R.id.answer5, 4}, {R.id.answer6, 5}, {R.id.answer7, 6}, {R.id.answer8, 7}};
        for (int i=0;i<viewIds.length;i++){
            menuItemSelected(viewIds[i], new MenuSelectedListener() {
                @Override
                public void onMenuSelected(int[] viewId) {
                    changeImageViewAlpha(viewId);
                }
            });
        }
    }

    /**
     * 选中底部 Menu 菜单项
     */
    private void menuItemSelected(final int viewId[], final MenuSelectedListener listener) {
        findViewById(viewId[0]).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onMenuSelected(viewId);
            }
        });

    }

    interface MenuSelectedListener {
        void onMenuSelected(int [] viewId);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Session.dump(session, userInfoPath);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getCaptcha();
            }
        }).start();
        isFrist = false;
        unbindService(mVideoServiceConnection);//不显示悬浮框
    }
}
