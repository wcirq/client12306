package com.wcy.client12306.test;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MyTasks extends AsyncTask<Void, Void, String> {
    private OnFinishTask onFinishTask;
    private String user;
    private String pass;
    private String result;

    private List<String> finalCookie = new ArrayList<String>();
    private String jiaowuchu = "http://zhjw.dlut.edu.cn/loginAction.do";
    public MyTasks (String user, String pass, OnFinishTask onFinishTask) {
        this.user = user;
        this.pass = pass;

        this.onFinishTask = onFinishTask;
    }

    @Override
    protected String doInBackground(Void... voids) {
        URL url;
        HttpURLConnection httpURLConnection;

        try {
            url = new URL(jiaowuchu);
            httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setDoOutput(true);//是否向链接输出
            httpURLConnection.setDoInput(true);

            httpURLConnection.setRequestMethod("POST");

            httpURLConnection.setUseCaches(false);
            httpURLConnection.setInstanceFollowRedirects(true);

            //不知道为什么加了这一行就不行了
            httpURLConnection.setRequestProperty("Content-Type","text/html; charset=GBK");
            httpURLConnection.setConnectTimeout(10*1000);//连接超时 单位毫秒
            httpURLConnection.setReadTimeout(10*1000);//读取超时 单位毫秒

            // 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
            // 要注意的是connection.getOutputStream会隐含的进行connect。
            httpURLConnection.connect();
            DataOutputStream out = new DataOutputStream(httpURLConnection
                    .getOutputStream());


            StringBuffer params = new StringBuffer();
            params.append("zjh").append("=").append(user).append("&")
                    .append("mm").append("=").append(pass);
            httpURLConnection.getOutputStream().write(params.toString().getBytes("gb2312"));
            out.flush();
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpURLConnection.getInputStream(), "gb2312"));
            String line;
            while ((line = reader.readLine()) != null) {
                result = result + line;
            }
            reader.close();

            result="";
            Map<String, List<String>> header = httpURLConnection.getHeaderFields();
            List<String> cookies = header.get("Set-Cookie");
            Iterator<String> it = cookies.iterator();
            StringBuffer sbu = new StringBuffer();
            //  sbu.append("eos_style_cookie=default; ");
            while(it.hasNext()){
                sbu.append(it.next());
            }

            result = cookies.size()+"";

            for (int i = 0; i < cookies.size(); i++) {
                finalCookie.add(cookies.get(i));
            }

            result = result+finalCookie.get(0);
            result = result+"--------------"+finalCookie.get(1);

            httpURLConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get课表

        try {
            url = new URL("http://zhjw.dlut.edu.cn/xkAction.do?actionType=6");
            httpURLConnection = (HttpURLConnection) url.openConnection();
            //必要的设置
            httpURLConnection.setConnectTimeout(10*1000);//连接超时 单位毫秒
            httpURLConnection.setReadTimeout(10*1000);//读取超时 单位毫秒
            httpURLConnection.setRequestProperty("Cookie", finalCookie.get(0)+", "+finalCookie.get(1));
            System.out.println(finalCookie.get(0)+", "+finalCookie.get(1));
//            httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=nprNP171vDeCHb_5hawDv; path=/, NSC_kjbpxv-iuuq=2385a3d4705debd5f29101f47dcbd3b0e664f181853b4741569229a0561453d027e6fa67;path=/");
            httpURLConnection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "gb2312"));
            String lines;
            //result = "";
            while ((lines = reader.readLine()) != null) {
                result = result + lines;
            }
            reader.close();
            // 断开连接
            result = result + httpURLConnection.getResponseCode();
            httpURLConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        onFinishTask.onFinish(s);
    }

    public interface OnFinishTask {
        public void onFinish(String data);
    }
}
