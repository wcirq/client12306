package com.wcy.client12306.test;

import android.util.Log;

import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MySession {
    private OkHttpClient client;

    public MySession(){

    }

    public void getDatasync(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://www.baidu.com")//请求接口。如果需要传参拼接到接口后面。
                            .build();//创建Request 对象
                    Response response = null;
                    response = client.newCall(request).execute();//得到Response 对象
                    if (response.isSuccessful()) {
                        Log.d("kwwl","response.code()=="+response.code());
                        Log.d("kwwl","response.message()=="+response.message());
                        Log.d("kwwl","res=="+response.body().string());
                        //此时的代码执行在子线程，修改UI的操作请使用handler跳转到UI线程。
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String args[])
    {
        MySession mySession = new MySession();
        mySession.getDatasync();
    }
}
