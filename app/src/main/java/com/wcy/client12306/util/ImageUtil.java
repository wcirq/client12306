package com.wcy.client12306.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class ImageUtil {
    static boolean useNativeConversion = false;
    private static Bitmap bitmap = null;
    private static int[] rgbBytes = null;
    public static boolean isRun = true;
    private static Bitmap newBM;

    static {
        System.loadLibrary("image-lib");
    }

    public static Bitmap convertYUV420SPToARGB4444(byte[] yuv420sp, int width, int height) {
        if (useNativeConversion) {
            isRun = false;
            if (rgbBytes==null||rgbBytes.length!=width*height){
                rgbBytes = new int[width*height];
            }
            try {
                long start = System.currentTimeMillis();
                ImageUtil.convertYUV420SPToARGB8888(yuv420sp, rgbBytes, width, height, false);
                long end = System.currentTimeMillis();
                Log.d("time0", String.valueOf(end-start));
                if (bitmap==null){
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
                }
                bitmap.setPixels(rgbBytes, 0, width, 0, 0, width, height);
                isRun = true;
                end = System.currentTimeMillis();
                Log.d("time1", String.valueOf(end-start));
                return bitmap;
            } catch (UnsatisfiedLinkError e) {
                Log.w("", "Native YUV420SP -> RGB implementation not found, falling back to Java implementation");
                useNativeConversion = false;
            }
        }else {
            long start = System.currentTimeMillis();
            byte[] argb = new byte[width*height*4];
            for(int i=0;i<width*height;i++){
                int offset = width*height;
                int y = ((int) yuv420sp[i]&0xFF)-16;
                int u = ((int) yuv420sp[offset+i/4*2]&0xFF)-128;
                int v = ((int) yuv420sp[offset+i/4*2+1]&0xFF)-128;
                int r = (int) (y+1.4075*v);
                int g = (int) (y-0.3455*u-0.7169*v);
                int b = (int) (y+1.779*u);
                byte R = (byte) (Math.min(Math.max(r, 0), 255));
                byte G = (byte) (Math.min(Math.max(g, 0), 255));
                byte B = (byte) (Math.min(Math.max(b, 0), 255));

                argb[i*4] = R;
                argb[i*4+1] = G;
                argb[i*4+2] = B;
                argb[i*4+3] = (byte) (255*1.0);
            }
            if (bitmap==null){
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            }
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argb));
            long end = System.currentTimeMillis();
            Log.d("time11", String.valueOf(end-start));
            return bitmap;
        }
        return null;
    }

    public static native void convertYUV420SPToARGB8888(byte[] input, int[] output, int width, int height, boolean halfSize);

    /**
     * 根据给定的宽和高进行拉伸
     *
     * @param origin    原图
     * @param newWidth  新图的宽
     * @param newHeight 新图的高
     * @return new Bitmap
     */
    public static Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }

    /**
     * 按比例缩放图片
     *
     * @param origin 原图
     * @param ratio  比例
     * @return 新的bitmap
     */
    public static Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    /**
     * 裁剪
     *
     * @param bitmap 原图
     * @return 裁剪后的图像
     */
    public static Bitmap cropBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();
        int cropWidth = w >= h ? h : w;// 裁切后所取的正方形区域边长
        cropWidth /= 2;
        int cropHeight = (int) (cropWidth / 1.2);
        return Bitmap.createBitmap(bitmap, w / 3, 0, cropWidth, cropHeight, null, false);
    }

    /**
     * 选择变换
     *
     * @param origin 原图
     * @param alpha  旋转角度，可正可负
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        if (origin == null) {
            return null;
        }
        newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
//        isRun = true;
        // 围绕原地进行旋转
        if (newBM.equals(origin)) {
            return newBM;
        }
//        origin.recycle();
        return newBM;
    }

    /**
     * 偏移效果
     * @param origin 原图
     * @return 偏移后的bitmap
     */
    public static Bitmap skewBitmap(Bitmap origin) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.postSkew(-0.6f, -0.3f);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    public static Bitmap getAlplaBitmap(Bitmap sourceImg, int number) {

        int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];


        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg.getWidth(), sourceImg.getHeight());

        number = number * 255 / 100;

        for (int i = 0; i < argb.length; i++) {
            argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);
        }
        sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg.getHeight(), Bitmap.Config.ARGB_8888);
        return sourceImg;
    }

    public static Matrix getTransformationMatrix(
            final int srcWidth,
            final int srcHeight,
            final int dstWidth,
            final int dstHeight,
            final int applyRotation,
            final boolean maintainAspectRatio) {
        final Matrix matrix = new Matrix();

        if (applyRotation != 0) {
            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

            // Rotate around origin.
            matrix.postRotate(applyRotation);
        }

        // Account for the already applied rotation, if any, and then determine how
        // much scaling is needed for each axis.
        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;

        final int inWidth = transpose ? srcHeight : srcWidth;
        final int inHeight = transpose ? srcWidth : srcHeight;

        // Apply scaling if necessary.
        if (inWidth != dstWidth || inHeight != dstHeight) {
            final float scaleFactorX = dstWidth / (float) inWidth;
            final float scaleFactorY = dstHeight / (float) inHeight;

            if (maintainAspectRatio) {
                // Scale by minimum factor so that dst is filled completely while
                // maintaining the aspect ratio. Some image may fall off the edge.
                final float scaleFactor = Math.max(scaleFactorX, scaleFactorY);
                matrix.postScale(scaleFactor, scaleFactor);
            } else {
                // Scale exactly to fill dst from src.
                matrix.postScale(scaleFactorX, scaleFactorY);
            }
        }

        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }

        return matrix;
    }


    public static Bitmap processBitmap(Bitmap source,int width, int height){

        int image_height = source.getHeight();
        int image_width = source.getWidth();

        Bitmap croppedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Matrix frameToCropTransformations = getTransformationMatrix(image_width,image_height,width, height,0,false);
        Matrix cropToFrameTransformations = new Matrix();
        frameToCropTransformations.invert(cropToFrameTransformations);

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(source, frameToCropTransformations, null);

        return croppedBitmap;


    }

    public static Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();			//获取位图的宽
        int height = img.getHeight();		//获取位图的高

        int []pixels = new int[width * height];	//通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for(int i = 0; i < height; i++)	{
            for(int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey  & 0x00FF0000 ) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    public static float[] dealImage(Bitmap img) {
        int width = img.getWidth();			//获取位图的宽
        int height = img.getHeight();		//获取位图的高
        int []pixels = new int[width * height];	//通过位图的大小创建像素点数组
        img.getPixels(pixels, 0, width, 0, 0, width, height);
        float []ouput = new float[width * height];	//通过位图的大小创建像素点数组
        int alpha = 0xFF << 24;
        for(int i = 0; i < height; i++)	{
            for(int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey  & 0x00FF0000 ) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);
                ouput[width * i + j] = grey/255.0f;
            }
        }
        return ouput;
    }

    public static float[] normalizeBitmap(Bitmap source,int width, int height,float mean,float std){

        float[] output = new float[width * height * 3];

        int[] intValues = new int[source.getHeight() * source.getWidth()];

        source.getPixels(intValues, 0, source.getWidth(), 0, 0, source.getWidth(), source.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            output[i * 3+2] = ((val >> 16) & 0xFF)-123.68f;
            output[i * 3 + 1] = ((val >> 8) & 0xFF)-116.779f;
            output[i * 3] = (val & 0xFF)-103.939f;
        }

        return output;

    }

    public static Object[] argmax(float[] array){


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


    public static String getLabel(InputStream jsonStream, int index){
        String label = "";
        try {

            byte[] jsonData = new byte[jsonStream.available()];
            jsonStream.read(jsonData);
            jsonStream.close();

            String jsonString = new String(jsonData,"utf-8");

            JSONObject object = new JSONObject(jsonString);

            label = object.getString(String.valueOf(index));



        }
        catch (Exception e){


        }
        return label;
    }
}
