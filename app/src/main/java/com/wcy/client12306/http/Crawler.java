package com.wcy.client12306.http;

import android.app.Activity;
import android.util.Log;

import com.wcy.client12306.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {
    Session session;

    public Crawler(){
        session = new Session();
    }

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

    public String getBaiduImageUrl(Activity activity){
        String imageUrl = "http://image.baidu.com/search/flip?tn=baiduimage&ie=utf-8&word=%s&pn=%d&gsm=50&ct=&ic=0&lm=-1&width=0&height=0";
        String type = SystemUtil.getFile(activity, "wallpaper.txt");
        if (type!=null){
            imageUrl = String.format(imageUrl, type, (int)Math.random()*100);
        }else {
            imageUrl = String.format(imageUrl, "手机壁纸", (int)Math.random()*100);
        }

        String html = (String) session.get(imageUrl,null);
        if (html==null){
            return null;
        }else {
            List<String> ouput = getMatcher(html, "\"objURL\":\"(.*?)\"");
            int index = (int) (Math.random() * ouput.size());
            String url = ouput.get(index);
            return url;
        }
    }
}
