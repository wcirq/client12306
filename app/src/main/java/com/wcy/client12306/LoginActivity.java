package com.wcy.client12306;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wcy.client12306.http.HttpUtil;
import com.wcy.client12306.ui.SuperEditTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;


public class LoginActivity extends AppCompatActivity {
    private MyHandler handler = null;
    private ImageView imageView;
    private ArrayList<Bitmap> bitmaps;
    private Bitmap bitmap;
    private LinearLayout linearLayoutImageCode;
    private int choose[] = new int[8];
    private JSONObject jsonObject=null;
    HttpUtil networkUtil = new HttpUtil();

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
            if (msg.what==1){
                activity.linearLayoutImageCode.setVisibility(View.VISIBLE);
                int i = 0;
                for (Bitmap bmp:activity.bitmaps){
                    switch (i){
                        case 0:
                            ImageView imageView0 = activity.findViewById(R.id.question);
                            imageView0.setImageBitmap(bmp);
                            break;
                        case 1:
                            ImageView imageView1 = activity.findViewById(R.id.answer1);
                            imageView1.setImageBitmap(bmp);
                            break;
                        case 2:
                            ImageView imageView2 = activity.findViewById(R.id.answer2);
                            imageView2.setImageBitmap(bmp);
                            break;
                        case 3:
                            ImageView imageView3 = activity.findViewById(R.id.answer3);
                            imageView3.setImageBitmap(bmp);
                            break;
                        case 4:
                            ImageView imageView4 = activity.findViewById(R.id.answer4);
                            imageView4.setImageBitmap(bmp);
                            break;
                        case 5:
                            ImageView imageView5 = activity.findViewById(R.id.answer5);
                            imageView5.setImageBitmap(bmp);
                            break;
                        case 6:
                            ImageView imageView6 = activity.findViewById(R.id.answer6);
                            imageView6.setImageBitmap(bmp);
                            break;
                        case 7:
                            ImageView imageView7 = activity.findViewById(R.id.answer7);
                            imageView7.setImageBitmap(bmp);
                            break;
                        case 8:
                            ImageView imageView8 = activity.findViewById(R.id.answer8);
                            imageView8.setImageBitmap(bmp);
                            break;
                    }
                    i++;
                }
            }else if (msg.what==2){
                TextView textView = activity.findViewById(R.id.test);
                if(activity.jsonObject!=null){
//                    textView.setText(textView.getText()+activity.jsonObject.toString());
                    textView.setText(activity.jsonObject.toString());
                }else {
                    textView.setText("null");
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        linearLayoutImageCode = findViewById(R.id.imageCode);
        handler = new MyHandler(LoginActivity.this);
        initMenu();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://kyfw.12306.cn/passport/captcha/captcha-image?login_site=E&module=login&rand=sjrand&0.6523880813900003";
                bitmap = (Bitmap) networkUtil.get(url, null);
                bitmaps = new ArrayList<Bitmap>();
                for (int i=0;i<9;i++){
                    Bitmap bmp = null;
                    if (i==0){
                        bmp = Bitmap.createBitmap(bitmap, 0, 0, 293, 28);
                    }else {
                        int j = i-1;
                        int col = j%4;
                        int row = (int) j/4;
                        int x = col*72+3;
                        int y = row*72+39;
                        int w = 72;
                        int h = 72;
                        bmp = Bitmap.createBitmap(bitmap, x, y, w, h);
                        Log.d("","");
                    }
                    bitmaps.add(bmp);
                }
                handler.sendEmptyMessage(1);
            }
        }).start();

    }

    public void onClick(View view) {
        String url = "https://kyfw.12306.cn/passport/captcha/captcha-check?answer=%s&rand=sjrand&login_site=E";
        String []data = {"35,35", "105,35", "175,35", "245,35", "35,105", "105,105", "175,105", "245,105"};
        StringBuilder answer = new StringBuilder();
        for (int i=0;i<choose.length;i++){
            if (choose[i]==1){
                answer.append(",").append(data[i]);
            }
        }
        //去除第一个字符
        answer.deleteCharAt(0);
        String answerStr = answer.toString().replace(",","%2C");
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
                jsonObject = (JSONObject) networkUtil.get(finalUrl, null);
                String loginUrl = "https://kyfw.12306.cn/passport/web/login";
                jsonObject = (JSONObject) networkUtil.post(loginUrl, null, paramsMap);
                handler.sendEmptyMessage(2);
            }
        }).start();
    }

    public void changeImageViewAlpha(int viewId, int id){
        ImageView imageView = findViewById(viewId);
        float alpha = imageView.getAlpha();
        if (alpha==0.5){
            alpha=1f;
            choose[id]=0;
            imageView.setPadding(0,0,0,0);
        }else {
            alpha=0.5f;
            choose[id]=1;
            imageView.setPadding(10,10,10,10);
        }
        imageView.setAlpha(alpha);
        TextView textView = findViewById(R.id.test);
        textView.setText(Arrays.toString(choose));
    }

    private void initMenu() {
        int viewIds[] = {R.id.answer1,R.id.answer2,R.id.answer3,R.id.answer4,R.id.answer5,R.id.answer6,R.id.answer7,R.id.answer8};
        menuItemSelected(viewIds[0], new MenuSelectedListener() {
            @Override
            public void onMenuSelected(int viewId) {
                changeImageViewAlpha(viewId, 0);
            }
        });
        menuItemSelected(viewIds[1], new MenuSelectedListener() {
            @Override
            public void onMenuSelected(int viewId) {
                changeImageViewAlpha(viewId, 1);
            }
        });
        menuItemSelected(viewIds[2], new MenuSelectedListener() {
            @Override
            public void onMenuSelected(int viewId) {
                changeImageViewAlpha(viewId, 2);
            }
        });
        menuItemSelected(viewIds[3], new MenuSelectedListener() {
            @Override
            public void onMenuSelected(int viewId) {
                changeImageViewAlpha(viewId, 3);
            }
        });
        menuItemSelected(viewIds[4], new MenuSelectedListener() {
            @Override
            public void onMenuSelected(int viewId) {
                changeImageViewAlpha(viewId, 4);
            }
        });
        menuItemSelected(viewIds[5], new MenuSelectedListener() {
            @Override
            public void onMenuSelected(int viewId) {
                changeImageViewAlpha(viewId, 5);
            }
        });
        menuItemSelected(viewIds[6], new MenuSelectedListener() {
            @Override
            public void onMenuSelected(int viewId) {
                changeImageViewAlpha(viewId, 6);
            }
        });
        menuItemSelected(viewIds[7], new MenuSelectedListener() {
            @Override
            public void onMenuSelected(int viewId) {
                changeImageViewAlpha(viewId, 7);
            }
        });
    }

    /**
     * 选中底部 Menu 菜单项
     */
    private void menuItemSelected(final int viewId, final MenuSelectedListener listener) {
        findViewById(viewId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onMenuSelected(viewId);
            }
        });

    }

    interface MenuSelectedListener {
        void onMenuSelected(int viewId);
    }
}
