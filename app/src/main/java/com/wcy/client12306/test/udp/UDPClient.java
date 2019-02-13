package com.wcy.client12306.test.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPClient {

    public static void main(String[] args) throws Exception {

        DatagramSocket ds = null;
        DatagramPacket dp = null;
        byte[] buf = new byte[1024];

        ds = new DatagramSocket(3333);
        dp = new DatagramPacket(buf, 1024);

        // 接收数据，放入数据报
        ds.receive(dp);
        // 从数据报中取出数据
        String info = new String(dp.getData(),0, dp.getLength());
        System.out.println("接收到的信息是：" + info);
    }

}
