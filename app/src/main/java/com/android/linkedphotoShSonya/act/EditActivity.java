package com.android.linkedphotoShSonya.act;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.linkedphotoShSonya.Adapter.ImageAdapter;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.Status.FilterManager;
import com.android.linkedphotoShSonya.Status.StatusItem;
import com.android.linkedphotoShSonya.databinding.EditLayoutBinding;
import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.db.NewPost;
import com.android.linkedphotoShSonya.screens.ChooseImageActiviry;
import com.android.linkedphotoShSonya.utils.CountryManager;
import com.android.linkedphotoShSonya.utils.DialogHelper;
import com.android.linkedphotoShSonya.utils.ImagesManager;
import com.android.linkedphotoShSonya.utils.MyConstants;
import com.android.linkedphotoShSonya.utils.OnBitMapLoaded;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditActivity extends AppCompatActivity implements OnBitMapLoaded {
    private EditLayoutBinding rootElement; //через rootElement  есть доступ ко всем элементамэкрана
    private ActivityResultLauncher<Intent> chooseImageLauncher;
    private StorageReference mstorageRef;
    private String[] uploadUri = {"empty", "empty", "empty"};
    private String[] uploadNewUri = new String[3];
    private boolean edit_state = false;
    private String temp_cat = "";
    private String temp_uid = "";
    private String temp_time = "";
    private String temp_key = "";
    private String temp_total_views = "";
    private boolean image_update = false;
    private ProgressDialog pd;
    private int load_image_counter = 0;
    private List<String> imagesUris;
    private ImageAdapter imageAdapter;
    private List<Bitmap> bitMapArrayList;
    private ImagesManager imagesManager;
    private boolean isImagesLoaded = false;
    private MainAppClass mainAppClass;
    private NewPost post;
    private String UserName;
    private String UserPhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootElement = EditLayoutBinding.inflate(getLayoutInflater());
        setContentView(rootElement.getRoot());
        init();
        onResultLauncher();
        //getMyIntent();
    }

    private void init() {
        mainAppClass = (MainAppClass) getApplicationContext();
        imagesManager = new ImagesManager(this, this);
        imagesUris = new ArrayList<>();
        bitMapArrayList = new ArrayList<>();
        imageAdapter = new ImageAdapter(this);
        pd = new ProgressDialog(this);
        rootElement.viewPager.setAdapter(imageAdapter);
        pd.setMessage("Идет загрузка...");
        mstorageRef = mainAppClass.getFs().getReference("Images");
        onChangePageListener();
        getMyIntent();// до 125 урока у меня этого не было увидела у него
    }

    private void onResultLauncher() {
        chooseImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            onActivityResult(result.getData());
        });

    }

    private void onActivityResult(Intent data) {//
        image_update = true;
        imagesUris.clear();
        String[] tempUriArray = getUrisForChoose(data);
        isImagesLoaded = false;
        imagesManager.resizeMultiLargeImages(Arrays.asList(tempUriArray), this);
        for (String s : tempUriArray) {
            if (!s.equals("empty")) {
                imagesUris.add(s);
            }
        }
        imageAdapter.updateImages(imagesUris);
        viewPagerimageCounter();

        //rootElement.tvImagedCounter.setVisibility(View.VISIBLE);// до 126 уркоа у меня была эта строка


    }

    private void onChangePageListener() {
        rootElement.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                String dataText = position + 1 + "/" + imagesUris.size();
                rootElement.tvImagedCounter.setVisibility(View.VISIBLE);
                rootElement.tvImagedCounter.setText(dataText);
            }

            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void getMyIntent() {
        if (getIntent() != null) {
            Intent i = getIntent();
            UserName = i.getStringExtra("userName");
            UserPhoto = i.getStringExtra("userPhoto");

            edit_state = i.getBooleanExtra(MyConstants.EDIT_STATE, false);
            if (edit_state) {
                setDataAds(i);
            }
        }
    }

    private void setDataAds(Intent i) {
        // PicassoPicassoV.get().load(i.getStringExtra(MyConstants.IMAGE_ID)).into(imItem);
        NewPost newPost = (NewPost) i.getSerializableExtra(MyConstants.New_POST_INTENT);
        if (newPost == null) {
            return;
        }
        rootElement.editDesc.setText(newPost.getDisc());
        rootElement.tvSelectCountry.setText(newPost.getCountry());
        rootElement.tvSelectCIty.setText(newPost.getCity());
        temp_cat = newPost.getCat();
        temp_uid = newPost.getUid();
        temp_time = newPost.getTime();
        temp_key = newPost.getKey();
        temp_total_views = newPost.getTotal_views();

        fillImageArray(newPost);

        viewPagerimageCounter();
        rootElement.tvImagedCounter.setVisibility(View.VISIBLE);

    }

    private void fillImageArray(NewPost newPost) {//заполняю массив фотографиями
        isImagesLoaded = true;
        uploadUri[0] = newPost.getImageId();
        uploadUri[1] = newPost.getImageId2();
        uploadUri[2] = newPost.getImageId3();
        for (String s : uploadUri) {
            if (!s.equals("empty")) {
                imagesUris.add(s);
            }
        }
        imageAdapter.updateImages(imagesUris);
    }

    private void viewPagerimageCounter() {
        String dataText = (imagesUris.size() > 0) ? 1 + "/" + imagesUris.size() : 0 + "/" + imagesUris.size();
        rootElement.tvImagedCounter.setText(dataText);
    }

    private void uploadImage() {
        if (imagesUris.size() == load_image_counter) {
            publishPost();
            finish();
            return;
        }
        Bitmap bitmap = bitMapArrayList.get(load_image_counter);
        sendImageToStorage(getBytesFromBitMap(bitmap));
    }

    private void sendImageToStorage(byte[] byteArray) {// загрузка массива из байтов в firebase storage
        final StorageReference mRef = mstorageRef.child(System.currentTimeMillis() + "_image");
        UploadTask up = mRef.putBytes(byteArray); //загркжаем картинку на указанный путь(путь строкой выше)
        up.continueWithTask(task1 -> mRef.getDownloadUrl()).addOnCompleteListener(task12 -> {
            if (task12.getResult() == null) return;// когда ничего не пришло
            uploadUri[load_image_counter] = task12.getResult().toString();
            nexImageToSend();
        }).addOnFailureListener(e -> {
        });
    }

    private byte[] getBytesFromBitMap(Bitmap bitmap) { //берем битмап и  получаем массссив байтов

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assert bitmap != null;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, out);
        return out.toByteArray();
    }

    private void nexImageToSend() {
        load_image_counter++;
        uploadImage();
    }

    private String[] getUrisForChoose(Intent data) {
        if (edit_state) {
            uploadNewUri[0] = data.getStringExtra("uriMain");
            uploadNewUri[1] = data.getStringExtra("uri2");
            uploadNewUri[2] = data.getStringExtra("uri3");
            return uploadNewUri;
        } else {
            uploadUri[0] = data.getStringExtra("uriMain");
            uploadUri[1] = data.getStringExtra("uri2");
            uploadUri[2] = data.getStringExtra("uri3");
            return uploadUri;
        }
    }

    public void onClickImage(View view) {
        Intent intent = new Intent(EditActivity.this, ChooseImageActiviry.class);
        intent.putExtra(MyConstants.New_POST_INTENT, uploadUri[0]);
        intent.putExtra(MyConstants.IMAGE_ID2, uploadUri[1]);
        intent.putExtra(MyConstants.IMAGE_ID3, uploadUri[2]);
        chooseImageLauncher.launch(intent);

    }

    private void getImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 10);
    }

    private void savePost(NewPost post) {
        String key = mainAppClass.getMainDbRef().push().getKey();
        String key2 = mainAppClass.getUserDbRef().push().getKey();
        post.setKey(key);
        post.setTime(String.valueOf(System.currentTimeMillis()));
        post.setUid(mainAppClass.getAuth().getUid());
        post.setCat("notes");
        post.setTotal_views("0");
        post.setName(UserName);
        post.setLogoUser(UserPhoto);
        if (key != null) {
            mainAppClass.getMainDbRef().child(key).child(mainAppClass.getAuth().getUid()).child("post").setValue(post);
            mainAppClass.getMainDbRef().child(key).child(DbManager.STATUS + "/" + DbManager.STATUS).setValue(new StatusItem());
            mainAppClass.getMainDbRef().child(key).child(DbManager.STATUS + "/" + DbManager.FILTER1).setValue(FilterManager.fillFilter_1_2(post, true)).addOnCompleteListener(
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mainAppClass.getMainDbRef().child(key).child(DbManager.STATUS + "/" + DbManager.FILTER2).setValue(FilterManager.fillFilter_1_2(post, false));
                        }
                    }
            );

        }


        // mainAppClass.getUserDbRef().push().child("son").setValue("llll");
    }

    public void onClickSavePost(View view) {
        if (!isFieldEmpty()) {
            Toast.makeText(this, R.string.empty_field_error, Toast.LENGTH_LONG).show();
            return;
        }
        if (isImagesLoaded) {
            pd.show();
            if (!edit_state) {
                uploadImage();

            } else {
                if (image_update) {
                    uploadUpdateImage();
                } else {
                    publishPost();
                }
            }
        } else {
            Toast.makeText(this, R.string.images_loading, Toast.LENGTH_SHORT).show();
            if (!edit_state) {
                uploadImage();

            } else {
                if (image_update) {
                    uploadUpdateImage();
                } else {
                    publishPost();
                }
            }
        }
    }

    private boolean isFieldEmpty() {//делаем что бы если пустое поле - то ошибка

        String country = rootElement.tvSelectCountry.getText().toString();
        String city = rootElement.tvSelectCIty.getText().toString();
        String disc = rootElement.editDesc.getText().toString();
        return (!getString(R.string.select_country).equals(country) && !getString(R.string.select_city).equals(city) && !TextUtils.isEmpty(disc));
    }

    private void publishPost() {
        NewPost post = new NewPost();
        post.setImageId(uploadUri[0]);
        post.setImageId2(uploadUri[1]);
        post.setImageId3(uploadUri[2]);

        post.setDisc(rootElement.editDesc.getText().toString());
        post.setCountry(rootElement.tvSelectCountry.getText().toString());
        post.setCity(rootElement.tvSelectCIty.getText().toString());
        if (edit_state) {
            updatePost(post);
        } else {
            savePost(post);
        }

    }


    private void updatePost(NewPost post) {
        this.post = post;
        if (mainAppClass.getAuth().getUid() == null) {
            return;
        }
        post.setKey(temp_key);
        post.setTime(temp_time);
        post.setUid(temp_uid);
        post.setCat(temp_cat);
        post.setTotal_views(temp_total_views);
        StatusItem statusItem = new StatusItem();
        statusItem.totalViews = post.getTotal_views();
        mainAppClass.getMainDbRef().child(temp_key).child(DbManager.STATUS + "/" + DbManager.STATUS).setValue(statusItem);
        mainAppClass.getMainDbRef().child(temp_key).child(mainAppClass.getAuth().getUid()).child("post").setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mainAppClass.getMainDbRef().child(temp_key).child(DbManager.STATUS + "/" + DbManager.FILTER1).setValue(FilterManager.fillFilter_1_2(post, true)).addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mainAppClass.getMainDbRef().child(temp_key).child(DbManager.STATUS + "/" + DbManager.FILTER2).setValue(FilterManager.fillFilter_1_2(post, false));
                            }
                        }
                );
                Toast.makeText(EditActivity.this, "Upload  done!! ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void uploadUpdateImage() {
        if (load_image_counter < uploadUri.length) {
            compareUpdateImageArrays();
        } else {
            publishPost();

        }
    }

    private void uploadNextUpdateImage() {
        load_image_counter++;
        uploadUpdateImage();
    }

    private void updateImageOnFireBase(byte[] byteArray) {//то как мы записываем bytearray на fireBase
        StorageReference mRef = getUpdateRef();
        UploadTask up = mRef.putBytes(byteArray);
        up.continueWithTask(task1 -> mRef.getDownloadUrl()).addOnCompleteListener(task12 -> {
            uploadUri[load_image_counter] = task12.getResult().toString();
            uploadNextUpdateImage();

        }).addOnFailureListener(e -> {
        });
    }

    private StorageReference getUpdateRef() {// возвращает место куда записываем ИЛИ перезаписываем картинку

        if (!uploadUri[load_image_counter].equals("empty")) { // если старая не empty, то презаписывает в страрую ссылку в новую
            return FirebaseStorage.getInstance().getReferenceFromUrl(uploadUri[load_image_counter]);
        } else {// тут наоброт если empty - созадаем новую картинку в storage
            return mstorageRef.child(System.currentTimeMillis() + "_image");
        }
    }

    private void deleteUpdateImage() {
        StorageReference mRef = FirebaseStorage.getInstance().getReferenceFromUrl(uploadUri[load_image_counter]);
        mRef.delete().addOnCompleteListener(task -> {
            uploadUri[load_image_counter] = "empty";

            uploadNextUpdateImage();
        });
    }
    private void compareUpdateImageArrays(){
        Bitmap bitmap = null;
        // первое условие :если ссылка на старой позиции равна ссылке на новой (ничего не изменилось)
        if (uploadUri[load_image_counter].equals(uploadNewUri[load_image_counter])) {
            uploadNextUpdateImage();
        }
        // второе условие :если ссылка на старой позиции НЕ равна ссылке на новой и  Не ПКСТОТА новая (изменилось)
        else if (!uploadUri[load_image_counter].equals(uploadNewUri[load_image_counter]) && !uploadNewUri[load_image_counter].equals("empty")) {
            bitmap = bitMapArrayList.get(load_image_counter);

        }
        // удалить старую ссылку и картинку
        else if (!uploadUri[load_image_counter].equals("empty") && uploadNewUri[load_image_counter].equals("empty")) {
            deleteUpdateImage();
        }
        if (bitmap == null) {
            return;
        }
        updateImageOnFireBase(getBytesFromBitMap(bitmap));
    }

    @Override
    protected void onPause() {
        super.onPause();
        pd.dismiss();
    }

    @Override
    public void onBitmapLoadedd(List<Bitmap> bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bitMapArrayList.clear();
                bitMapArrayList.addAll(bitmap);
                isImagesLoaded = true;
            }
        });

    }

    public void onClickSetCountry(View view) {
        String city = rootElement.tvSelectCountry.getText().toString();
        if (!city.equals(getString(R.string.select_city))) {
            rootElement.tvSelectCIty.setText(getString(R.string.select_city));
        }
        DialogHelper.INSTANCE.showDialog(this, CountryManager.INSTANCE.getAllCountries(this), (TextView) view);
    }

    public void onClickSetCity(View view) {
        String country = rootElement.tvSelectCountry.getText().toString();
        if (!country.equals(getString(R.string.select_country))) {
            DialogHelper.INSTANCE.showDialog(this, CountryManager.INSTANCE.getAllCites(this, country), (TextView) view);
        } else {
            Toast.makeText(this, R.string.country_notSelected, Toast.LENGTH_SHORT).show();
        }
    }

    public EditLayoutBinding getRootElement() {
        return rootElement;
    }
}

