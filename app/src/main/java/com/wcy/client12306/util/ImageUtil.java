package com.wcy.client12306.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;

public class ImageUtil {
    static boolean useNativeConversion = true;
    private static Bitmap bitmap = null;
    private static int[] rgbBytes = null;
    public static boolean isRun = true;
    private static Bitmap newBM;

    static {
        System.loadLibrary("image-lib");
    }

    public static Bitmap convertYUV420SPToARGB4444(byte[] input, int width, int height) {
        if (useNativeConversion) {
            isRun = false;
            if (rgbBytes==null||rgbBytes.length!=width*height){
                rgbBytes = new int[width*height];
            }
            try {
                long start = System.currentTimeMillis();
                ImageUtil.convertYUV420SPToARGB8888(input, rgbBytes, width, height, false);
                if (bitmap==null){
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
                }
                bitmap.setPixels(rgbBytes, 0, width, 0, 0, width, height);
                isRun = true;
                long end = System.currentTimeMillis();
                Log.d("time", String.valueOf(end-start));
                return bitmap;
            } catch (UnsatisfiedLinkError e) {
                Log.w("", "Native YUV420SP -> RGB implementation not found, falling back to Java implementation");
                useNativeConversion = false;
            }
        }else {
            long start = System.currentTimeMillis();
            byte[] byteHeat1 = new byte[width*height*4];
            for(int i=0;i<width*height;i++){
                int offset = width*height;
                int y = ((int) input[i]&0xFF)-16;
                int u = ((int) input[offset+i/4*2]&0xFF)-128;
                int v = ((int) input[offset+i/4*2+1]&0xFF)-128;
                int r = (int) (y+1.14*v);
                int g = (int) (y-0.39*u-0.58*v);
                int b = (int) (y+2.03*u);
                byte R = (byte) (Math.min(Math.max(r, 0), 255));
                byte G = (byte) (Math.min(Math.max(g, 0), 255));
                byte B = (byte) (Math.min(Math.max(b, 0), 255));

                byteHeat1[i*4] = R;
                byteHeat1[i*4+1] = G;
                byteHeat1[i*4+2] = B;
                byteHeat1[i*4+3] = (byte) (255*1.0);
            }
            if (bitmap==null){
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
            }
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(byteHeat1));
            long end = System.currentTimeMillis();
            Log.d("time", String.valueOf(end-start));
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
}
