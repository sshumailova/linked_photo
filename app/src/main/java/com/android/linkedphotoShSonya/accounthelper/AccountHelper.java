package com.android.linkedphotoShSonya.accounthelper;

import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.android.linkedphotoShSonya.MainActivity;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.Status.StatusManager;
import com.android.linkedphotoShSonya.act.MainAppClass;
import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.db.User;
import com.android.linkedphotoShSonya.dialog.SignDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

//import static com.android.linkedphotoShSonya.MainActivity.GOOGLE_SIGN_IN_CODE;

public class AccountHelper {
    private FirebaseAuth mAuth;
    private MainActivity activity;

    private GoogleSignInClient signInClient;
    public static final int GOOGLE_SIGN_IN_CODE = 10;
    public static final int GOOGLE_SIGN_IN_LINK_CODE = 15;
    private String temp_email;
    private String temp_password;


    public AccountHelper(FirebaseAuth mAuth, MainActivity activity) {
        this.mAuth = mAuth;
        this.activity = activity;
        googleAccountManager();
    }


    //Sign Up by email
    public void signUp(String email, String password, String name) {
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
                                    creatUser(user, name);
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

        } else {
            Toast.makeText(activity, "Email или Password пустой!", Toast.LENGTH_SHORT).show();
        }
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
        Intent signInIntent = signInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, code);
    }

    public void SignInFireBaseGoogle(String idToken, int index) {
        GoogleAuthCredential credential = (GoogleAuthCredential) GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (index == 1) linkEmailAndPassword(temp_email, temp_password);
                    Toast.makeText(activity, "Log in Done", Toast.LENGTH_SHORT).show();

                    activity.updateUI();
                } else {

                }
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
                SignInGoogle(GOOGLE_SIGN_IN_LINK_CODE);
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

    private void creatUser(FirebaseUser firebaseUser, String name) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(DbManager.USERS);
       // myRef.push().child("empty").setValue("llll");
        User user = new User();
        //user.setId(firebaseUser.getUid());
        //user.setId(firebaseUser.get);
        //usersDatabaseReference.child(user.getId()).setValue(user);
        String key = FirebaseDatabase.getInstance().getReference().push().getKey();
        user.setKey(key);
        user.setId(FirebaseAuth.getInstance().getUid());
        user.setName(name);
        if (key != null) {
            myRef.push().setValue(user);
//            mainAppClass.getMainDbRef().child(key).child(mainAppClass.getAuth().getUid()).child("post").setValue(post);
            //           mainAppClass.getMainDbRef().child(key).child("status").setValue(StatusManager.fillStatusItem(post));
        }
    }
}