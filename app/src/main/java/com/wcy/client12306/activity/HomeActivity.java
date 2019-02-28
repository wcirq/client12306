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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wcy.client12306.R;
import com.wcy.client12306.http.HttpUtil;
import com.wcy.client12306.http.Session;
import com.wcy.client12306.util.MessageUtil;
import com.wcy.client12306.util.SystemUtil;

import org.json.JSONArray;
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

import static com.wcy.client12306.util.SystemUtil.setStatusBarColor;

public class HomeActivity extends AppCompatActivity {
    Handler handler;
    String items[]=null;
    Session session;
    ListView listView;
    ArrayAdapter<String> arrayAdapter;
    JSONObject jsonObject=null;
    TextView userName, infoTextView;
    ImageView userImage;
    private File file = null;
    LinearLayout linearLayoutBar;

    private static class MyHandler extends Handler {
        private final WeakReference<HomeActivity> mTarget;

        MyHandler(HomeActivity target) {
            mTarget = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            HomeActivity activity = mTarget.get();
            if (msg.what == 1) {
                activity.arrayAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, activity.items);
                activity.listView.setAdapter(activity.arrayAdapter);
                try {
                    if (activity.jsonObject!=null&&activity.userName!=null){
                        activity.userName.setText(activity.jsonObject.getJSONObject("data").getJSONObject("userDTO").getJSONObject("loginUserDTO").getString("name"));
                        activity.infoTextView.setText(activity.jsonObject.getJSONObject("data").getJSONObject("userDTO").getString("email"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (msg.what == 2) {

            }else if (msg.what==3){

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setStatusBarColor(this, R.color.colorfocus);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_details, null);
        LinearLayout drawerLayout = findViewById(R.id.linear_layout_content);
        drawerLayout.addView(view,0);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        View headView=navigationView.inflateHeaderView(R.layout.nav_header_setting);
        userImage = headView.findViewById(R.id.userImageView);
        userName = headView.findViewById(R.id.user_name);
        infoTextView = headView.findViewById(R.id.info_text_view);
        setBackground();

        Intent intent = getIntent();
        session = (Session) intent.getSerializableExtra("session");
        final MessageUtil messageUtil = intent.getParcelableExtra("messageUtil");

        linearLayoutBar = findViewById(R.id.linear_layout_bar);
        listView = findViewById(R.id.list_item);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            ViewGroup.LayoutParams params=linearLayoutBar.getLayoutParams();
            boolean scroll = false; // 是否滑动
            boolean trend = false;  // 是否向上滑动
            int lastIndex = 0;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    // 当不滚动时
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:// 是当屏幕停止滚动时
                        scroll=false;
                        // 判断滚动到底部
                        if (listView.getLastVisiblePosition() == (listView
                                .getCount() - 1)) {

                        }
                        // 判断滚动到顶部
                        if (listView.getFirstVisiblePosition() == 0) {

                        }

                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 滚动时
                        scroll=true;
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:// 是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
                        scroll=true;
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastIndex = firstVisibleItem;
                if (lastIndex-firstVisibleItem<=0){
                    trend=true;
                }else {
                    trend=false;
                }
                if (firstVisibleItem<2){
                    params.height=150;
                    linearLayoutBar.setLayoutParams(params);
                }else {
                    if (trend&&scroll){
                        params.height=Math.max(linearLayoutBar.getHeight()-5, 0);
                        linearLayoutBar.setLayoutParams(params);
                    }else if (!trend&&scroll){
                        params.height=Math.min(linearLayoutBar.getHeight()+5, 150);
                        linearLayoutBar.setLayoutParams(params);
                    }if (trend&&!scroll){
                        params.height=0;
                        linearLayoutBar.setLayoutParams(params);
                    }
                }
            }
        });

        handler = new MyHandler(HomeActivity.this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> paramsMap = new HashMap<>();
                paramsMap.put("appid", "otn");
                String url = "https://kyfw.12306.cn/passport/web/auth/uamtk";
                JSONObject jsonObject0 = (JSONObject) session.post(url,null, paramsMap);

                try {
                    HashMap<String, String> paramsMap1 = new HashMap<>();
                    paramsMap1.put("tk", jsonObject0.getString("newapptk"));
                    JSONObject jsonObject1 = (JSONObject) session.post("https://kyfw.12306.cn/otn/uamauthclient",null, paramsMap1);
                    JSONObject jsonObject2 = (JSONObject) session.post("https://kyfw.12306.cn/otn/index/initMy12306Api",null, null);
                    JSONObject jsonObject3 = (JSONObject) session.post("https://kyfw.12306.cn/otn/login/conf",null, null);
                    jsonObject = (JSONObject) session.post("https://kyfw.12306.cn/otn/modifyUser/initQueryUserInfoApi",null, null);
                    JSONObject jsonObject4 = (JSONObject) session.post("https://kyfw.12306.cn/otn/login/conf",null, null);

                    HashMap<String, String> paramsMap2 = new HashMap<>();
                    paramsMap2.put("pageIndex", "1");
                    paramsMap2.put("pageSize", "10");
                    JSONObject jsonObject5 = (JSONObject) session.post("https://kyfw.12306.cn/otn/passengers/query",null, paramsMap2);
                    ArrayList<String> info = new ArrayList<>();
                    info.add("姓名: "+jsonObject.getJSONObject("data").getJSONObject("userDTO").getJSONObject("loginUserDTO").getString("name"));
                    info.add("用户名: "+jsonObject.getJSONObject("data").getJSONObject("userDTO").getJSONObject("loginUserDTO").getString("user_name"));
                    info.add("性别: "+jsonObject.getJSONObject("data").getJSONObject("userDTO").getString("sex_code"));
                    info.add("生日: "+jsonObject.getJSONObject("data").getJSONObject("userDTO").getString("born_date"));
                    info.add("身份证号: "+jsonObject.getJSONObject("data").getJSONObject("userDTO").getJSONObject("loginUserDTO").getString("id_no"));
                    info.add("地址: "+jsonObject.getJSONObject("data").getJSONObject("userDTO").getString("address"));
                    info.add("邮箱: "+jsonObject.getJSONObject("data").getJSONObject("userDTO").getString("email"));
                    info.add("手机: "+jsonObject.getJSONObject("data").getJSONObject("userDTO").getString("mobile_no"));
                    info.add("邮编: "+jsonObject.getJSONObject("data").getJSONObject("userDTO").getString("postalcode"));
                    info.add("类型: "+jsonObject.getJSONObject("data").getString("userTypeName"));
                    info.add("国家: "+jsonObject.getJSONObject("data").getString("country_name"));
                    info.add("验证: "+jsonObject.getJSONObject("data").getString("notice"));
                    info.add("IP: "+jsonObject.getJSONObject("data").getJSONObject("userDTO").getJSONObject("loginUserDTO").getString("userIpAddress"));
                    JSONArray arrayList = jsonObject5.getJSONObject("data").getJSONArray("datas");
                    for (int i=0;i<arrayList.length();i++){
                        JSONObject data = arrayList.getJSONObject(i);
                        String passenger_name = data.getString("passenger_name");
                        String sex_name;
                        if (data.has("sex_name")){
                            sex_name = data.getString("sex_name");
                        }else {
                            sex_name = "？";
                        }
                        String passenger_id_no = data.getString("passenger_id_no");
                        info.add(String.format("联系人: %s  %s  %s", passenger_name, sex_name, passenger_id_no));
                    }
                    info.add(SystemUtil.getVersionCode(getApplicationContext())+"");
                    info.add(SystemUtil.getVersionName(getApplicationContext()));
                    items = info.toArray(new String[info.size()]);
                    handler.sendEmptyMessage(1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int menuId = menuItem.getItemId();
                switch (menuId){
                    case R.id.nav_update:
                        checkUpdate();
                        return true;
                    case R.id.nav_setting:
                        Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_share:
                        Intent intent_share=new Intent(Intent.ACTION_SEND);
//                        intent_share.setType("image/*");
                        intent_share.setType("text/plain");
                        intent_share.putExtra(Intent.EXTRA_SUBJECT, "分享");
                        intent_share.putExtra(Intent.EXTRA_TEXT, "我有一个好玩的应用,推荐你下载! \r\n http://imtt.dd.qq.com/16891/08D63F6E91D1713194CBC3929B0BB7CC.apk");
                        intent_share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(Intent.createChooser(intent_share, getTitle()));
                        break;
                    default:
                        break;
                }
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void setBackground() {
        String image_path = getFilesDir().getAbsolutePath()+File.separator+"welcome.jpg";
        boolean exists = new File(image_path).exists();
        if (exists){
            LinearLayout linearLayout = findViewById(R.id.linear_layout_home);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(image_path);
                Bitmap bmp  = BitmapFactory.decodeStream(fis);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bmp);
                linearLayout.setBackground(bitmapDrawable);
            } catch (FileNotFoundException e) {
                linearLayout.setBackground(getResources().getDrawable(R.drawable.welcome));
                e.printStackTrace();
            }
        }
    }

    protected String getMIMEType(File file) {
        String type = "";
        String fileName = file.getName();
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
        final String path = getFilesDir().getAbsolutePath()+ File.separator+"updata.apk";
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
//                    handler.sendEmptyMessage(3);
                    installApk(file);
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

    public void checkUpdate(){
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
    }

    @Override
    public void onBackPressed() {
        /**
         * 监听back键 防止直接结束当前activity
         */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
