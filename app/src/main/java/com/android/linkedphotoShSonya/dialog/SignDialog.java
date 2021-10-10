package com.android.linkedphotoShSonya.dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;


import com.android.linkedphotoShSonya.MainActivity;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.accounthelper.AccountHelper;
import com.android.linkedphotoShSonya.act.MainAppClass;
import com.android.linkedphotoShSonya.databinding.SignAppLayoutBinding;

import com.android.linkedphotoShSonya.db.User;
import com.android.linkedphotoShSonya.screens.ChooseImageActiviry;
import com.android.linkedphotoShSonya.utils.ImagePicker;
import com.android.linkedphotoShSonya.utils.ImagePickerForSignUp;
import com.android.linkedphotoShSonya.utils.ImagesManager;
import com.android.linkedphotoShSonya.utils.OnBitMapLoaded;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignDialog  {
    private FirebaseAuth auth;
    private Activity activity;
    private AccountHelper accountHelper;
    private SignAppLayoutBinding binding;//делаем снова bindin что б разгррузить код
    private AlertDialog dialog;
    private ArrayList<User> userArrayList;
    private MainAppClass mainAppClass;
    private String login;
    private String email;
    private String password;
    private boolean isIamgeLoaded;
    private ImagesManager imagesManager;
    private OnBitMapLoaded onBitMapLoaded;
    private String wayToLgo;
    private boolean isImagesLoaded = false;
    String[] uris = new String[1];
    private final int MAX_SIZE = 1020;
    private int width;
    private int height;
    private Uri uri;
    private StorageReference mStorageRef;
    private Uri uploadUri;

    List<Bitmap> bmList;

    Bitmap bm;

    public SignDialog(FirebaseAuth auth, Activity activity, AccountHelper accountHelper) {
        this.auth = auth;
        this.activity = activity;
        this.accountHelper = accountHelper;
        // this.imagesManager=imagesManager;
    }

    public void showSignDialog(int title, int buttonTitle, int index) {
        AlertDialog.Builder dialogBulder = new AlertDialog.Builder((MainActivity) activity);
        binding = SignAppLayoutBinding.inflate(activity.getLayoutInflater());
        dialogBulder.setView(binding.getRoot());
        binding.tvAlerTitle.setText(title);
        showForgetButton(index);
//       OnBitMapLoaded();
//       imagesManager = new ImagesManager(this, onBitMapLoaded);
        binding.buttonSignUp.setText(buttonTitle);
        binding.imageId.setOnClickListener(onClickOnImage());
        binding.buttonSignUp.setOnClickListener(onClickSignWithEmail(index));
        binding.bSignGoogle.setOnClickListener(onClickSignWithGoogle(index));
        binding.bForgetPassword.setOnClickListener(onClickForgetButton());
        dialog = dialogBulder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().
                    setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

//FF
    private View.OnClickListener onClickOnImage() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login = binding.edName.getText().toString();
                email = binding.edEmail.getText().toString();
                password = binding.edPassword.getText().toString();
                getImage();

            }


        };
    }
    private void showForgetButton(int index) {
        if (index == 0) {
            binding.bForgetPassword.setVisibility(View.GONE);
            binding.imageId.setVisibility(View.VISIBLE);
            binding.edName.setVisibility(View.VISIBLE);
        } else {
            binding.bForgetPassword.setVisibility(View.VISIBLE);
            binding.imageId.setVisibility(View.GONE);
            binding.edName.setVisibility(View.GONE);

        }
    }
    private void getImage() {
        dialog.dismiss();
        ImagePickerForSignUp.INSTANCE.getImage((ImagePickerForSignUp.Listener) uri -> {
            Log.d("MyLog", "ChooseFoeSign : " + uri);
            try {
                selectedimage(uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            closePixFragmentInSign();
        },(MainActivity) activity);
       // activity.getFragmentManager().popBackStack();

    }

    private void selectedimage(Uri uri) throws IOException {
        if (!uri.equals("empty")) {
         Picasso.get().load(Uri.parse(uri.toString())).into(binding.imageId);
           // Bitmap bm = Picasso.get().load(Uri.parse(String.valueOf(uri))).get();
        }

    }

    private void closePixFragmentInSign() {
        if( activity instanceof MainActivity)
        {((MainActivity) activity).closePixFragment();        }
        dialog.show();
    }//  DialogAfterChoosePhoto();



    private View.OnClickListener onClickSignWithEmail(final int index) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auth.getCurrentUser() != null) {
                    if (auth.getCurrentUser().isAnonymous()) {
                        auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    if (index == 0) {
                                        Log.d("MyLog", "For Index " + index);
                                       // UploadImage();
                                        Bitmap bitmap = ((BitmapDrawable) binding.imageId.getDrawable()).getBitmap();
                                        boolean result = accountHelper.signUp(binding.edEmail.getText().toString(), binding.edPassword.getText().toString(), binding.edName.getText().toString(),bitmap);
                                        if (!result) {
                                            Toast.makeText(activity, "Email или Password пустой!", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        accountHelper.SignIn(binding.edEmail.getText().toString(), binding.edPassword.getText().toString());
                                    }
                                }
                            }
                        });
                    }
                }
                dialog.dismiss();

            }
        };
    }
//    private void  UploadImage() {
//        mStorageRef= FirebaseStorage.getInstance().getReference("ImagesUserLogo");
//        Bitmap bitmap = ((BitmapDrawable) binding.imageId.getDrawable()).getBitmap();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        byte[] byteArray = baos.toByteArray();
//        StorageReference mRef=mStorageRef.child(System.currentTimeMillis()+ "my_Image");
//        UploadTask up=mRef.putBytes(byteArray);
//        Task<Uri> task=up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//            @Override
//            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                return mRef.getDownloadUrl();
//            }
//        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//            @Override
//            public void onComplete(@NonNull Task<Uri> task) {
//                uploadUri=task.getResult();
//            }
//        });
//    }

    private View.OnClickListener onClickSignWithGoogle(final int index) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auth.getCurrentUser() != null) {
                    if (auth.getCurrentUser().isAnonymous()) {
                        auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    accountHelper.SignInGoogle(AccountHelper.GOOGLE_SIGN_IN_CODE);
                                }
                            }
                        });
                    }
                }
                dialog.dismiss();
            }
        };
    }

    private View.OnClickListener onClickForgetButton() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.edPassword.isShown()) {
                    binding.edPassword.setVisibility(View.GONE);
                    binding.buttonSignUp.setVisibility(View.GONE);
                    binding.bSignGoogle.setVisibility(View.GONE);
                    binding.tvAlerTitle.setText(R.string.forget_password);
                    binding.bForgetPassword.setText(R.string.send_recet_password);
                    binding.tvMessage.setVisibility(View.VISIBLE);
                    binding.tvMessage.setText(R.string.forget_password_message);
                    // dialog.dismiss();
                } else {
                    if (!binding.edEmail.getText().toString().equals("")) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(binding.edEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(activity, R.string.email_is_send, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(activity, "Mistake", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(activity, R.string.email_is_empty, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }


}
