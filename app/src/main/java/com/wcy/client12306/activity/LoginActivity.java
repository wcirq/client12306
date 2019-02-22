package com.wcy.client12306.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wcy.client12306.R;
import com.wcy.client12306.http.HttpUtil;
import com.wcy.client12306.http.Session;
import com.wcy.client12306.ui.SuperEditTextView;
import com.wcy.client12306.util.ImageUtil;
import com.wcy.client12306.util.MessageUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class LoginActivity extends AppCompatActivity {
    private MyHandler handler = null;
    private ImageView imageView;
    private ArrayList<Bitmap> bitmaps;
    private Bitmap bitmap;
    private LinearLayout linearLayoutImageCode;
    private int choose[] = new int[8];
    private JSONObject jsonObject = null;
//    HttpUtil networkUtil = new HttpUtil();
    Session networkUtil = new Session();
    private File file = null;
    TextView message;

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
                int i = 0;
                for (Bitmap bmp : activity.bitmaps) {
                    switch (i) {
                        case 0:
                            ImageView imageView0 = activity.findViewById(R.id.question);
                            imageView0.setImageBitmap(bmp);
                            break;
                        case 1:
                            ImageView imageView1 = activity.findViewById(R.id.answer1);
                            imageView1.setImageBitmap(bmp);
                            imageView1.setPadding(0, 0, 0, 0);
                            imageView1.setAlpha(1f);
                            break;
                        case 2:
                            ImageView imageView2 = activity.findViewById(R.id.answer2);
                            imageView2.setImageBitmap(bmp);
                            imageView2.setPadding(0, 0, 0, 0);
                            imageView2.setAlpha(1f);
                            break;
                        case 3:
                            ImageView imageView3 = activity.findViewById(R.id.answer3);
                            imageView3.setImageBitmap(bmp);
                            imageView3.setPadding(0, 0, 0, 0);
                            imageView3.setAlpha(1f);
                            break;
                        case 4:
                            ImageView imageView4 = activity.findViewById(R.id.answer4);
                            imageView4.setImageBitmap(bmp);
                            imageView4.setPadding(0, 0, 0, 0);
                            imageView4.setAlpha(1f);
                            break;
                        case 5:
                            ImageView imageView5 = activity.findViewById(R.id.answer5);
                            imageView5.setImageBitmap(bmp);
                            imageView5.setPadding(0, 0, 0, 0);
                            imageView5.setAlpha(1f);
                            break;
                        case 6:
                            ImageView imageView6 = activity.findViewById(R.id.answer6);
                            imageView6.setImageBitmap(bmp);
                            imageView6.setPadding(0, 0, 0, 0);
                            imageView6.setAlpha(1f);
                            break;
                        case 7:
                            ImageView imageView7 = activity.findViewById(R.id.answer7);
                            imageView7.setImageBitmap(bmp);
                            imageView7.setPadding(0, 0, 0, 0);
                            imageView7.setAlpha(1f);
                            break;
                        case 8:
                            ImageView imageView8 = activity.findViewById(R.id.answer8);
                            imageView8.setImageBitmap(bmp);
                            imageView8.setPadding(0, 0, 0, 0);
                            imageView8.setAlpha(1f);
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
            }else if (msg.what==3){
                if (activity.file!=null){
                    activity.installApk(null);
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // 继承的是AppCompatActivity时
        }
        setContentView(R.layout.activity_login);
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

    }

    /**
     * 获取验证码
     */
    private void getCaptcha() {
        String url = "https://kyfw.12306.cn/passport/captcha/captcha-image?login_site=E&module=login&rand=sjrand&0.6523880813900003";
        try {
            bitmap = (Bitmap) networkUtil.get(url, null);
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

    protected String getMIMEType(File file) {
        String type = "";
        String fileName = file.getName();
        int a = fileName.lastIndexOf(".");
        String var3 = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var3);
        return type;
    }

    protected void installApk(@Nullable File file) {
        if (file==null){
            file = this.file;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            Uri uriForFile = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String type = getApplicationContext().getContentResolver().getType(uriForFile);
            // String type = getMIMEType(file);
            intent.setDataAndType(uriForFile, type);
        }else{
            intent.setDataAndType(Uri.fromFile(file), getMIMEType(file));
        }
        startActivity(intent);
    }

    protected void loadNewVersionProgress() {
//        final String uri = "http://www.apk.anzhi.com/data3/apk/201703/14/4636d7fce23c9460587d602b9dc20714_88002100.apk";
        final String uri = "http://imtt.dd.qq.com/16891/08D63F6E91D1713194CBC3929B0BB7CC.apk";
//        final String uri = "http://dlied5.myapp.com/myapp/1104466820/sgame/10006654_com.tencent.tmgp.sgame_u180_1.43.1.15_ca5461.apk";
        final String path = getFilesDir().getAbsolutePath()+File.separator+"updata.apk";
        final ProgressDialog pd;    //进度条对话框
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("正在下载更新");
        pd.show();
        //启动子线程下载任务
        new Thread() {
            @Override
            public void run() {
                try {
                    pd.setCancelable(false);
                    file = HttpUtil.getFileFromServer(uri, pd, path);
                    sleep(500);
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(), "请安装更新！", Toast.LENGTH_SHORT).show();
                    pd.dismiss(); //结束掉进度条对话框
                    handler.sendEmptyMessage(3);
//                    installApk(file);
                    Looper.loop();
                } catch (UnknownHostException e) {
                    //下载apk失败
                    e.printStackTrace();
                    Looper.prepare();
                    pd.dismiss(); //结束掉进度条对话框
                    Toast.makeText(getApplicationContext(), "无法连接网络！", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Looper.prepare();
                    pd.dismiss();
                    Toast.makeText(getApplicationContext(), "下载失败！", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                    Looper.prepare();
                    pd.dismiss();
                    Toast.makeText(getApplicationContext(), "下载失败！无法找到文件！", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                } catch (Exception e) {
                    e.printStackTrace();
                    Looper.prepare();
                    pd.dismiss();
                    Toast.makeText(getApplicationContext(), "下载失败！", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }.start();
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
                            jsonObject = (JSONObject) networkUtil.get(finalUrl, null);
                            if (jsonObject.getInt("result_code")==4){
                                String loginUrl = "https://kyfw.12306.cn/passport/web/login";
                                jsonObject = (JSONObject) networkUtil.post(loginUrl, null, paramsMap);
                                try {
                                    if (jsonObject.getInt("result_code")==0){
                                        MessageUtil messageUtil = new MessageUtil();
                                        messageUtil.setMessStr(jsonObject.getString("uamtk"));
                                        Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                                        intent.putExtra("httpUtil", networkUtil);
                                        intent.putExtra("messageUtil", messageUtil);
                                        startActivity(intent);
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
        } else if (view.getId() == R.id.install) {
            TextView message = new TextView(this);
            message.setText("发现新版本！请及时更新");
            message.setPadding(10, 10, 10, 10);
            message.setGravity(Gravity.CENTER);
            message.setTextColor(getResources().getColor(R.color.textColor));
            message.setTextSize(18);

            // 这里的属性可以一直设置，因为每次设置后返回的是一个builder对象
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // 设置提示框的标题
            builder.setTitle("版本升级").
//                    setCustomTitle(message).
                    // 设置提示框的图标
                    setIcon(R.mipmap.up).
                    // 通过自定义View设置要显示的信息
                    setView(message).
                    // 设置要显示的信息
//                    setMessage("发现新版本！请及时更新").
                    // 设置确定按钮
                    setPositiveButton("升级", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Toast.makeText(MainActivity.this, "选择确定哦", 0).show();
                            loadNewVersionProgress();//下载最新的版本程序
                        }
                    }).
                    // 设置取消按钮,null是什么都不做，并关闭对话框
                    setNegativeButton("取消", null).
                    setNeutralButton("忽略",null);

            // 生产对话框
            AlertDialog alertDialog = builder.create();
            // 显示对话框
            alertDialog.show();
            // 通过反射机制修改Message(必须 setMessage(), 不能 setView())
            try {
                Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
                mAlert.setAccessible(true);
                Object mAlertController = mAlert.get(alertDialog);
                Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
                mMessage.setAccessible(true);
                TextView mMessageView = (TextView) mMessage.get(mAlertController);
                mMessageView.setTextColor(Color.BLUE);
            }catch (Exception e){
                e.printStackTrace();
            }
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(16);
        } else if (view.getId() == R.id.refresh) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getCaptcha();
                }
            }).start();
        }

    }

    public void changeImageViewAlpha(int viewId, int id) {
        ImageView imageView = findViewById(viewId);
        float alpha = imageView.getAlpha();
        if (alpha == 0.5) {
            alpha = 1f;
            choose[id] = 0;
            imageView.setPadding(0, 0, 0, 0);
        } else {
            alpha = 0.5f;
            choose[id] = 1;
            imageView.setPadding(10, 10, 10, 10);
        }
        imageView.setAlpha(alpha);
//        message.setText(Arrays.toString(choose));
    }

    private void initMenu() {
        int viewIds[] = {R.id.answer1, R.id.answer2, R.id.answer3, R.id.answer4, R.id.answer5, R.id.answer6, R.id.answer7, R.id.answer8};
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
    }
}
