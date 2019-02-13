package com.wcy.client12306.test.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Test {
    public static void main(String []args) throws IOException {
        Socket client = new Socket();
        InetSocketAddress inetSocketAddress = new InetSocketAddress("www.w3school.com", 80);
        // 建立TCP连接，虚拟机的地址为192.168.194.129
        // Nginx监听的端口设置为8080
        client.connect(inetSocketAddress, 1000);
        String request = "GET / HTTP/1.1\r\n"+
                "Host: www.qq.com:80\r\n\r\n";

//        PrintWriter pWriter = new PrintWriter(client.getOutputStream(),true);
//        pWriter.println(request);

        OutputStream out = client.getOutputStream();
        out.write(request.getBytes());
        out.write("\r\n".getBytes());
        out.flush();

        int len;
        InputStream in = client.getInputStream();
        StringBuffer html = new StringBuffer();
        byte []buf = new byte[1024];
        while ((len=in.read(buf)) != -1){
            html.append(new String(buf, 0, len,"utf-8"));
        }
        System.out.println(html);

//        out.close();
//        pWriter.close();
        client.close();
    }
}
