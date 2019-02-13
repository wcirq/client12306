package com.wcy.client12306.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session {
    private String sessionId = null;
    private String cookie = null;
    HttpURLConnection httpURLConnection = null;

    public Session(){

    }

    public Object get(String url, HashMap<String, String> headers) throws MalformedURLException {
        httpURLConnection = new HttpURLConnection(new URL("")) {
            @Override
            public void disconnect() {

            }

            @Override
            public boolean usingProxy() {
                return false;
            }

            @Override
            public void connect() throws IOException {

            }
        };
        return null;
    }

    public Object post(String url, HashMap<String, String> headers, HashMap<String, String> data) {
        return null;
    }

    public static void main(String args[]){

    }

}
