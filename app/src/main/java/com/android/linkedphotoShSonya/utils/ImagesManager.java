package com.android.linkedphotoShSonya.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImagesManager {
    private Context context;
    private final int MAX_SIZE = 1020;
    private int width;
    private int height;
    private OnBitMapLoaded onBitMapLoaded;
    List<Bitmap> bmList;

    public ImagesManager(Context context, OnBitMapLoaded onBitMapLoaded) {
        this.context = context;
        this.onBitMapLoaded = onBitMapLoaded;
        bmList = new ArrayList<>();
    }

    public static Bitmap resizeImage(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float imageRatio = (float) width / (float) height;
        if (imageRatio > 1) {
            if (width > maxSize) {
                width = maxSize;
                height = (int) (width / imageRatio);
            }
        } else {
            if (height > maxSize) {
                height = maxSize;
                width = (int) (height * imageRatio);
            }
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public int[] getImageSize(String uri) {
        int[] size = new int[2];
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(uri));
            BitmapFactory.decodeStream(inputStream, null, options);
            size[1] = options.outHeight;
            size[0] = options.outWidth;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return size;
    }
    public void resizeMultiLargeImages(final List<String> uries) {
        List<int[]> sizeList = new ArrayList<>();
        for (int i = 0; i < uries.size(); i++) {
            width = getImageSize(uries.get(i))[0];
            height = getImageSize(uries.get(i))[1];
            float imageRatio = (float) width / (float) height;
            if (imageRatio > 1) {
                if (width > MAX_SIZE) {
                    width = MAX_SIZE;
                    height = (int) (width / imageRatio);
                }
            } else {
                if (height > MAX_SIZE) {
                    height = MAX_SIZE;
                    width = (int) (height * imageRatio);
                }
            }
            sizeList.add(new int[]{width,height});
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bmList.clear();
                    for(int i=0;i<sizeList.size();i++){
                        if(!uries.get(i).equals("empty")){
                    Bitmap bm = Picasso.get().load(uries.get(i)).resize(sizeList.get(i)[0], sizeList.get(i)[1]).get();
                    bmList.add(bm);}
                        else {
                            bmList.add(null);
                        }}
                    onBitMapLoaded.onBitmapLoadedd(bmList);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

}