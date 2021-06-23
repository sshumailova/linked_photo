package com.android.linkedphotoShSonya;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.linkedphotoShSonya.Adapter.DataSender;
import com.android.linkedphotoShSonya.Adapter.PostAdapter;
import com.android.linkedphotoShSonya.accounthelper.AccountHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView nav_view;
    private FirebaseAuth mAuth;
    private TextView userEmail;
    private AlertDialog dialog;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private FloatingActionButton fb;
    private PostAdapter.OnItemClickCustom onItemClickCustom;
    private RecyclerView rcView;
    private PostAdapter postAdapter;
    private DataSender dataSender;
    private DbManager dbManager;
    public static String MAUTh = "";
    public static String current_cat = "";
    private AccountHelper accountHelper;
    private ImageView imPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("MyLog", "Oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    protected void onResume() {
        super.onResume();
        GoogleSignInAccount account=GoogleSignIn.getLastSignedInAccount(this);
        if(account!=null){
            Picasso.get().load(account.getPhotoUrl()).into(imPhoto);
        }
        Log.d("MyLog", "OnResume");
        if (current_cat.equals("Мои файлы")) {
            dbManager.getMyDataFromDb(mAuth.getUid(), "notes");
        } else {
            dbManager.getDataFromDb("notes");
        }
    }

    private void setOnItemClickCustom() {
        onItemClickCustom = new PostAdapter.OnItemClickCustom() {
            @Override
            public void onItemSelected(int position) {

            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch ((requestCode)) {
            case AccountHelper.GOOGLE_SIGN_IN_CODE:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                //GoogleSignInAccount account= null;
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        Picasso.get().load(account.getPhotoUrl()).into(imPhoto);
                        accountHelper.SignInFireBaseGoogle(account.getIdToken(), 0);
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }

                break;
            case AccountHelper.GOOGLE_SIGN_IN_LINK_CODE:
                Task<GoogleSignInAccount> task2 = GoogleSignIn.getSignedInAccountFromIntent(data);
                //GoogleSignInAccount account= null;
                try {
                    GoogleSignInAccount account = task2.getResult(ApiException.class);
                    if (account != null) {
                        accountHelper.SignInFireBaseGoogle(account.getIdToken(), 1);
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    public void onStart() {
        super.onStart();
        getUserData();
    }

    private void init() {
        setOnItemClickCustom();
        rcView = findViewById(R.id.rcView);
        rcView.setLayoutManager(new GridLayoutManager(this, 1));//тут можно указать как будут выглядеть элементы в recyclerView обычно делают LinearLayoutManager
        List<NewPost> arrayPost = new ArrayList<>();
        postAdapter = new PostAdapter(arrayPost, this, onItemClickCustom);

        rcView.setAdapter(postAdapter);
        nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);
        imPhoto=nav_view.getHeaderView(0).findViewById(R.id.imPhoto);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.toggle_open, R.string.toggle_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
//        drawerLayout.openDrawer(GravityCompat.START);
        //test
        userEmail = nav_view.getHeaderView(0).findViewById(R.id.tvEmail);
        mAuth = FirebaseAuth.getInstance();
        accountHelper = new AccountHelper(mAuth, this);
        Menu menu=nav_view.getMenu();
        MenuItem categoryAccountItem;
        getDataDb();
        dbManager = new DbManager(dataSender, this);
        //dbManager.getDataFromDb("notes");
        postAdapter.setDbManager(dbManager);

    }

    public void getUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmail.setText(currentUser.getEmail());
            MAUTh = mAuth.getUid();
        } else {
            userEmail.setText(R.string.sign_in_our_sign_up);
            MAUTh = "";
        }
    }

    private void getDataDb() {
        dataSender = new DataSender() {
            @Override
            public void onDataRecived(List<NewPost> listData) {
                Collections.reverse(listData);
                postAdapter.updateAdapter(listData);
            }
        };
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.id_my_files:
                current_cat = "Мои файлы";
                dbManager.getMyDataFromDb(mAuth.getUid(), "notes");
                break;
            case R.id.id_all_files:
                current_cat = "Лента";
                dbManager.getDataFromDb("notes");
                break;
            case R.id.id_sing_up:
                signUpDialog(R.string.sing_up, R.string.signup_button, R.string.google_sing_up, 0);
                // Toast.makeText(this, "Pressed id sign up", Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_sing_in:
                signUpDialog(R.string.sign_in, R.string.signin_button, R.string.google_sign_in, 1);
                //Toast.makeText(this, "Pressed id sign in", Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_sing_out:
                imPhoto.setImageResource(android.R.color.transparent);
                accountHelper.SignOut();
                //Toast.makeText(this, "Pressed id sign out", Toast.LENGTH_SHORT).show();
                break;

        }
        return true;
    }

    private void signUpDialog(int title, int buttonTitle, int b2Title, int index) {

        AlertDialog.Builder dialogBulder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.sign_app_layout, null);
        dialogBulder.setView(dialogView);
        TextView titleTextView = dialogView.findViewById(R.id.tvAlerTitle);
        TextView tMessage= dialogView.findViewById(R.id.tvMessage);
        titleTextView.setText(title);
        Button singUpEmail = dialogView.findViewById(R.id.buttonSignUp);
        SignInButton signUpGoogle = dialogView.findViewById(R.id.bSignGoogle);
        Button forgetPassword = dialogView.findViewById(R.id.bForgetPassword);
        switch (index){
            case 0:
                forgetPassword.setVisibility(View.GONE);
                break;
            case 1:
                forgetPassword.setVisibility(View.VISIBLE);
                break;
        }
        EditText edEmail = dialogView.findViewById(R.id.edEmail);
        EditText edPassword = dialogView.findViewById(R.id.edPassword);
        singUpEmail.setText(buttonTitle);
        singUpEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index == 0) {
                    accountHelper.signUp(edEmail.getText().toString(), edPassword.getText().toString());
                } else {
                    accountHelper.SignIn(edEmail.getText().toString(), edPassword.getText().toString());
                }
                dialog.dismiss();
            }
        });
        signUpGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    dialog.dismiss();
                    return;
                } else {

                    accountHelper.SignInGoogle(AccountHelper.GOOGLE_SIGN_IN_CODE);
                }
                dialog.dismiss();
            }
        });
        forgetPassword.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  if (edPassword.isShown()) {
                                                      edPassword.setVisibility(View.GONE);
                                                      singUpEmail.setVisibility(View.GONE);
                                                      signUpGoogle.setVisibility(View.GONE);
                                                      titleTextView.setText(R.string.forget_password);
                                                      forgetPassword.setText(R.string.send_recet_password);
                                                      tMessage.setVisibility(View.VISIBLE);
                                                      tMessage.setText(R.string.forget_password_message);
                                                      // dialog.dismiss();
                                                  } else {
                                                      if (!edEmail.getText().toString().equals("")) {
                                                          FirebaseAuth.getInstance().sendPasswordResetEmail(edEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                              @Override
                                                              public void onComplete(@NonNull Task<Void> task) {
                                                                  if (task.isSuccessful()) {
                                                                      Toast.makeText(MainActivity.this, R.string.email_is_send, Toast.LENGTH_SHORT).show();
                                                                      dialog.dismiss();
                                                                  } else {
                                                                      Toast.makeText(MainActivity.this, "Mistake", Toast.LENGTH_SHORT).show();
                                                                  }
                                                              }
                                                          });
                                                      } else {
                                                          Toast.makeText(MainActivity.this, R.string.email_is_empty, Toast.LENGTH_SHORT).show();
                                                      }
                                                  }
                                              }
                                          });
        dialog = dialogBulder.create();
        if(dialog.getWindow()!=null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog.show();

    }

    public void onClickEdit(View view) {
        if (mAuth.getCurrentUser() != null) {
            if (mAuth.getCurrentUser().isEmailVerified()) {
                Intent i = new Intent(MainActivity.this, EditActivity.class);
                startActivity(i);
            } else {
                accountHelper.showDialogNotVarificate(R.string.alert, R.string.email_not_verified);
            }
        }

    }


}
