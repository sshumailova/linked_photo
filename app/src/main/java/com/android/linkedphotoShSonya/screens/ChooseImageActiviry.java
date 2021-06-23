package com.android.linkedphotoShSonya.screens;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.linkedphotoShSonya.EditActivity;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.utils.ImagesManager;
import com.android.linkedphotoShSonya.utils.MyConstants;
import com.android.linkedphotoShSonya.utils.OnBitMapLoaded;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseImageActiviry extends AppCompatActivity {
    private String uriMain = "empty", uri2 = "empty", uri3 = "empty";
    private ImageView imMain, im2, im3;
    private ImageView[] imagesViews = new ImageView[3];
    String[] uris = new String[3];
    private ImagesManager imagesManager;
    private final int MAX_IMAGE_SIZE = 2000;
    private OnBitMapLoaded onBitMapLoaded;
    private boolean isImagesLoaded=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image_activiry);
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

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null ) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            if(returnValue==null){return;}
            switch (requestCode) {
                case 1:
                    uris[0] =returnValue.get(0);
                    isImagesLoaded=false;
                    imagesManager.resizeMultiLargeImages(Arrays.asList(uris));
                    //код проверяет большая картнка или нет - можно так же сдлеать(НУЖНО) на две другие

                    break;
                case 2:
                    uris[1] = returnValue.get(0);
                    isImagesLoaded=false;
                    imagesManager.resizeMultiLargeImages(Arrays.asList(uris));
                    break;
                case 3:
                    uris[2] = returnValue.get(0);
                    isImagesLoaded=false;
                    imagesManager.resizeMultiLargeImages(Arrays.asList(uris));
                    break;
            }
        }
    }

    private void OnBitMapLoaded() {
        onBitMapLoaded = new OnBitMapLoaded() {
            @Override
            public void onBitmapLoadedd(List<Bitmap> bitmap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(int i=0;i<bitmap.size();i++){
                       if(bitmap.get(i)!=null)
                           imagesViews[i].setImageBitmap(bitmap.get(i));

                    }
                        isImagesLoaded=true;
                    }
                });
            }
        };
    }

    public void MainImage(View view) {
        if(!isImagesLoaded){
            Toast.makeText(this, R.string.images_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        getImage(1);
    }

    public void onClickImage2(View view) {
        if(!isImagesLoaded){
            Toast.makeText(this,  R.string.images_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        getImage(2);
    }

    public void onClickImage3(View view) {
        if(!isImagesLoaded){
            Toast.makeText(this,  R.string.images_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        getImage(3);
    }


    private void getImage(int index) {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//        startActivityForResult(intent, index);
        Options options = Options.init()
                .setRequestCode(index)                                          //Request code for activity results
                .setCount(1)                                                   //Number of images to restict selection count
                .setFrontfacing(false)
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)    ; //Orientaion//Custom Path For media Storage

        Pix.start(ChooseImageActiviry.this, options);
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
//        Intent i=new Intent();
//        i.putExtra("uriMain",uriMain);
//        i.putExtra("uri2",uri2);
//        i.putExtra("uri3",uri3);
//
//        setResult(RESULT_OK,i);
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
            uris[0] = i.getStringExtra(MyConstants.IMAGE_ID);
            uris[1] = i.getStringExtra(MyConstants.IMAGE_ID2);
            uris[2] = i.getStringExtra(MyConstants.IMAGE_ID3);
            isImagesLoaded=false;
           imagesManager.resizeMultiLargeImages(sortImages(uris));
        }
    }

    private  List<String> sortImages(String[] uris) {
        List<String> tempList =new ArrayList<>();
        for (int i = 0; i < uris.length; i++) {
            if (uris[i].startsWith("http")) {
                showHttpImages(uris[i],i);
                tempList.add("empty");
            }
else {
    tempList.add(uris[i]);
            }
        }
return  tempList;
    }

    private void showHttpImages(String uri, int position) {
            Picasso.get().load(uri).into(imagesViews[position]);

}}