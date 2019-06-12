package com.wcy.client12306.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.InputStream;

public class OcrModel {
    static {
        System.loadLibrary("tensorflow_inference");
    }

    //PATH TO OUR MODEL FILE AND NAMES OF THE INPUT AND OUTPUT NODES
    private String MODEL_PATH = "file:///android_asset/model.v2.0.pb";
    private String INPUT_NAME = "conv2d_11_input:0";
    private String OUTPUT_NAME = "dense_6/Softmax:0";
//    private String OUTPUT_NAME = "output_1";
    private TensorFlowInferenceInterface tf;

    //ARRAY TO HOLD THE PREDICTIONS AND FLOAT VALUES TO HOLD THE IMAGE DATA
    float[] PREDICTIONS = new float[1000];
    private float[] floatValues;
    private int[] INPUT_SIZE = {19,57,1};
    String verify_titles[] = {"打字机", "调色板", "跑步机", "毛线", "老虎", "安全帽", "沙包", "盘子", "本子", "药片", "双面胶", "龙舟", "红酒", "拖把", "卷尺", "海苔", "红豆", "黑板", "热水袋", "烛台", "钟表", "路灯", "沙拉", "海报", "公交卡", "樱桃", "创可贴", "牌坊", "苍蝇拍", "高压锅", "电线", "网球拍", "海鸥", "风铃", "订书机", "冰箱", "话梅", "排风机", "锅铲", "绿豆", "航母", "电子秤", "红枣", "金字塔", "鞭炮", "菠萝", "开瓶器", "电饭煲", "仪表盘", "棉棒", "篮球", "狮子", "蚂蚁", "蜡烛", "茶盅", "印章", "茶几", "啤酒", "档案袋", "挂钟", "刺绣", "铃铛", "护腕", "手掌印", "锦旗", "文具盒", "辣椒酱", "耳塞", "中国结", "蜥蜴", "剪纸", "漏斗", "锣", "蒸笼", "珊瑚", "雨靴", "薯条", "蜜蜂", "日历", "口哨"};

    public OcrModel(AssetManager assetManager){
        tf = new TensorFlowInferenceInterface(assetManager,MODEL_PATH);
    }

    public Object[] argmax(float[] array){


        int best = -1;
        float best_confidence = 0.0f;

        for(int i = 0;i < array.length;i++){

            float value = array[i];

            if (value > best_confidence){

                best_confidence = value;
                best = i;
            }
        }

        return new Object[]{best,best_confidence};
    }

    public void predict(final Bitmap bitmap){


        //Runs inference in background thread
        new AsyncTask<Integer,Integer,Integer>(){

            @Override

            protected Integer doInBackground(Integer ...params){

                //Resize the image into 224 x 224
                Bitmap resized_image = ImageUtil.processBitmap(bitmap, INPUT_SIZE[1], INPUT_SIZE[0]);
//                Bitmap convertGreyImg = ImageUtil.convertGreyImg(resized_image);

                //Normalize the pixels
//                floatValues = ImageUtil.normalizeBitmap(convertGreyImg,INPUT_SIZE[1], INPUT_SIZE[0],127.5f,1.0f);
                floatValues = ImageUtil.dealImage(resized_image);

                //Pass input into the tensorflow
                tf.feed(INPUT_NAME,floatValues,1,INPUT_SIZE[0], INPUT_SIZE[1],INPUT_SIZE[2]);

                //compute predictions
                tf.run(new String[]{OUTPUT_NAME});

                //copy the output into the PREDICTIONS array
                tf.fetch(OUTPUT_NAME,PREDICTIONS);

                //Obtained highest prediction
                Object[] results = argmax(PREDICTIONS);


                int class_index = (Integer) results[0];
                float confidence = (Float) results[1];


                try{

                    final String conf = String.valueOf(confidence * 100).substring(0,5);

                    //Convert predicted class index into actual label name
//                    final String label = ImageUtil.getLabel(inputStream,class_index);
                    final String label = verify_titles[class_index];
                    Log.d("结果：", label);
                }

                catch (Exception e){


                }


                return 0;
            }



        }.execute(0);

    }
}
