package com.wcy.client12306.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    private List<String> responseCookie = null;
    private String sessionIdString = null;
    public HttpUtil(){

    }

    public Object get(String urlPath, HashMap<String, String> headers) {
        Object result=null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(urlPath);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            if (headers!=null){
                for (String key:headers.keySet()){
                    httpURLConnection.setRequestProperty(key, headers.get(key));
                }
            }else {
                httpURLConnection.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
            }
            if (responseCookie!=null){
                String request1 = responseCookie.get(0).split(";")[0];
                String request2 = responseCookie.get(1).split(";")[0];
                String request3 = responseCookie.get(2).split(";")[0];
                String cookie = request1+";"+request2+";"+request3;
                httpURLConnection.setRequestProperty("Cookie", cookie);// 给服务器送登录后的cookie
            }
            httpURLConnection.setRequestMethod("GET");
            if (httpURLConnection.getResponseCode() == 200) {
                if (responseCookie==null){
                    responseCookie = httpURLConnection.getHeaderFields().get("Set-Cookie");
                }else {
                    Map<String, List<String>> header = httpURLConnection.getHeaderFields();
                    Log.d("","");
                }
                InputStream inputStream = httpURLConnection.getInputStream();
                if (httpURLConnection.getContentType().contains("application/json")){
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                    // Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                    StringBuffer stringBuffer = new StringBuffer();
                    String temp = null;
                    while ((temp = bufferedReader.readLine()) != null) {
                        stringBuffer.append(temp);
                        stringBuffer.append("\n");
                    }
                    String str = stringBuffer.toString();
                    result = new JSONObject(str);
                }else if (httpURLConnection.getContentType().contains("text/html")){
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                    // Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                    StringBuffer stringBuffer = new StringBuffer();
                    String temp = null;
                    while ((temp = bufferedReader.readLine()) != null) {
                        stringBuffer.append(temp);
                        stringBuffer.append("\r\n");
                    }
                    result = stringBuffer.toString();
                }else if (httpURLConnection.getContentType().contains("image/jpeg")){
                     result= BitmapFactory.decodeStream(inputStream);
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (httpURLConnection!=null){
                httpURLConnection.disconnect();
            }
        }
        return null;
    }

    public Object post(String urlPath, HashMap<String, String> headers, HashMap<String, String> paramsMap) {
        Object result=null;
        String params = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(urlPath);
            String a = url.getHost();
            httpURLConnection = (HttpURLConnection) url.openConnection();
            if (headers!=null){
                for (String key:headers.keySet()){
                    httpURLConnection.setRequestProperty(key, headers.get(key));
                }
            }else {
                httpURLConnection.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
            }
            if (paramsMap!=null){
                params = getParams(paramsMap);
            }else {
                // params = "";
                params="username=18685134228&password=wcy206211&appid=otn";
            }
            if (responseCookie!=null){
                String request1 = responseCookie.get(0).split(";")[0];
                String request2 = responseCookie.get(1).split(";")[0];
                String request3 = responseCookie.get(2).split(";")[0];
                String cookie = request1+";"+request2+";"+request3;
                httpURLConnection.setRequestProperty("Cookie", cookie);// 给服务器送登录后的cookie
            }
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.connect();
            DataOutputStream out = new DataOutputStream(httpURLConnection
                    .getOutputStream());
            httpURLConnection.getOutputStream().write(params.getBytes());
            out.flush();
            out.close();

            if (httpURLConnection.getResponseCode() == 200) {
                InputStream inputStream = httpURLConnection.getInputStream();
                if (httpURLConnection.getContentType().contains("application/json")){
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                    // Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                    StringBuffer stringBuffer = new StringBuffer();
                    String temp = null;
                    while ((temp = bufferedReader.readLine()) != null) {
                        stringBuffer.append(temp);
                        stringBuffer.append("\n");
                    }
                    String str = stringBuffer.toString();
                    result = new JSONObject(str);
                }else if (httpURLConnection.getContentType().contains("text/html")){
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                    // Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                    StringBuffer stringBuffer = new StringBuffer();
                    String temp = null;
                    while ((temp = bufferedReader.readLine()) != null) {
                        stringBuffer.append(temp);
                        stringBuffer.append("\r\n");
                    }
                    result = stringBuffer.toString();
                }else if (httpURLConnection.getContentType().contains("image/jpeg")){
                    result= BitmapFactory.decodeStream(inputStream);
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (httpURLConnection!=null){
                httpURLConnection.disconnect();
            }
        }
        return null;
    }

    private String getParams(HashMap<String, String> paramsMap) {
        StringBuilder result = new StringBuilder();
        for (HashMap.Entry<String, String> entity : paramsMap.entrySet()) {
            result.append("&").append(entity.getKey()).append("=").append(entity.getValue());
        }
        return result.substring(1);
    }

    public static void main(String args[]){
        HttpUtil networkUtil = new HttpUtil();
        JSONObject result1 = (JSONObject) networkUtil.get("http://192.168.150.151:8000/getjson", null);
        String result2 = (String) networkUtil.get("http://192.168.150.151:8000/hello", null);

        String url = "https://kyfw.12306.cn/passport/captcha/captcha-image?login_site=E&module=login&rand=sjrand&0.6523880813900003";
        Bitmap result3 = (Bitmap) networkUtil.get(url, null);
        System.out.print("");
    }

}
