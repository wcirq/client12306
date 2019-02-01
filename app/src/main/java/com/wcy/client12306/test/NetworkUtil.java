package com.wcy.client12306.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class NetworkUtil {


    /*
     * 传入一个Url地址  返回一个JSON字符串
     * 网络请求的情况分析:
     *   如果是404 500 ... 代表网络(Http协议)请求失败
     *   200 服务器返回成功
     *       业务成功  /业务失败
     * */
    public static String doGet(String urlPath) {
        String result = null;// 返回结果字符串
        try {
            URL url = new URL(urlPath);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //设置请求头
//            httpURLConnection.setRequestProperty("token", "10051:abc");
//            httpURLConnection.setRequestProperty("Content-type", "application/json");
            httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
            httpURLConnection.setRequestProperty("contentType", "utf-8");
//            httpURLConnection.setRequestProperty("contentType", "GBK");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
            httpURLConnection.setRequestMethod("GET");
            if (httpURLConnection.getResponseCode() == 200) {
                InputStream is = httpURLConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                Bitmap bitmap= BitmapFactory.decodeStream(is);
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = reader.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{ \"success\": false,\n   \"errorMsg\": \"后台服务器开小差了!\",\n     \"result\":{}}";
    }

    /*
     * 传入一个Url地址  返回一个JSON字符串
     * */
    public static String doPost(String urlPath, HashMap<String, String> paramsMap) {
        String result = null;// 返回结果字符串
        try {
            URL url = new URL(urlPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            //--------------------------------
            conn.setDoOutput(true);
            conn.getOutputStream().write(getParams(paramsMap).getBytes());
            //--------------------------------
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = reader.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{ \"success\": false,\n   \"errorMsg\": \"后台服务器开小差了!\",\n     \"result\":{}}";
    }

    private static String getParams(HashMap<String, String> paramsMap) {
        String result = "";
        for (HashMap.Entry<String, String> entity : paramsMap.entrySet()) {
            result += "&" + entity.getKey() + "=" + entity.getValue();
        }
        return result.substring(1);
    }

    public static void main(String args[]){
        NetworkUtil networkUtil = new NetworkUtil();
        String result1 = networkUtil.doGet("192.168.150.1:8000/getjson");
    }


}
