package com.wcy.client12306;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wcy.client12306.http.Session;
import com.wcy.client12306.util.MessageUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class DetailsActivity extends AppCompatActivity {
    Handler handler;
    String items[]=null;
    private Session networkUtil;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    JSONObject jsonObject;

    private static class MyHandler extends Handler {
        private final WeakReference<DetailsActivity> mTarget;

        MyHandler(DetailsActivity target) {
            mTarget = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            DetailsActivity activity = mTarget.get();
            if (msg.what == 1) {
                activity.arrayAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, activity.items);
                activity.listView.setAdapter(activity.arrayAdapter);
            } else if (msg.what == 2) {

            }else if (msg.what==3){

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // 继承的是AppCompatActivity时
        }
        setContentView(R.layout.activity_details);
        handler = new DetailsActivity.MyHandler(DetailsActivity.this);
        Intent intent = getIntent();
        networkUtil = (Session) intent.getSerializableExtra("httpUtil");
        final MessageUtil messageUtil = intent.getParcelableExtra("messageUtil");
        listView = findViewById(R.id.list_item);


        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> paramsMap = new HashMap<>();
                paramsMap.put("appid", "otn");
                String url = "https://kyfw.12306.cn/passport/web/auth/uamtk";
                JSONObject jsonObject0 = (JSONObject) networkUtil.post(url,null, paramsMap);

                try {
                    HashMap<String, String> paramsMap1 = new HashMap<>();
                    paramsMap1.put("tk", jsonObject0.getString("newapptk"));
                    JSONObject jsonObject1 = (JSONObject) networkUtil.post("https://kyfw.12306.cn/otn/uamauthclient",null, paramsMap1);
                    JSONObject jsonObject2 = (JSONObject) networkUtil.post("https://kyfw.12306.cn/otn/index/initMy12306Api",null, null);
                    JSONObject jsonObject3 = (JSONObject) networkUtil.post("https://kyfw.12306.cn/otn/login/conf",null, null);
                    jsonObject = (JSONObject) networkUtil.post("https://kyfw.12306.cn/otn/modifyUser/initQueryUserInfoApi",null, null);
                    JSONObject jsonObject4 = (JSONObject) networkUtil.post("https://kyfw.12306.cn/otn/login/conf",null, null);

                    HashMap<String, String> paramsMap2 = new HashMap<>();
                    paramsMap2.put("pageIndex", "1");
                    paramsMap2.put("pageSize", "10");
                    JSONObject jsonObject5 = (JSONObject) networkUtil.post("https://kyfw.12306.cn/otn/passengers/query",null, paramsMap2);
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
                    items = info.toArray(new String[info.size()]);
                    handler.sendEmptyMessage(1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
