package com.wcy.client12306.test.tcp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

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
        String requestLine = String.format("%s %s HTTP/1.1\r\n", type, FILE);
        this.requestArray[0] = requestLine;

        StringBuilder requestHeards = new StringBuilder();
        if (heards!=null){
            for (HashMap.Entry<String, String> heard: heards.entrySet()){
                requestHeards.append(String.format("%s:%s", heard.getKey(), heard.getValue())).append("\r\n");
            }
            requestHeards.delete(0,2);
            this.requestArray[1] = requestHeards.toString();
        }else {
            StringBuffer datas = new StringBuffer();
            try {
                datas.append(URLEncoder.encode("name", "utf-8")).append("=").append(URLEncoder.encode("wcy", "utf-8")).append("&").append(URLEncoder.encode("age", "utf-8")).append("=").append(URLEncoder.encode("25", "utf-8")).append("");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            this.requestArray[1] = "" +
                    "Accept: text/html, application/xhtml+xml, image/jxr, */*\r\n" +
                    "Accept-Encoding: gzip, deflate\r\n" +
                    "Content-Encoding: utf-8\r\n" +
                    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0\r\n" +
                    "Content-Length: " + datas.length() + "\r\n" +
                    "Content-Type: application/x-www-form-urlencoded\r\n"
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
            try {
                this.requestArray[3] = URLEncoder.encode("name", "utf-8") + "="
                        + URLEncoder.encode("wcy", "utf-8") + "&"
                        + URLEncoder.encode("age", "utf-8") + "="
                        + URLEncoder.encode("25", "utf-8") + "\r\n";
//                this.requestArray[3] = "name=wcy&age=25\r\n";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ;
        }
        if(type.equals("GET")){
            request = String.format("%s%s", this.requestArray[0], this.requestArray[1]);
        }else {
            request = String.format("%s%s%s%s", this.requestArray[0], this.requestArray[1], this.requestArray[2],this.requestArray[3]);
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
//            OutputStream out = socket.getOutputStream();
//            out.write(request.getBytes());
//            out.flush();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            out.write(request);
            out.write("\r\n");
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
            in.close();
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

        httpConnect.post("http://127.0.0.1:8000/hello", null, null);

//        httpConnect.get("http://www.baidu.com/", null);
    }
}
