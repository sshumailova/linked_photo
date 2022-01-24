package com.sonya_shum.linkedphotoShSonya.act;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.sonya_shum.linkedphotoShSonya.R;
import com.sonya_shum.linkedphotoShSonya.databinding.ActivityScaleImageBinding;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class ScaleImageActivity extends AppCompatActivity {
    private ActivityScaleImageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScaleImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getImageIntent();
    }

    private void getImageIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String uri = intent.getStringExtra("IMAGE_URI");
            new Thread(() -> {//создаем еще один поток- ВТОРОСТИПЕНННЫЙ ПОТОК
                try {
                    Bitmap bt = Picasso.get().load(uri).get();
                    runOnUiThread(() -> {
                        binding.imageView.setImage(ImageSource.bitmap(bt));
                    });// внутри ВТОРОСТЕПЕННОГО ПОТОКА ЗАПУСКАЕМ ОСНОВНОЙ поток
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}