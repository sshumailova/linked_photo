package com.android.linkedphotoShSonya.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.utils.ImagesManager;
import com.android.linkedphotoShSonya.utils.OnBitMapLoaded;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends PagerAdapter implements OnBitMapLoaded {
    private LayoutInflater inflater;
    private Activity context;
    private List<Bitmap> bmList;
    private OnBitMapLoaded onBitMapLoaded;
    private ImagesManager imagesManager;



    public ImageAdapter(Activity context) {
        this.context = context;
        imagesManager = new ImagesManager(context, this);
        inflater = LayoutInflater.from(context);
        bmList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return bmList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.pager_item, container, false);
        ImageView imItem = view.findViewById(R.id.imageViewPager);
        imItem.setImageBitmap(bmList.get(position));
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }

    public void updateImages(List<String> images) {

        imagesManager.resizeMultiLargeImages(images,context);
       // Log.d("MyLog", "Update" + images.get(0));
    }
    @Override
    public void onBitmapLoadedd(final List<Bitmap> bitmap) {
        Log.d("MyLog","OnBitmapLoadded"+bitmap.size());
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bmList.clear();
                bmList.addAll(bitmap);
                notifyDataSetChanged();
            }
        });

    }

}
