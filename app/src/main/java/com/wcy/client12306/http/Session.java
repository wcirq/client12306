package com.wcy.client12306.http;

import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/***
 * 实现自动管理Cookie
 */
public class Session implements Serializable {
    private HashMap<String, HashMap<String, String>> COOKIES = new HashMap<>();
    private String url;

    public Session(){

    }

    public void clearCookies(){
        COOKIES.clear();
    }

    public void addCookies(HashMap<String, HashMap<String, String>> cookies){
        for (Map.Entry<String, HashMap<String, String>> cookiesPath:cookies.entrySet()){
            String keyPath = cookiesPath.getKey();
            if (!COOKIES.containsKey(keyPath)){
                COOKIES.put(keyPath, cookies.get(keyPath));
                COOKIES.put(keyPath, cookies.get(keyPath));
            }else {
                for (Map.Entry<String, String> cookiesPathValue:cookiesPath.getValue().entrySet()) {
                    Objects.requireNonNull(COOKIES.get(keyPath)).put(cookiesPathValue.getKey(), cookiesPathValue.getValue());
                }
            }
        }
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
        httpURLConnection.setRequestProperty("User-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_4) AppleWebKit/537.36 (KHTML, like Gecko) 12306-electron/1.0.1 Chrome/59.0.3071.115 Electron/1.8.4 Safari/537.36");
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpURLConnection.setRequestProperty("Origin", "https://kyfw.12306.cn");
        httpURLConnection.setRequestProperty("Connection", "keep-alive");

        if (headers != null) {
            for (String key : headers.keySet()) {
                httpURLConnection.setRequestProperty(key, headers.get(key));
            }
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

        StringBuilder cookiesBuffer = new StringBuilder();
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
                StringBuilder stringBuilder = new StringBuilder();
                String temp = null;
                while ((temp = bufferedReader.readLine()) != null) {
                    stringBuilder.append(temp);
                    stringBuilder.append("\r\n");
                }
                String str = stringBuilder.toString();
                if(str.contains("jQuery")) {
                    str = str.split("\\(")[1].split("\\)")[0];
                }else if(str.contains("callbackFunction")){
                    str = str.split("\\(")[1].split("\\)")[0];
                    str = str.substring(1, str.length()-1);
                }
                result = new JSONObject(str);

            } else if (httpURLConnection.getContentType().contains("text/html")|httpURLConnection.getContentType().contains("text/javascript")) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                StringBuilder stringBuilder = new StringBuilder();
                String temp = null;
                while ((temp = bufferedReader.readLine()) != null) {
                    stringBuilder.append(temp);
                    stringBuilder.append("\r\n");
                }
                result = stringBuilder.toString();
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
            httpURLConnection.setConnectTimeout(5000);
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
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection = setRequestProperty(httpURLConnection, headers);
            httpURLConnection = setCookie(httpURLConnection);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.connect();
            httpURLConnection = setData(httpURLConnection, data);
            Log.d("*****", "00000" +url);
            if (httpURLConnection.getResponseCode() == 200) {
                Log.d("*****", "11111");
                dealCookie(httpURLConnection);
                result = dealResult(httpURLConnection);
            }
        } catch (SocketTimeoutException e) {
            Log.d("超时", e.toString());
            return null;
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

    public static void dump(Session session, String userInfoPath){
        if (userInfoPath==null){
            userInfoPath = "/data/user/0/com.wcy.client12306/files/userInfo.ser";
        }
        FileOutputStream fs = null;
        ObjectOutputStream os = null;
        try {
            fs = new FileOutputStream(userInfoPath);
            os = new ObjectOutputStream(fs);
            os.writeObject(session);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Session load(String userInfoPath){
        File file = new File(userInfoPath);
        ObjectInputStream ois = null;
        Session session;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            session = (Session) ois.readObject();
            ois.close();
            return session;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String args[]){
        String url = "https://kyfw.12306.cn/passport/captcha/captcha-image?login_site=E&module=login&rand=sjrand&0.6523880813900003";
        Session session = new Session();
        session.get(url,null);
    }

}
