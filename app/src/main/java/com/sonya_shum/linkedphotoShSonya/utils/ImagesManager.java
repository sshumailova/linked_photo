package com.sonya_shum.linkedphotoShSonya.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

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

    public int[] getImageSize(String uri, Activity act) {
        int[] size = new int[2];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            InputStream is = act.getContentResolver().openInputStream(Uri.parse(uri));
            BitmapFactory.decodeStream(is, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        size[0] = options.outWidth;
        size[1] = options.outHeight;
        return size;
    }

    public void resizeMultiLargeImages(final List<String> uries, Activity act) {
        List<int[]> sizeList = new ArrayList<>();
        List<int[]> realSizeList = new ArrayList<>();

        for (int i = 0; i < uries.size(); i++) {
            int[] sizes = getImageSize(uries.get(i), act);
            width = sizes[0];
            height = sizes[1];
            realSizeList.add(new int[]{width, height});
            Log.d("MyLog", "Origin Image Size " + width);
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
            sizeList.add(new int[]{width, height});

        }
        new Thread(() -> {
            try {
                bmList.clear();
                for (int i = 0; i < sizeList.size(); i++) {
                    if (!uries.get(i).equals("empty") && !uries.get(i).startsWith("http") && realSizeList.get(i)[0] > MAX_SIZE ||
                            realSizeList.get(i)[1] > MAX_SIZE) {
                        Bitmap bm = Picasso.get().load(Uri.parse(uries.get(i))).resize(sizeList.get(i)[0], sizeList.get(i)[1]).get();
                        bmList.add(bm);
                    } else if (uries.get(i).startsWith("http")) {
                        Bitmap bm = Picasso.get().load(uries.get(i)).get();
                        bmList.add(bm);
                    } else if (!uries.get(i).equals("empty") &&
                            !uries.get(i).startsWith("http") && realSizeList.get(i)[0] < MAX_SIZE && realSizeList.get(i)[1] < MAX_SIZE) {
                        Bitmap bm = Picasso.get().load(Uri.parse(uries.get(i))).get();
                        bmList.add(bm);
                    } else {
                        bmList.add(null);
                    }
                }

                onBitMapLoaded.onBitmapLoadedd(bmList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}