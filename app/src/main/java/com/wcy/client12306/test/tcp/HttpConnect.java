package com.wcy.client12306.test.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpConnect {
    InetSocketAddress inetAddress;
    private Socket socket;
    String requestArray[] = new String[4];
    String request;

    public HttpConnect() {
        socket = new Socket();
    }

    public String buildRequest(String type, String HOST, String FILE, HashMap<String, String> heards, HashMap<String, String> data){
        String request;
        String requestLine = String.format("%s %s HTTP/1.1", type, FILE);
        this.requestArray[0] = requestLine;

        StringBuilder requestHeards = new StringBuilder();
        if (heards!=null){
            for (HashMap.Entry<String, String> heard: heards.entrySet()){
                requestHeards.append(String.format("%s:%s", heard.getKey(), heard.getValue())).append("\r\n");
            }
            requestHeards.delete(0,2);
            this.requestArray[1] = requestHeards.toString();
        }else {
            this.requestArray[1] = "" +
                    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0\r\n"
//                    "Host: " + HOST + "\r\n"
            ;
        }
        String requestNull = "\r\n";
        this.requestArray[2] = requestNull;

        StringBuilder requestData = new StringBuilder();
        if (data!=null){
            for (HashMap.Entry<String, String> body: data.entrySet()){
                requestData.append(String.format("%s: %s", body.getKey(), body.getValue())).append("\r\n");
            }
            requestData.delete(0,2);
            this.requestArray[3] = requestData.toString();
        } else {
            this.requestArray[3] = "txtUserName=1400170226\r\n" +
                    "TextBox2=qlz520";
                               ;
        }
        if(type.equals("GET")){
            request = String.format("%s\r\n%s\r\n", this.requestArray[0], this.requestArray[1]);
        }else {
            request = String.format("%s\r\n%s%s%s", this.requestArray[0], this.requestArray[1], this.requestArray[2],this.requestArray[3]);
        }

        return request;
    }

    public Object post(String url, HashMap<String, String> heards, HashMap<String, String> data) {
        socket = new Socket();
        String IP;
        String FILE;
        int PORT;
        try {
            URL URL = new URL(url);
            IP = URL.getHost();
            PORT = URL.getPort();
            FILE = URL.getFile();
            if(IP.equals("")){
                IP="127.0.0.1";
            }
            if(PORT==-1){
                PORT=80;
            }
            String HOST = String.format("%s:%s", IP, PORT);
            request = buildRequest("POST", HOST, FILE, heards, data);
            System.out.println(request);
            inetAddress = new InetSocketAddress(IP, PORT);
            socket.connect(inetAddress, 1000);
            OutputStream out = socket.getOutputStream();
            out.write(request.getBytes());
            out.write("\r\n".getBytes());
            BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String tem;
            while(!(tem = buffer.readLine()).equals("")) {
                System.out.println(tem);
            }
            out.close();
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public Object get(String url, HashMap<String, String> heards) {
        socket = new Socket();
        String IP;
        String FILE;
        String Protocol;
        int PORT;
        try {
            URL URL = new URL(url);
            IP = URL.getHost();
            PORT = URL.getPort();
            FILE = URL.getPath();
            if(IP.equals("")){
                IP="127.0.0.1";
            }
            if(PORT==-1){
                PORT=80;
            }
            String HOST = String.format("%s:%s", IP, PORT);
            request = buildRequest("GET", HOST, FILE, heards, null);
            System.out.println(request);
            System.out.println("#### ");
            inetAddress = new InetSocketAddress(IP, PORT);
            socket.connect(inetAddress, 2000);
            OutputStream out = socket.getOutputStream();
            out.write(request.getBytes());
            out.write("\r\n".getBytes());
            out.flush();
//            BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            String tem;
//            while(!(tem = buffer.readLine()).equals("")) {
//                System.out.println(tem);
//            }
            int len;
            InputStream in = socket.getInputStream();
            StringBuffer html = new StringBuffer();
            byte []buf = new byte[1024];
            while ((len=in.read(buf)) != -1){
                html.append(new String(buf, 0, len,"utf-8"));
            }
            System.out.println(html);
            out.close();
//            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static void main(String []args){
        HttpConnect httpConnect = new HttpConnect();

//        httpConnect.post("http://127.0.0.1:8000/hello", null, null);

        httpConnect.get("http://www.qq.com/", null);
    }
}
