package com.android.linkedphotoShSonya;

import static com.android.linkedphotoShSonya.db.DbManager.MAIN_ADS_PATH;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.linkedphotoShSonya.Adapter.DataSender;
import com.android.linkedphotoShSonya.act.MainAppClass;
import com.android.linkedphotoShSonya.act.PersonListActiviti;
import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.db.NewPost;
import com.android.linkedphotoShSonya.db.User;
import com.android.linkedphotoShSonya.utils.CircleTransform;
import com.android.linkedphotoShSonya.utils.ImagePickerForChangePhotoUser;
import com.android.linkedphotoShSonya.utils.ImagePickerForSignUp;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class userActivity extends AppCompatActivity implements Observer, DataSender {
    private String uid;
    private String UserName;
    private String UserPhoto;
    private DbManager dbManager;
    private EditText login;
    private EditText text;
    private ImageView logo;
    private Button saveButton;
    private String nameBeforeChange = "";
    private String discBeforeChange = "";
    private String photoBeforeChange = "";
    private boolean clickPhoto;
    private DatabaseReference users;
    private String newUri;
    private boolean isChoosePhoto = false;
    private StorageReference mStorageRef;
    private Uri uploadUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        init();
    }

    private void init() {
        if (getIntent() != null) {
            Intent i = getIntent();
            uid = i.getStringExtra("Uid");
        }

        users = FirebaseDatabase.getInstance().getReference(DbManager.USERS).child(uid);
        clickPhoto = false;
        login = findViewById(R.id.login);
        text = findViewById(R.id.description);
        logo = findViewById(R.id.imageLogo);
        saveButton = findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(onClickItem());
        logo.setOnClickListener(onClickItem());
        dbManager = new DbManager(this);
        dbManager.getCurrentUser(uid);
        dbManager.loadAllUsers(this);

    }

    private View.OnClickListener onClickItem() {
        return view -> {
            if (view.getId() == R.id.
                    buttonSave) {
                AlertDialog.Builder builder = new AlertDialog.Builder(userActivity.this);
                builder.setMessage("изменения");
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        backOnMainActivity();
                    }
                });
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isSomethingChange();

                    }
                });
                builder.show();
            }
            if (view.getId() == R.id.imageLogo) {
                clickPhoto = true;
                isChoosePhoto = true;
                getImage();
            }

        };
    }

    private void getImage() {
        ImagePickerForChangePhotoUser.INSTANCE.getImage((ImagePickerForChangePhotoUser.Listener) uri -> {
            Log.d("MyLog", "ChooseFoeSign : " + uri);
            try {

                selectedimage(uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            closePixFragmentInSign();
        }, (userActivity) this);

    }

    private void selectedimage(Uri uri) throws IOException {
        if (!uri.equals("empty")) {
            Picasso.get().load(Uri.parse(uri.toString())).transform(new CircleTransform()).into(logo);
            // Bitmap bm = Picasso.get().load(Uri.parse(String.valueOf(uri))).get();
        }

    }

    @Override
    public void onBackPressed() {
        Log.d("MyLog", "BACK ");
        // нажатие на кнопку назад
        onClickBack(null);
    }

    public void onClickBack(View view) {
        if (isChoosePhoto == true) {
            closePixFragmentInSign();
            isChoosePhoto = false;
        } else {
            finish();
        }

    }

    private void closePixFragmentInSign() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            if (f.isVisible()) getSupportFragmentManager().beginTransaction().remove(f).commit();
        }

    }
    //  DialogAfterChoosePhoto();

    private void isSomethingChange() {
        if (!login.getText().equals(nameBeforeChange)) {
            users.child("user").child("name").setValue(login.getText().toString());
        }
        if (!text.getText().equals(discBeforeChange)) {
            users.child("user").child("description").setValue(text.getText().toString());
        }
        if ((clickPhoto == true)) {
            DatabaseReference dRef = FirebaseDatabase.getInstance().getReference(MAIN_ADS_PATH);
            deletePreviousPhoto();
            Bitmap bitmap = ((BitmapDrawable) logo.getDrawable()).getBitmap();
            saveNewPhoto(bitmap);
           // dRef.child("-Mpqk4ARbFzOVGMSwH31").child(uid).child("post").child("name").removeValue();
           // dRef.child("-Mpqk4ARbFzOVGMSwH31").child(uid).child("post").push().child("name").setValue("zzzzzzzzz");
           // dbManager.getMyAds(dbManager.getMyAdsNode());
        }
       // backOnMainActivity();
    }

    private void deletePreviousPhoto() {
        Log.d("MyLog","PhotoBefore "+photoBeforeChange);
        StorageReference mRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoBeforeChange);
        mRef.delete().addOnCompleteListener(task -> {
                Log.d("MyLog", "onSuccess: deleted file");
            });
        }


    private void saveNewPhoto(Bitmap bitmap) {
        mStorageRef = FirebaseStorage.getInstance().getReference("ImagesUserLogo");
        //bitmap = ((BitmapDrawable) binding.imageId.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] byteArray = baos.toByteArray();
        StorageReference mRef = mStorageRef.child(System.currentTimeMillis() + "my_Image");
        UploadTask up = mRef.putBytes(byteArray);
        Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                uploadUri = task.getResult();
                Log.d("MyLog"," savePhoto "+ uploadUri.toString());
                users.child("user").child("imageId").setValue(uploadUri.toString());
                dbManager.getMyAds(dbManager.getMyAdsNode());
            }
        });
    }

    private void backOnMainActivity() {
        Intent intent = new Intent(userActivity.this, MainActivity.class);
        startActivity(intent);
    }


    @Override
    public void handleEvent(List<User> users) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            login.setText(user.getName());
            text.setText(user.getDescription());
            nameBeforeChange = user.getName();
            discBeforeChange = user.getDescription();
            photoBeforeChange = user.getImageId();
            Log.d("MyLog", "Photo" +photoBeforeChange);
            Picasso.get().load(user.getImageId()).transform(new CircleTransform()).into(logo);
        }
    }

    @Override
    public void onDataRecived(List<NewPost> listData) {
        for (int i = 0; i < listData.size(); i++) {
            Log.d("MyLog", "List" + listData.size());
            NewPost n = listData.get(i);
            changeDataOnDb(n, listData.size());

        }
        backOnMainActivity();
    }

    public void changeDataOnDb(NewPost newPost, int listDataSize) {
        Log.d("MyLog", "ListDa" + listDataSize);
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference(MAIN_ADS_PATH);
        String key = newPost.getKey();
       dRef.child(key).child(uid).child("post").child("name").setValue(login.getText().toString());
//        .addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
                Log.d("MyLog", "key" + key);
                dRef.child(key).child(uid).child("post").child("logoUser").setValue(uploadUri.toString());
//                        .addOnCompleteListener(task1 -> {
//                    if (task1.isSuccessful()) {
                        Log.d("MyLog", "way l" + dRef.child(key).child(uid).child("post").child("name"));
                    }
//                });
//            }

//        });
//    }
}