package com.android.linkedphotoShSonya.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.android.linkedphotoShSonya.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends PagerAdapter {
    private LayoutInflater inflater;
    private Context context;
    private List<String> imagesUries;

    public ImageAdapter(Context context) {
        this.context = context;
        inflater=LayoutInflater.from(context);
        imagesUries= new ArrayList<>();
    }

    @Override
    public int getCount() {
        return imagesUries.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view=inflater.inflate(R.layout.pager_item,container,false);
        ImageView imItem=view.findViewById(R.id.imageViewPager);
        String uri=imagesUries.get(position);
        if(uri.substring(0,4).equals("http")){
            Picasso.get().load(uri).into(imItem);
        }
        else {
            imItem.setImageURI(Uri.parse(imagesUries.get(position)));
        }

        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @Override
    public int getItemPosition(@NonNull  Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(@NonNull  ViewGroup container, int position, @NonNull  Object object) {
       container.removeView((LinearLayout)object);
    }
    public void updateImages(List<String> images){
        imagesUries.clear();
        imagesUries.addAll(images);
        notifyDataSetChanged();
    }
}
