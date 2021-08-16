package com.android.linkedphotoShSonya;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.linkedphotoShSonya.Adapter.ImageAdapter;
import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.db.NewPost;
import com.android.linkedphotoShSonya.utils.MyConstants;

import java.util.ArrayList;
import java.util.List;

public class ShowLayoutActivityActivity extends AppCompatActivity {
    private TextView tvDisc, tvTotalViews, tvTotalLike;
    private ImageView imMAin;
    private List<String> imagesUris;
    private ImageAdapter imageAdapter;
    private TextView tvImagesCounter;
    private int like;
    private NewPost newPost;
    private DbManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_layout_activity);
        init();
    }

    private void init() {
        dbManager=new DbManager(null,this);
        imagesUris = new ArrayList<>();
        // tvImagesCounter=findViewById(R.id.tvImagedCounter2);
        ViewPager vp = findViewById(R.id.view_pager);
        imageAdapter = new ImageAdapter(this);
        vp.setAdapter(imageAdapter);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                String dataText = position + 1 + "/" + imagesUris.size();
                tvImagesCounter.setText(dataText);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tvImagesCounter = findViewById(R.id.tvImagedCounter2);
        tvDisc = findViewById(R.id.tvMain);
        tvTotalViews = findViewById(R.id.tvViews);
        tvTotalLike=findViewById(R.id.tvQuantityLike);
        // imMAin=findViewById(R.id.imMain);
        if (getIntent() != null) {
            Intent i = getIntent();
            NewPost newPost = (NewPost) i.getSerializableExtra(MyConstants.New_POST_INTENT);
            if (newPost == null) {
                return;
            }
            tvDisc.setText(newPost.getDisc());
            tvTotalViews.setText(newPost.getTotal_views());
            //like=newPost.getLike(); КОЛИЧЕСТВО ЛАЙКОВ
            //tvTotalLike.setText(newPost.getLike());
            String[] images = new String[3];
            images[0] = newPost.getImageId();
            images[1] = newPost.getImageId2();
            images[2] = newPost.getImageId3();
            for (String s : images) {
                if (!s.equals("empty")) {
                    imagesUris.add(s);
                }
            }

            imageAdapter.updateImages(imagesUris);

            String dataText;
            if (imagesUris.size() > 0) {
                dataText = 1 + "/" + imagesUris.size();
            } else {
                dataText = 0 + "/" + imagesUris.size();
            }
            tvImagesCounter.setText(dataText);
            // Picasso.get().load(i.getStringExtra(MyConstants.IMAGE_ID)).into(imMAin);

        }
    }

    public void Like(View view) {
        //  dbManager.updateTotalLike(newPost);
       // like++;
       // tvTotalLike.setText(like);
    }
}