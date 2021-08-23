package com.android.linkedphotoShSonya;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.linkedphotoShSonya.Adapter.ImageAdapter;
import com.android.linkedphotoShSonya.databinding.EditLayoutBinding;
import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.db.NewPost;
import com.android.linkedphotoShSonya.db.StatusItem;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditActivity extends AppCompatActivity implements OnBitMapLoaded {
    private ImageView imItem;
    private EditLayoutBinding rootElement; //через rootElement  есть доступ ко всем элементамэкрана
    private StorageReference mstorageRef;
    private String[] uploadUri = new String[3];
    private String[] uploadNewUri = new String[3];
    private DatabaseReference dRef;
    private FirebaseAuth myAuth;
    private boolean edit_state = false;
    private String temp_cat = "";
    private String temp_uid = "";
    private String temp_time = "";
    private String temp_key = "";
    private String temp_total_views = "";
    private boolean image_update = false;
    private ProgressDialog pd;
    private int load_image_coat = 0;
    private List<String> imagesUris;
    private ImageAdapter imageAdapter;
    private List<Bitmap> bitMapArrayList;
    private final int MAX_UPOLOAD_IMAGE_SIZE = 1920;
    private ImagesManager imagesManager;
    private boolean isImagesLoaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootElement = EditLayoutBinding.inflate(getLayoutInflater());
        setContentView(rootElement.getRoot());
        init();
        getMyIntent();
    }

    private void init() {
        imagesManager = new ImagesManager(this, this);
        imagesUris = new ArrayList<>();
        bitMapArrayList = new ArrayList<>();
        imageAdapter = new ImageAdapter(this);
        rootElement.viewPager.setAdapter(imageAdapter);
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
        uploadUri[0] = "empty";
        uploadUri[1] = "empty";
        uploadUri[2] = "empty";
        pd = new ProgressDialog(this);
        pd.setMessage("Идет загрузка...");
        mstorageRef = FirebaseStorage.getInstance().getReference("Images");

    }

    private void getMyIntent() {
        if (getIntent() != null) {
            Intent i = getIntent();
            edit_state = i.getBooleanExtra(MyConstants.EDIT_STATE, false);
            if (edit_state) {
                setDataAds(i);
            }
        }
    }

    private void setDataAds(Intent i) {
        // Picasso.get().load(i.getStringExtra(MyConstants.IMAGE_ID)).into(imItem);
        NewPost newPost = (NewPost) i.getSerializableExtra(MyConstants.New_POST_INTENT);
        if (newPost == null) {
            return;
        }
        rootElement.editDesc.setText(newPost.getDisc());
        temp_cat = newPost.getCat();
        temp_uid = newPost.getUid();
        temp_time = newPost.getTime();
        temp_key = newPost.getKey();
        temp_total_views = newPost.getTotal_views();
        uploadUri[0] = newPost.getImageId();
        uploadUri[1] = newPost.getImageId2();
        uploadUri[2] = newPost.getImageId3();

        for (String s : uploadUri) {
            if (!s.equals("empty")) {
                imagesUris.add(s);
            }
        }
        isImagesLoaded = true;
        imageAdapter.updateImages(imagesUris);
        String dataText;
        if (imagesUris.size() > 0) {
            dataText = 1 + "/" + imagesUris.size();
        } else {
            dataText = 0 + "/" + imagesUris.size();
        }
        rootElement.tvImagedCounter.setVisibility(View.VISIBLE);
        rootElement.tvImagedCounter.setText(dataText);

    }


    private void uploadImage() {
        if (load_image_coat < uploadUri.length) {
            if (!uploadUri[load_image_coat].equals("empty")) {
                Bitmap bitmap = bitMapArrayList.get(load_image_coat);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                assert bitmap != null;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, out);
                byte[] byteArray = out.toByteArray();
                final StorageReference mRef = mstorageRef.child(System.currentTimeMillis() + "_image");
                UploadTask up = mRef.putBytes(byteArray);
                Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        return mRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.getResult() == null) return;
                        uploadUri[load_image_coat] = task.getResult().toString();
                        load_image_coat++;
                        if (load_image_coat < uploadUri.length) {
                            uploadImage();
                        } else {
                            savePost();
                            Toast.makeText(EditActivity.this, "Upload  done: ", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            } else {
                load_image_coat++;
                uploadImage();
            }
        } else {
            savePost();
            finish();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //отпраяляем наши ссылки
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MyLOg", "Uri 3 " + data);
        if (requestCode == 15 && data != null && data != null) {
            if (resultCode == RESULT_OK) {
                image_update = true;
                imagesUris.clear();
                String[] tempUriArray = getUrisForChoose(data);
                isImagesLoaded = false;
                imagesManager.resizeMultiLargeImages(Arrays.asList(tempUriArray));
                for (String s : tempUriArray) {
                    if (!s.equals("empty")) {
                        imagesUris.add(s);
                    }
                }
                imageAdapter.updateImages(imagesUris);
                String dataText;
                if (imagesUris.size() > 0) {
                    dataText = rootElement.viewPager.getCurrentItem() + 1 + "/" + imagesUris.size();
                } else {
                    dataText = 0 + "/" + imagesUris.size();
                }
                rootElement.tvImagedCounter.setVisibility(View.VISIBLE);
                rootElement.tvImagedCounter.setText(dataText);
            }
        }
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
        startActivityForResult(intent, 15);
    }

    private void getImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 10);
    }

    private void savePost() {
        dRef = FirebaseDatabase.getInstance().getReference(DbManager.MAIN_ADS_PATH);
        myAuth = FirebaseAuth.getInstance();
        if (myAuth.getUid() != null) {
            String key = dRef.push().getKey();
            NewPost post = new NewPost();
            post.setImageId(uploadUri[0]);
            post.setImageId2(uploadUri[1]);
            post.setImageId3(uploadUri[2]);
            post.setDisc(rootElement.editDesc.getText().toString());
            post.setCountry(rootElement.tvSelectCountry.getText().toString());
            post.setCity(rootElement.tvSelectCIty.getText().toString());
            post.setKey(key);
            post.setTime(String.valueOf(System.currentTimeMillis()));
            post.setUid(myAuth.getUid());
            post.setCat("notes");
            post.setTotal_views("0");
            if (key != null) {
                StatusItem stItem = new StatusItem();
                stItem.catTime = post.getCat() + "_" + post.getTime();
                stItem.filter_by_time = post.getTime();
                dRef.child(key).child(myAuth.getUid()).child("post").setValue(post);
                dRef.child(key).child("status").setValue(stItem);
            }
        }
    }

    public void onClickSavePost(View view) {
        if (isImagesLoaded) {
            pd.show();
            if (!edit_state) {
                uploadImage();

            } else {
                if (image_update) {
                    uploadUpdateImage();
                } else {
                    updatePost();
                }
            }
        } else {
            Toast.makeText(this, R.string.images_loading, Toast.LENGTH_SHORT).show();
        }

    }

    private void updatePost() {
        myAuth = FirebaseAuth.getInstance();
        if (myAuth.getUid() != null) {
            dRef = FirebaseDatabase.getInstance().getReference(DbManager.MAIN_ADS_PATH);
            NewPost post = new NewPost();
            post.setImageId(uploadUri[0]);
            post.setImageId2(uploadUri[1]);
            post.setImageId3(uploadUri[2]);

            post.setDisc(rootElement.editDesc.getText().toString());
            post.setCountry(rootElement.tvSelectCountry.getText().toString());
            post.setCity(rootElement.tvSelectCIty.getText().toString());
            post.setDisc(rootElement.editDesc.getText().toString());

            post.setKey(temp_key);
            post.setTime(temp_time);
            post.setUid(temp_uid);
            post.setCat(temp_cat);
            post.setTotal_views(temp_total_views);
            dRef.child(temp_key).child(myAuth.getUid()).child("post").setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(EditActivity.this, "Upload  done!! ", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

    }

    private void uploadUpdateImage() {
        Bitmap bitmap = null;
        if (load_image_coat < uploadUri.length) {
            // первое условие :если ссылка на старой позиции равна ссылке на новой (ничего не изменилось)
            if (uploadUri[load_image_coat].equals(uploadNewUri[load_image_coat])) {
                load_image_coat++;
                uploadUpdateImage();
            }
// второе условие :если ссылка на старой позиции НЕ равна ссылке на новой и  Не ПКСТОТА новая (изменилось)
            else if (!uploadUri[load_image_coat].equals(uploadNewUri[load_image_coat]) && !uploadNewUri[load_image_coat].equals("empty")) {

                bitmap = bitMapArrayList.get(load_image_coat);

            }
            // удалить старую ссылку и картинку
            else if (!uploadUri[load_image_coat].equals("empty") && uploadNewUri[load_image_coat].equals("empty")) {
                StorageReference mRef = FirebaseStorage.getInstance().getReferenceFromUrl(uploadUri[load_image_coat]);
                mRef.delete();
                mRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        uploadUri[load_image_coat] = "empty";
                        load_image_coat++;
                        if (load_image_coat < uploadUri.length) {
                            uploadUpdateImage();
                        } else {
                            updatePost();
                        }
                    }
                });
            }
            if (bitmap == null) {
                return;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            byte[] byteArray = out.toByteArray();
            final StorageReference mRef;
            if (!uploadUri[load_image_coat].equals("empty")) { // если старая не empty, то презаписывает в страрую ссылку в новую
                mRef = FirebaseStorage.getInstance().getReferenceFromUrl(uploadUri[load_image_coat]);
            } else {// тут наоброт если empty - созадаем новую картинку в storage
                mRef = mstorageRef.child(System.currentTimeMillis() + "_image");
            }

            UploadTask up = mRef.putBytes(byteArray);
            Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return mRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    uploadUri[load_image_coat] = task.getResult().toString();
//                    assert uploadUri != null;
//                    uploadUri[0] = uploadUri.toString();
                    load_image_coat++;
                    if (load_image_coat < uploadUri.length) {
                        uploadUpdateImage();
                    } else {
                        updatePost();
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        } else {
            updatePost();

        }
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
        if (city.equals(getString(R.string.select_city))) {
            DialogHelper.INSTANCE.showDialog(this, CountryManager.INSTANCE.getAllCountries(this), (TextView) view);
        } else {
            rootElement.tvSelectCIty.setText(getString(R.string.select_city));
            DialogHelper.INSTANCE.showDialog(this, CountryManager.INSTANCE.getAllCountries(this), (TextView) view);
        }
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

