package com.wcy.client12306.util;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.wcy.client12306.activity.LoginActivity;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class ClassifyModel {
    static {
        System.loadLibrary("tensorflow_inference");
    }

    //PATH TO OUR MODEL FILE AND NAMES OF THE INPUT AND OUTPUT NODES
    private String MODEL_PATH = "file:///android_asset/12306.image.model.pb";
    private String INPUT_NAME = "vgg16_input:0";
    private String OUTPUT_NAME = "dense_2/Softmax:0";
//    private String OUTPUT_NAME = "output_1";
    private TensorFlowInferenceInterface tf;

    //ARRAY TO HOLD THE PREDICTIONS AND FLOAT VALUES TO HOLD THE IMAGE DATA
    float[] PREDICTIONS = new float[80];
    private float[] floatValues;
    private int[] INPUT_SIZE = {72,72,3};
    String verify_titles[] = {"打字机", "调色板", "跑步机", "毛线", "老虎", "安全帽", "沙包", "盘子", "本子", "药片", "双面胶", "龙舟", "红酒", "拖把", "卷尺", "海苔", "红豆", "黑板", "热水袋", "烛台", "钟表", "路灯", "沙拉", "海报", "公交卡", "樱桃", "创可贴", "牌坊", "苍蝇拍", "高压锅", "电线", "网球拍", "海鸥", "风铃", "订书机", "冰箱", "话梅", "排风机", "锅铲", "绿豆", "航母", "电子秤", "红枣", "金字塔", "鞭炮", "菠萝", "开瓶器", "电饭煲", "仪表盘", "棉棒", "篮球", "狮子", "蚂蚁", "蜡烛", "茶盅", "印章", "茶几", "啤酒", "档案袋", "挂钟", "刺绣", "铃铛", "护腕", "手掌印", "锦旗", "文具盒", "辣椒酱", "耳塞", "中国结", "蜥蜴", "剪纸", "漏斗", "锣", "蒸笼", "珊瑚", "雨靴", "薯条", "蜜蜂", "日历", "口哨"};
    LoginActivity activity;

    public ClassifyModel(AssetManager assetManager, LoginActivity activity){
        tf = new TensorFlowInferenceInterface(assetManager,MODEL_PATH);
        this.activity = activity;
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

    public void predict(final int title, final Bitmap bitmap, final int index){


        //Runs inference in background thread
        new AsyncTask<Integer,Integer,Integer>(){

            @Override

            protected Integer doInBackground(Integer ...params){

                //Resize the image into 224 x 224
                Bitmap resized_image = ImageUtil.processBitmap(bitmap, INPUT_SIZE[1], INPUT_SIZE[0]);

                //Normalize the pixels
                floatValues = ImageUtil.normalizeBitmap(resized_image,INPUT_SIZE[1], INPUT_SIZE[0],127.5f,1.0f);

                //Pass input into the tensorflow
                tf.feed(INPUT_NAME,floatValues,1,INPUT_SIZE[0], INPUT_SIZE[1],INPUT_SIZE[2]);

                //compute predictions
                tf.run(new String[]{OUTPUT_NAME});

                //copy the output into the PREDICTIONS array
                tf.fetch(OUTPUT_NAME,PREDICTIONS);

                //Obtained highest prediction
                Object[] results = argmax(PREDICTIONS);


                final int class_index = (Integer) results[0];
                float confidence = (Float) results[1];


                try{

                    final String conf = String.valueOf(confidence * 100).substring(0,5);

                    //Convert predicted class index into actual label name
//                    final String label = ImageUtil.getLabel(inputStream,class_index);
                    final String label = verify_titles[class_index];
                    Log.d("结果：", label);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (class_index==title) {
                                activity.changeImageViewAlpha(activity.viewIds[index]);
                            }
                        }
                    });
                }

                catch (Exception e){


                }
                return 0;
            }



        }.execute(0);

    }
}
