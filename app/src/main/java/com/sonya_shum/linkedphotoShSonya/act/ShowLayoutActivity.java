package com.sonya_shum.linkedphotoShSonya.act;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sonya_shum.linkedphotoShSonya.Adapter.ImageAdapter;
import com.sonya_shum.linkedphotoShSonya.R;
import com.sonya_shum.linkedphotoShSonya.dagger.App;
import com.sonya_shum.linkedphotoShSonya.databinding.ShowLayoutActivityBinding;
import com.sonya_shum.linkedphotoShSonya.db.DbManager;
import com.sonya_shum.linkedphotoShSonya.db.NewPost;
import com.sonya_shum.linkedphotoShSonya.utils.CircleTransform;
import com.sonya_shum.linkedphotoShSonya.utils.MyConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

public class ShowLayoutActivity extends AppCompatActivity {
    private ShowLayoutActivityBinding binding;
    private List<String> imagesUris;
    private ImageAdapter imageAdapter;
    private int like;
    private NewPost newPost;
    private DbManager dbManager;
    private FirebaseAuth mAuth;
    private Context context;
   @Inject
    MainAppClass mainAppClass;
   @Inject
    @Named("mainDb")
    DatabaseReference databaseReferenceMain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App)getApplication()).getComponent().inject(this);
        binding = ShowLayoutActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
//        binding.ibScaleImage.setOnClickListener(view -> {
//            Intent intent=new Intent(ShowLayoutActivity.this,ScaleImageActivity.class);
//            intent.putExtra("IMAGE_URI", imagesUris.get(binding.viewPager.getCurrentItem()));
//            startActivity(intent);
//        });
    }

    private void init() {
        //mAuth = FirebaseAuth.getInstance();
        dbManager = new DbManager(this);
        FirebaseUser currentUser = mainAppClass.getCurrentUser();
         //dRef = mainAppClass.getMainDbRef();
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
                binding.tvImagedCounter2.setText(dataText);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // imMAin=findViewById(R.id.imMain);
        if (getIntent() != null) {
            Intent i = getIntent();
            newPost = (NewPost) i.getSerializableExtra(MyConstants.New_POST_INTENT);
            if (newPost == null) {
                return;
            }
            binding.tvDiscShow.setText(newPost.getDisc());
            binding.tvViews.setText(newPost.getTotal_views());
            binding.tvName.setText(newPost.getName());
            Picasso.get().load(newPost.getLogoUser()).transform(new CircleTransform()).into(binding.UserPhoto);
            binding.tvCountryDisc.setText(newPost.getCountry());
            binding.tvCityDisk.setText(newPost.getCity());
            String publishTime=getData(newPost.getTime());
            binding.tvDate.setText(publishTime);
            if (newPost.isFav() || Objects.requireNonNull(currentUser).isAnonymous()) {
                binding.imFav.setImageResource(R.drawable.ic_fav_selected);
                binding.tvQuantityLike.setText(String.valueOf(newPost.getFavCounter()));
            } else {
                binding.imFav.setImageResource(R.drawable.ic_fav_not_selected);
                binding.tvQuantityLike.setText(String.valueOf(newPost.getFavCounter()));
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
            binding.tvImagedCounter2.setText(dataText);
            // Picasso.get().load(i.getStringExtra(MyConstants.IMAGE_ID)).into(imMAin);

        }
    }

    public String getData(String timeMillis) {
        SimpleDateFormat formater = new SimpleDateFormat(" hh:mm dd/MM/yyyy", Locale.getDefault());
        Calendar c=Calendar.getInstance();
        c.setTimeInMillis(Long.parseLong(timeMillis));
      return formater.format(c.getTime());

    }

    public void Like(View view) {
        FirebaseUser currentUser = mainAppClass.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isAnonymous()) {
                return;
            }
        }
        updateFav(newPost);
        setFavCounter(newPost, binding.tvQuantityLike);
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
//        DatabaseReference dRef = mainAppClass.getMainDbRef();
        databaseReferenceMain.child(newPost.getKey()).child(DbManager.FAv_ADS_PATh).child(mAuth.getUid()).child(DbManager.USER_FAV_ID).
                setValue(mAuth.getUid()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                binding.imFav.setImageResource(R.drawable.ic_fav_selected);
                binding.tvQuantityLike.setText(String.valueOf(newPost.getFavCounter()));
                newPost.setFav(true);

            }
        });
    }

    public void deleteFav(NewPost newPost) {
        if (mAuth.getUid() == null) {
            return;
        }
//        DatabaseReference dRef = mainAppClass.getMainDbRef();;
        databaseReferenceMain.child(newPost.getKey()).child(DbManager.FAv_ADS_PATh).child(mAuth.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {
                    binding.imFav.setImageResource(R.drawable.ic_fav_not_selected);
                    binding.tvQuantityLike.setText(String.valueOf(newPost.getFavCounter()));
                    newPost.setFav(false);
                }
            }
        });
    }

    public static void setFavCounter(NewPost newPost, TextView tvTotalLike) {
        int fCounter = Integer.parseInt(tvTotalLike.getText().toString());
        fCounter = (newPost.isFav()) ? --fCounter : ++fCounter; //???????? ?????? ?????????????? - ???????????? 1 ??.?? ???????????????????? ???? ??????????????????, ??
        // ???????? ?????? ???? ??????????????????- ???????????????? 1 ??.?? ???????????????????? ??????????????????
        tvTotalLike.setText(String.valueOf(fCounter));
        newPost.setFavCounter((long) fCounter);
    }

    public void ClickScale(View view) {
        Intent intent = new Intent(ShowLayoutActivity.this, ScaleImageActivity.class);
        intent.putExtra("IMAGE_URI", imagesUris.get(binding.viewPager.getCurrentItem()));
        startActivity(intent);
    }
}

