package com.wcy.client12306.util;

import android.graphics.Bitmap;

public class ImageUtil {

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
