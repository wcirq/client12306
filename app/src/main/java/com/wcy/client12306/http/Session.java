package com.wcy.client12306.http;

import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session implements Serializable {
    private HashMap<String, HashMap<String, String>> COOKIES = new HashMap<>();
    private String url;

    public Session(){

    }

    private void dealCookie(HttpURLConnection httpURLConnection){
        List<String> cookies = httpURLConnection.getHeaderFields().get("Set-Cookie");
        if (cookies!=null){
            for (String cookie:cookies){
                String[] argses = cookie.split(";");
                String pathKey=null, expires=null;
                HashMap<String, String> cookieValue = new HashMap<>();
                String[] data = new String[2];
                for (String args:argses){
                    if (args.toLowerCase().contains("path")){
                        int index = args.indexOf("=");
                        pathKey = args.substring(index+1, args.length());
                    }else if (args.toLowerCase().contains("expires")){
                        expires = args;
                    }else if (args.toLowerCase().contains("=")){
                        String[] array = args.split("=");
                        if (array.length<2){
                            data[0]=array[0];
                            data[1]="";
                        }else {
                            data=array.clone();
                        }
                        cookieValue.put(data[0], data[1]);
                    }
                }
                if (!COOKIES.containsKey(pathKey)){
                    COOKIES.put(pathKey, cookieValue);
                }else {
                    COOKIES.get(pathKey).put(data[0], data[1]);
                }
            }
        }
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

        StringBuffer cookiesBuffer = new StringBuffer();
        for (Map.Entry<String, HashMap<String, String>> cookiesPath:COOKIES.entrySet()){
            String key = cookiesPath.getKey();
            if (pathKey.contains(key)){
                HashMap<String,String> value = cookiesPath.getValue();
                for (Map.Entry<String, String> cookie:value.entrySet()){
                    String cookieKeyValue = String.format("%s=%s",cookie.getKey(), cookie.getValue());
                    cookiesBuffer.append(cookieKeyValue).append(";");
                }
            }
        }
        if (cookiesBuffer.length()>0) {
            cookiesBuffer.deleteCharAt(cookiesBuffer.length()-1);
            httpURLConnection.setRequestProperty("Cookie", cookiesBuffer.toString());
        }
        return httpURLConnection;
    }

    private Object dealResult(HttpURLConnection httpURLConnection) {
        Object result=null;
        try {
            InputStream inputStream = httpURLConnection.getInputStream();
            if (httpURLConnection.getContentType().contains("application/json")) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                StringBuffer stringBuffer = new StringBuffer();
                String temp = null;
                while ((temp = bufferedReader.readLine()) != null) {
                    stringBuffer.append(temp);
                    stringBuffer.append("\r\n");
                }
                String str = stringBuffer.toString();
                result = new JSONObject(str);
            } else if (httpURLConnection.getContentType().contains("text/html")|httpURLConnection.getContentType().contains("text/javascript")) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                StringBuffer stringBuffer = new StringBuffer();
                String temp = null;
                while ((temp = bufferedReader.readLine()) != null) {
                    stringBuffer.append(temp);
                    stringBuffer.append("\r\n");
                }
                result = stringBuffer.toString();
            } else if (httpURLConnection.getContentType().contains("image/jpeg")) {
                result = BitmapFactory.decodeStream(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object get(String url, HashMap<String, String> headers) {
        Object result=null;
        this.url = url;
        HttpURLConnection httpURLConnection=null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection = setRequestProperty(httpURLConnection, headers);
            httpURLConnection = setCookie(httpURLConnection);
            httpURLConnection.setRequestMethod("GET");
            if (httpURLConnection.getResponseCode() == 200) {
                dealCookie(httpURLConnection);
                result = dealResult(httpURLConnection);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally {
            if (httpURLConnection!=null){
                httpURLConnection.disconnect();
            }
        }
        return result;
    }

    private String getParams(HashMap<String, String> paramsMap) {
        StringBuilder result = new StringBuilder();
        for (HashMap.Entry<String, String> entity : paramsMap.entrySet()) {
            result.append("&").append(entity.getKey()).append("=").append(entity.getValue());
        }
        return result.substring(1);
    }

    private HttpURLConnection setData(HttpURLConnection httpURLConnection, HashMap<String, String> paramsMap) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(httpURLConnection.getOutputStream());
            String params = "";
            if (paramsMap!=null){
                params = getParams(paramsMap);
            }
            httpURLConnection.getOutputStream().write(params.getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return httpURLConnection;
    }

    public Object post(String url, HashMap<String, String> headers, HashMap<String, String> data) {
        Object result=null;
        this.url = url;
        HttpURLConnection httpURLConnection=null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection = setRequestProperty(httpURLConnection, headers);
            httpURLConnection = setCookie(httpURLConnection);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.connect();
            httpURLConnection = setData(httpURLConnection, data);
            if (httpURLConnection.getResponseCode() == 200) {
                dealCookie(httpURLConnection);
                result = dealResult(httpURLConnection);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally {
            if (httpURLConnection!=null){
                httpURLConnection.disconnect();
            }
        }
        return result;
    }

    public static void main(String args[]){
        String url = "https://kyfw.12306.cn/passport/captcha/captcha-image?login_site=E&module=login&rand=sjrand&0.6523880813900003";
        Session session = new Session();
        session.get(url,null);
    }

}
