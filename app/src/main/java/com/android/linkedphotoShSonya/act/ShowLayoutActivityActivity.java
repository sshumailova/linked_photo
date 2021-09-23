package com.android.linkedphotoShSonya.act;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.linkedphotoShSonya.Adapter.ImageAdapter;
import com.android.linkedphotoShSonya.Adapter.PostAdapter;
import com.android.linkedphotoShSonya.MainActivity;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.db.NewPost;
import com.android.linkedphotoShSonya.utils.MyConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShowLayoutActivityActivity extends AppCompatActivity {
    private TextView tvDisc, tvTotalViews, tvTotalLike;
    private ImageButton tvLike;
    private ImageView imMAin;
    private List<String> imagesUris;
    private ImageAdapter imageAdapter;
    private TextView tvImagesCounter;
    private int like;
    private NewPost newPost;
    private DbManager dbManager;
    private FirebaseAuth mAuth;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_layout_activity);
        init();
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        dbManager = new DbManager( this);
        FirebaseUser currentUser = mAuth.getCurrentUser();
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
        tvLike = findViewById(R.id.imFav);
        tvTotalLike = findViewById(R.id.tvQuantityLike);
        // imMAin=findViewById(R.id.imMain);
        if (getIntent() != null) {
            Intent i = getIntent();
            newPost = (NewPost) i.getSerializableExtra(MyConstants.New_POST_INTENT);
            if (newPost == null) {
                return;
            }
            tvDisc.setText(newPost.getDisc());
            tvTotalViews.setText(newPost.getTotal_views());
            if(newPost.isFav() || Objects.requireNonNull(currentUser).isAnonymous()  ){
               tvLike.setImageResource(R.drawable.ic_fav_selected);
                tvTotalLike.setText(String.valueOf(newPost.getFavCounter()));
            }
            else {
                tvLike.setImageResource(R.drawable.ic_fav_not_selected);
                tvTotalLike.setText(String.valueOf(newPost.getFavCounter()));
            }
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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isAnonymous()){
                return;
            }}
                updateFav(newPost);
        setFavCounter(newPost,tvTotalLike);
        }
    public void updateFav(final NewPost newPost) {

        if (newPost.isFav()) {
            deleteFav(newPost);
        } else {
            addFav(newPost);
        }
    }
    public void addFav(NewPost newPost) {
        if (mAuth.getUid() == null) {
            return;
        }
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference(DbManager.MAIN_ADS_PATH);
        dRef.child(newPost.getKey()).child(DbManager.FAv_ADS_PATh).child(mAuth.getUid()).child(DbManager.USER_FAV_ID).
                setValue(mAuth.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {
                   tvLike.setImageResource(R.drawable.ic_fav_selected);
                    tvTotalLike.setText(String.valueOf(newPost.getFavCounter()));
                    newPost.setFav(true);

                }
            }
        });
    }

    public void deleteFav(NewPost newPost) {
        if (mAuth.getUid() == null) {
            return;
        }
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference(DbManager.MAIN_ADS_PATH);
        dRef.child(newPost.getKey()).child(DbManager.FAv_ADS_PATh).child(mAuth.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {
                    tvLike.setImageResource(R.drawable.ic_fav_not_selected);
                    tvTotalLike.setText(String.valueOf(newPost.getFavCounter()));
                    newPost.setFav(false);
                }
            }
        });
    }
    public static void setFavCounter(NewPost newPost, TextView tvTotalLike){
        int fCounter=Integer.parseInt(tvTotalLike.getText().toString());
        fCounter=(newPost.isFav()) ? --fCounter : ++fCounter; //если это израное - отнять 1 т.к становится не избранным, а
        // если это не избранное- приавить 1 т.к становатся избранным
        tvTotalLike.setText(String.valueOf(fCounter));
        newPost.setFavCounter((long)fCounter);
    }
}

