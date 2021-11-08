package com.android.linkedphotoShSonya.accounthelper;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.android.linkedphotoShSonya.MainActivity;
import com.android.linkedphotoShSonya.Observer;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.databinding.SignAppLayoutBinding;
import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.db.User;
import com.android.linkedphotoShSonya.dialog.SignDialog;
import com.android.linkedphotoShSonya.utils.ImagePickerForSignUp;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import static com.android.linkedphotoShSonya.MainActivity.GOOGLE_SIGN_IN_CODE;

public class AccountHelper implements Observer {
    String name;
    private FirebaseAuth mAuth;
    private MainActivity activity;
    private AlertDialog dialog;
    private GoogleSignInClient signInClient;
    private String temp_email;
    private String temp_password;
    private SignAppLayoutBinding binding;
    boolean createNewUser;
    DbManager dbManager;
    private String currentEmail;
    private StorageReference mStorageRef;
    private Uri uploadUri;
    private int index=0;


    public AccountHelper(String name) {
        this.name = name;
    }

    public AccountHelper(FirebaseAuth mAuth, MainActivity activity, DbManager dbManager) {
        this.mAuth = mAuth;
        this.activity = activity;
        googleAccountManager();
        this.dbManager = dbManager;
    }

    //Sign Up by email
    public boolean signUp(String email, String password, String name, Bitmap bitmap) {
        if (!email.equals("") && !password.equals("")) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if (mAuth.getCurrentUser() != null) {
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    // FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    //DatabaseReference myRef = database.getReference(DbManager.USERS);
                                    //  myRef.push().child("hhh").setValue("pl");
                                    //UploadImage();
                                    //createUser(name, email, bitmap);
                                    UploadImage(name,email,bitmap);
                                    sendEmailVerifocation(user);
                                    Log.d("MyLog ", "Create user " + name);
                                }
                                activity.updateUI();

                            } else {
                                // If sign in fails, display a message to the user.
                                FirebaseAuthUserCollisionException exception = (FirebaseAuthUserCollisionException) task.getException();
                                if (exception == null) {
                                    return;
                                }
                                if (exception.getErrorCode().equals("ERROR_EMAIL_ALREADY_IN_USE")) {
                                    linkEmailAndPassword(email, password);
                                }

                            }
                        }
                    });
            return true;
        }
        return false;
    }

    public void SignIn(String email, String password) {
        if (!email.equals("") && !password.equals("")) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                activity.updateUI();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("MyLogMain", "signInWithEmail:failure", task.getException());
                                Toast.makeText(activity, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        } else {
            Toast.makeText(activity, "Email или Password пустой!", Toast.LENGTH_SHORT).show();
        }
    }

    public void SignOut() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }
        if (mAuth.getCurrentUser().isAnonymous()) {
            return;
        }
        mAuth.signOut();
        signInClient.signOut();
        activity.updateUI();
    }

    private void sendEmailVerifocation(FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showDialog(R.string.alert, R.string.email_verified_send);
                }
            }
        });
    }

    //Sign In by Google
    private void googleAccountManager() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                requestIdToken(activity.getString(R.string.default_web_client_id)).requestEmail().build();
        signInClient = GoogleSignIn.getClient(activity, gso);

    }

    public void SignInGoogle(int code) {
        index=code;
        Intent signInIntent = signInClient.getSignInIntent();
        activity.getSignInLauncher().launch(signInIntent);


    }

    public void SignInFireBaseGoogle(String idToken, GoogleSignInAccount account) {
        GoogleAuthCredential credential = (GoogleAuthCredential) GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (index == 1) linkEmailAndPassword(temp_email, temp_password);
                Log.d("MyLog", "SignInFireBaseG : " + index);
                currentEmail = account.getEmail();
                dbManager.loadAllUsers(AccountHelper.this);
                Toast.makeText(activity, "Log in Done", Toast.LENGTH_SHORT).show();
                //activity.updateUI();
            } else {
                Log.d("MyLog", "SignInFireBaseG 2: " + index);
            }
        });
    }
    //Dialog

    public void showDialog(int title, int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create();
        builder.show();
    }

    public void showDialogWithLink(int title, int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SignInGoogle(1);
            }
        });
        builder.create();
        builder.show();
    }

    public void showDialogNotVarificate(int title, int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SignOut();
            }
        });
        builder.setNegativeButton(R.string.send_email_again, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mAuth.getCurrentUser() != null) {
                    sendEmailVerifocation(mAuth.getCurrentUser());
                }
            }
        });
        builder.create();
        builder.show();
    }

    private void linkEmailAndPassword(String email, String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(activity, R.string.account_linked, Toast.LENGTH_SHORT).show();
                                Log.d("MyLog", "linkWithCredential:success");
                                if (task.getResult() == null) {
                                    return;
                                }
                                activity.updateUI();
                            } else {
                                Toast.makeText(activity, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        } else {
            Log.d("MyLog", " Please Sign In in your google account");
            temp_password = password;
            temp_email = email;
            showDialogWithLink(R.string.alert, R.string.sign_link_message);
        }
    }

    public void signInAnonimous() {
        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    activity.updateUI();
                } else {
                    //сюда написать что ошибка
                }
            }
        });
    }

    private void createUser(String name, String email) {
        User user = new User();
        String key = FirebaseDatabase.getInstance().getReference().push().getKey();
        user.setKey(key);
        user.setId(FirebaseAuth.getInstance().getUid());
        user.setName(name);
        user.setEmail(email);
        user.setImageId(uploadUri.toString());
        if (key != null) {
            dbManager.addUser(user);
        }
    }

    private void UploadImage(String name, String email, Bitmap bitmap) {
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
                createUser(name,email);
            }
        });
    }

    public void createUserWithGoogle(String temp_email) {
        dbManager.loadAllUsers(this);
    }

    @Override
    public void handleEvent(List<User> users) {
        boolean isUserExists = false;
        for (User user : users) {
            if (user.getEmail().equals(currentEmail)) {
                isUserExists = true;
                break;
            }
        }
        Log.d("MyLog", users.toString());
        if (!isUserExists) {
            AlertDialog.Builder dialogBulder = new AlertDialog.Builder(activity);
            binding = SignAppLayoutBinding.inflate(activity.getLayoutInflater());
            dialogBulder.setView(binding.getRoot());
            binding.tvAlerTitle.setText(R.string.choose_login);
            binding.bForgetPassword.setVisibility(View.GONE);
            binding.imageId.setVisibility(View.VISIBLE);
            binding.edName.setVisibility(View.VISIBLE);
            binding.edEmail.setText(mAuth.getCurrentUser().getEmail());
            binding.edPassword.setVisibility(View.GONE);
            binding.bSignGoogle.setVisibility(View.GONE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            /// binding.edEmail.setText(temp_email);
            binding.tvAlerTitle.setText(R.string.choose_login);
            binding.buttonSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bitmap bitmap = ((BitmapDrawable) binding.imageId.getDrawable()).getBitmap();
                    UploadImage(binding.edName.getText().toString(), binding.edEmail.getText().toString(), bitmap);
                    ///createUser(binding.edName.getText().toString(), binding.edEmail.getText().toString(), bitmap);
                    activity.updateUI();
                    dialog.dismiss();
                }
            });
            binding.imageId.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getImage();
                }
            });
            dialog = dialogBulder.create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().
                        setBackgroundDrawableResource(android.R.color.transparent);
            }

            dialog.show();
        } else {
            activity.updateUI();
        }
//        createUserWithGoogle();
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
        }, (MainActivity) activity);
        // activity.getFragmentManager().popBackStack();

    }

    private void selectedimage(Uri uri) throws IOException {
        if (!uri.equals("empty")) {
            Picasso.get().load(Uri.parse(uri.toString())).into(binding.imageId);
            // Bitmap bm = Picasso.get().load(Uri.parse(String.valueOf(uri))).get();
        }

    }

    private void closePixFragmentInSign() {
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).closePixFragment();
        }
        dialog.show();
    }//  DialogAfterChoosePhoto();
}