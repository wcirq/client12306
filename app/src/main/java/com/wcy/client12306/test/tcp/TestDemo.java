package com.wcy.client12306.test.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URLEncoder;

public class TestDemo {
    private int port;
    private String host;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    public TestDemo(String host, int port) {
        socket = new Socket();
        this.host = host;
        this.port = port;
    }

    public void sendGet() throws IOException
    {
        String path = "/";
        SocketAddress dest = new InetSocketAddress(this.host, this.port);
        socket.connect(dest);
        OutputStreamWriter streamWriter = new OutputStreamWriter(socket.getOutputStream());
        bufferedWriter = new BufferedWriter(streamWriter);

        bufferedWriter.write("GET " + path + " HTTP/1.1\r\n");
//        bufferedWriter.write("Host: " + this.host + "\r\n");
        bufferedWriter.write("User-Agent: " + "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0" + "\r\n");
        bufferedWriter.write("\r\n");
        bufferedWriter.write("\r\n");
        bufferedWriter.flush();

        int len;
        InputStream in = socket.getInputStream();
        StringBuffer html = new StringBuffer();
        byte []buf = new byte[2048];
        while ((len=in.read(buf)) != -1){
            html.append(new String(buf, 0, len,"utf-8"));
            System.out.println(html);
        }
        System.out.println(html);

//        BufferedInputStream streamReader = new BufferedInputStream(socket.getInputStream());
//        bufferedReader = new BufferedReader(new InputStreamReader(streamReader, "utf-8"));
//        String line = "";
//        while(!(line = bufferedReader.readLine()).equals("\n\n"))
//        {
//            System.out.println(line);
//        }
//        bufferedReader.close();
        bufferedWriter.close();
        socket.close();
    }

    public void sendPost() throws IOException
    {
        String path = "/";
        String data = URLEncoder.encode("name", "utf-8") + "=" + URLEncoder.encode("gloomyfish", "utf-8") + "&" +
                URLEncoder.encode("age", "utf-8") + "=" + URLEncoder.encode("32", "utf-8");
        // String data = "name=zhigang_jia";
        SocketAddress dest = new InetSocketAddress(this.host, this.port);
        socket.connect(dest);
        OutputStreamWriter streamWriter = new OutputStreamWriter(socket.getOutputStream(), "utf-8");
        bufferedWriter = new BufferedWriter(streamWriter);

        bufferedWriter.write("POST " + path + " HTTP/1.1\r\n");
        bufferedWriter.write("Host: " + this.host + "\r\n");
        bufferedWriter.write("Content-Length: " + data.length() + "\r\n");
        bufferedWriter.write("Content-Type: application/x-www-form-urlencoded\r\n");
        bufferedWriter.write("\r\n");
        bufferedWriter.write(data);
        bufferedWriter.flush();
        bufferedWriter.write("\r\n");
        bufferedWriter.flush();

        BufferedInputStream streamReader = new BufferedInputStream(socket.getInputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(streamReader, "utf-8"));
        String line ="" ;
        while(!(line = bufferedReader.readLine()).equals("==end=="))
        {
            System.out.println(line);
        }
        bufferedReader.close();
        bufferedWriter.close();
        socket.close();
    }

    public static void main(String[] args)
    {
        TestDemo td = new TestDemo("www.baidu.com",80);
        try {
             td.sendGet(); //send HTTP GET Request
//
//            td.sendPost(); // send HTTP POST Request
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
