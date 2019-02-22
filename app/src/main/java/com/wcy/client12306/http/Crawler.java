package com.wcy.client12306.http;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {
    Session session = new Session();

    public static List<String> getMatcher(String str,String url){
        List<String> result = new ArrayList<String>();
        Pattern p =Pattern.compile(url);//获取网页地址
        Matcher m =p.matcher(str);
        while(m.find()){
            //System.out.println(m.group(1));
            result.add(m.group(1));
        }
        return result;
    }

    public String getUrl(){
        String html = null;
        try {
            String imageUrl = "https://sj.enterdesk.com/woman/"+(int) (Math.random() * 20+1)+".html";
//            str = getHtml("https://sj.enterdesk.com/"+(int) (Math.random() * 20+1)+".html");
            html = (String) session.get(imageUrl,null);
            List<String> ouput = getMatcher(html, "src=\"(https://up.enterdesk.com[\\w\\s./:]+?)\"");
            int index = (int) (Math.random() * ouput.size());
            String url = ouput.get(index);
            return url;
        } catch (IndexOutOfBoundsException e){
            StackTraceElement warning = e.getStackTrace()[1];
            Log.w(String.format("网络错误 位置 [%s:%s]", warning.getFileName(), warning.getLineNumber()),"没有匹配到图片");
            return null;
        }
    }
}
