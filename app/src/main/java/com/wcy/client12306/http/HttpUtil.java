package com.wcy.client12306.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class HttpUtil {
    public HttpUtil(){

    }

    public static Object doGet(String urlPath, HashMap<String, String> headers) {
        Object result=null;
        try {
            URL url = new URL(urlPath);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            if (headers!=null){
                for (String key:headers.keySet()){
                    httpURLConnection.setRequestProperty(key, headers.get(key));
                }
            }else {
                httpURLConnection.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
            }
            httpURLConnection.setRequestMethod("GET");
            if (httpURLConnection.getResponseCode() == 200) {
                InputStream inputStream = httpURLConnection.getInputStream();
                if (httpURLConnection.getContentType().equals("application/json")){
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
                }else if (httpURLConnection.getContentType().equals("text/html; charset=utf-8")){
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                    // Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                    StringBuffer stringBuffer = new StringBuffer();
                    String temp = null;
                    while ((temp = bufferedReader.readLine()) != null) {
                        stringBuffer.append(temp);
                        stringBuffer.append("\r\n");
                    }
                    result = stringBuffer.toString();
                }else if (httpURLConnection.getContentType().equals("image/jpeg")){
                     result= BitmapFactory.decodeStream(inputStream);
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String args[]){
        HttpUtil networkUtil = new HttpUtil();
        JSONObject result1 = (JSONObject) networkUtil.doGet("http://192.168.150.151:8000/getjson", null);
        String result2 = (String) networkUtil.doGet("http://192.168.150.151:8000/hello", null);

        String url = "https://kyfw.12306.cn/passport/captcha/captcha-image?login_site=E&module=login&rand=sjrand&0.6523880813900003";
        Bitmap result3 = (Bitmap) networkUtil.doGet(url, null);
        System.out.print("");
    }

}
