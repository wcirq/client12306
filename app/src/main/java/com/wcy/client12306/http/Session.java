package com.wcy.client12306.http;

import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Session {
    private HashMap<String, HashMap<String, String>> COOKIES = null;

    public Session(){

    }

    private HttpURLConnection dealCookie(HttpURLConnection httpURLConnection){
        List<String> cookies = httpURLConnection.getHeaderFields().get("Set-Cookie");
        for (String cookie:cookies){
            String[] argses = cookie.split(";");
            for (int i=0;i<argses.length;i++){
                String args = argses[i];
                int index = args.indexOf("=");
                System.out.println("");
            }
        }
        return httpURLConnection;
    }

    private HttpURLConnection setRequestProperty(HttpURLConnection httpURLConnection, HashMap<String, String> headers) {
        if (headers != null) {
            for (String key : headers.keySet()) {
                httpURLConnection.setRequestProperty(key, headers.get(key));
            }
        } else {
            httpURLConnection.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
        }
        return httpURLConnection;
    }

    public Object get(String url, HashMap<String, String> headers) {
        HttpURLConnection httpURLConnection=null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection = setRequestProperty(httpURLConnection, headers);
            httpURLConnection = dealCookie(httpURLConnection);

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (httpURLConnection!=null){
                httpURLConnection.disconnect();
            }
        }
        return null;
    }

    public Object post(String url, HashMap<String, String> headers, HashMap<String, String> data) {
        return null;
    }

    public static void main(String args[]){
        String url = "https://kyfw.12306.cn/passport/captcha/captcha-image?login_site=E&module=login&rand=sjrand&0.6523880813900003";
        Session session = new Session();
        session.get(url,null);
    }

}
