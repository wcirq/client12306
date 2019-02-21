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
import java.util.Map;

public class Session {
    private HashMap<String, HashMap<String, String>> COOKIES = new HashMap<>();
    private String url;

    public Session(){

    }

    private HttpURLConnection dealCookie(HttpURLConnection httpURLConnection){
        List<String> cookies = httpURLConnection.getHeaderFields().get("Set-Cookie");
        for (String cookie:cookies){
            String[] argses = cookie.split(";");
            String pathKey=null, expires=null;
            HashMap<String, String> cookieValue = new HashMap<>();
            for (String args:argses){
                if (args.toLowerCase().contains("path")){
                    int index = args.indexOf("=");
                    pathKey = args.substring(index+1, args.length());
                }else if (args.toLowerCase().contains("expires")){
                    expires = args;
                }else if (args.toLowerCase().contains("=")){
                    String[] data = args.split("=");
                    cookieValue.put(data[0], data[1]);
                }
            }
            COOKIES.put(pathKey, cookieValue);
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

    private HttpURLConnection setCookie(HttpURLConnection httpURLConnection) {
        String pathKey = this.url;
        try {
            URL url = new URL(this.url);
            pathKey =url.getPath();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String cookies = "";
        for (Map.Entry<String, HashMap<String, String>> cookiesPath:COOKIES.entrySet()){
            String key = cookiesPath.getKey();
            if (pathKey.contains(key)){
                HashMap<String,String> value = cookiesPath.getValue();
                for (Map.Entry<String, String> cookie:value.entrySet()){

                }
            }
        }
        return httpURLConnection;
    }

    public Object get(String url, HashMap<String, String> headers) {
        this.url = url;
        HttpURLConnection httpURLConnection=null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection = setRequestProperty(httpURLConnection, headers);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection = setCookie(httpURLConnection);
            if (httpURLConnection.getResponseCode() == 200) {
                httpURLConnection = dealCookie(httpURLConnection);
            }


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
