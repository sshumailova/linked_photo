package com.android.linkedphotoShSonya.screens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.databinding.ActivityChooseImageActiviryBinding;
import com.android.linkedphotoShSonya.utils.ImagePicker;
import com.android.linkedphotoShSonya.utils.ImagesManager;
import com.android.linkedphotoShSonya.utils.MyConstants;
import com.android.linkedphotoShSonya.utils.OnBitMapLoaded;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseImageActiviry extends AppCompatActivity {
    private ActivityChooseImageActiviryBinding binding;
    private String uriMain = "empty", uri2 = "empty", uri3 = "empty";
    private ImageView imMain, im2, im3;
    private ImageView[] imagesViews = new ImageView[3];
    String[] uris = new String[3];
    private ImagesManager imagesManager;
    private OnBitMapLoaded onBitMapLoaded;
    private boolean isImagesLoaded = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChooseImageActiviryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        imMain = findViewById(R.id.mainImage);
        im2 = findViewById(R.id.image2);
        im3 = findViewById(R.id.image3);
        uris[0] = "empty";
        uris[1] = "empty";
        uris[2] = "empty";
        imagesViews[0] = imMain;
        imagesViews[1] = im2;
        imagesViews[2] = im3;
        OnBitMapLoaded();
        imagesManager = new ImagesManager(this, onBitMapLoaded);
        getMyIntent();

    }


    private void OnBitMapLoaded() {
        onBitMapLoaded = new OnBitMapLoaded() {
            @Override
            public void onBitmapLoadedd(List<Bitmap> bitmap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < bitmap.size(); i++) {
                            if (bitmap.get(i) != null)
                                imagesViews[i].setImageBitmap(bitmap.get(i));

                        }
                        isImagesLoaded = true;
                    }
                });
            }
        };
    }

    public void MainImage(View view) {
        if (!isImagesLoaded) {
            Toast.makeText(this, R.string.images_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        getImage(0);
    }

    public void onClickImage2(View view) {
        if (!isImagesLoaded) {
            Toast.makeText(this, R.string.images_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        getImage(1);
    }

    public void onClickImage3(View view) {
        if (!isImagesLoaded) {
            Toast.makeText(this, R.string.images_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        getImage(2);
    }


    private void getImage(int index) {
        ImagePicker.INSTANCE.getImage(uri -> {
            Log.d("MyLog", "Image selected : " + uri);
            selectedImage(index, uri);
            closePixFragment();
        }, this);
    }

    private void selectedImage(int index, Uri uri) {
        uris[index] = uri.toString();
        isImagesLoaded = false;
        imagesManager.resizeMultiLargeImages(Arrays.asList(uris), this);

    }

    private void closePixFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            if (f.isVisible()) getSupportFragmentManager().beginTransaction().remove(f).commit();
        }
    }

    public void onClickBack(View view) {
        Intent i = new Intent();
        i.putExtra("uriMain", uris[0]);
        i.putExtra("uri2", uris[1]);
        i.putExtra("uri3", uris[2]);

        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClickDeleteMainImage(View view) {
        imMain.setImageResource(android.R.drawable.ic_input_add);
        uris[0] = "empty";
    }

    public void onClickDeleteMainImage2(View view) {
        im2.setImageResource(android.R.drawable.ic_input_add);
        uris[1] = "empty";
    }

    public void onClickDeleteMainImage3(View view) {
        im3.setImageResource(android.R.drawable.ic_input_add);
        uris[2] = "empty";
    }

    private void getMyIntent() {
        Intent i = getIntent();
        if (i != null) {
            uris[0] = i.getStringExtra(MyConstants.New_POST_INTENT);
            uris[1] = i.getStringExtra(MyConstants.IMAGE_ID2);
            uris[2] = i.getStringExtra(MyConstants.IMAGE_ID3);
            isImagesLoaded = false;
            imagesManager.resizeMultiLargeImages(sortImages(uris), this);
        }
    }

    private List<String> sortImages(String[] uris) {
        List<String> tempList = new ArrayList<>();
        for (int i = 0; i < uris.length; i++) {
            if (uris[i].startsWith("http")) {
                showHttpImages(uris[i], i);
                tempList.add("empty");
            } else {
                tempList.add(uris[i]);
            }
        }
        return tempList;
    }

    private void showHttpImages(String uri, int position) {
        Picasso.get().load(uri).into(imagesViews[position]);

    }

    @Override
    public void onBackPressed() {// нажатие на кнопку назад
        onClickBack(null);

    }
}