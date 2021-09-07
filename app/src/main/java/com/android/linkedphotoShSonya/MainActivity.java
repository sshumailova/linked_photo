package com.android.linkedphotoShSonya;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.linkedphotoShSonya.Adapter.DataSender;
import com.android.linkedphotoShSonya.Adapter.PostAdapter;
import com.android.linkedphotoShSonya.accounthelper.AccountHelper;
import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.db.NewPost;
import com.android.linkedphotoShSonya.filter.FilterActivity;
import com.android.linkedphotoShSonya.filter.FilterManager;
import com.android.linkedphotoShSonya.utils.MyConstants;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener {
    private NavigationView nav_view;
    private FirebaseAuth mAuth;
    private TextView userEmail;
    private TextView tvfilterInfo;
    private AlertDialog dialog;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private PostAdapter.OnItemClickCustom onItemClickCustom;
    public RecyclerView rcView;
    private PostAdapter postAdapter;
    private DataSender dataSender;
    private DbManager dbManager;
    public static String MAUTh = "";
    public String current_cat = MyConstants.ALL_PHOTOS;
    private AccountHelper accountHelper;
    private ImageView imPhoto;
    private ImageButton imCloseFilter;
    private FloatingActionButton newAdItem;
    private MenuItem myAdsItem, myFavsItem;
    private AdView adView;
    private SharedPreferences preferences;
    private CardView filterHideContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("MyLog", "Oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addAds();
        init();
        setOnScrollListener();
    }

    protected void onResume() {
        super.onResume();
        dbManager.onResume(preferences);
        if (adView != null) {
            adView.resume();
        }
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Picasso.get().load(account.getPhotoUrl()).into(imPhoto);
        }
        showFilterDialog();
        dbManager.getDataFromDb(current_cat, "",false);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
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
        updateUI();
    }

    private void init() {
        preferences=getSharedPreferences(MyConstants.MAIN_PREF,MODE_PRIVATE);
        filterHideContainer=findViewById(R.id.filterHideLayout);
        tvfilterInfo=findViewById(R.id.tvFilterInfo);
        imCloseFilter=findViewById(R.id.imCloseFilter);
        setOnItemClickCustom();
        rcView = findViewById(R.id.rcView);
        rcView.setLayoutManager(new GridLayoutManager(this, 1));//тут можно указать как будут выглядеть элементы в recyclerView обычно делают LinearLayoutManager
        List<NewPost> arrayPost = new ArrayList<>();
        postAdapter = new PostAdapter(arrayPost, this, onItemClickCustom);

        rcView.setAdapter(postAdapter);
        nav_view = findViewById(R.id.nav_view);
        myAdsItem = nav_view.getMenu().findItem(R.id.id_my_files);
        myFavsItem = nav_view.getMenu().findItem(R.id.id_fav);
        nav_view.setNavigationItemSelectedListener(this);
        imPhoto = nav_view.getHeaderView(0).findViewById(R.id.imPhoto);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu);
        SearchView searchView = (SearchView) toolbar.getMenu().findItem(R.id.main_search).getActionView();
        searchView.setOnQueryTextListener(this);

        newAdItem = findViewById(R.id.fb);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.toggle_open, R.string.toggle_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
//        drawerLayout.openDrawer(GravityCompat.START);
        //test
        userEmail = nav_view.getHeaderView(0).findViewById(R.id.tvEmail);
        mAuth = FirebaseAuth.getInstance();
        accountHelper = new AccountHelper(mAuth, this);
        Menu menu = nav_view.getMenu();
        MenuItem categoryAccountItem;
        getDataDb();
        dbManager = new DbManager(dataSender, this);
        //dbManager.getDataFromDb("notes");
        postAdapter.setDbManager(dbManager);
        onToolbarItemClick();
        imCloseFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FilterManager.clearFilter(preferences);
                filterHideContainer.setVisibility(View.GONE);
                dbManager.clearFilter();
            }
        });
    }
private void showFilterDialog(){
        String filter=preferences.getString(MyConstants.TEXT_FILTER,"empty");
        String orderBy=preferences.getString(MyConstants.ORDER_BY_FILTER,"empty");

        if(filter.equals("empty")){
            filterHideContainer.setVisibility(View.GONE);
        }
        else {
            filterHideContainer.setVisibility(View.VISIBLE);
            tvfilterInfo.setText(FilterManager.getFilterText(filter));
        }

}
    private void setOnScrollListener() {
        rcView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                if (!rcView.canScrollVertically(1)) {
                    //Log.d("MyLog ", "Can not scroll down");
                /*   dbManager.getDataFromDb(current_cat,postAdapter.getMainList().get(postAdapter.getMainList().size()-1).getTime());
                    rcView.scrollToPosition(0);*/
                } else if (!rcView.canScrollVertically(-1)) {
                    Log.d("MyLog ", "Can not scroll down");
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    public void updateUI() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isAnonymous()) {
                newAdItem.setVisibility(View.GONE);
                myFavsItem.setVisible(false);
                myAdsItem.setVisible(false);
                userEmail.setText(R.string.host);
            } else {
                newAdItem.setVisibility(View.VISIBLE);
                myFavsItem.setVisible(true);
                myAdsItem.setVisible(true);
                userEmail.setText(currentUser.getEmail());
            }
            MAUTh = mAuth.getUid();
            onResume();
        } else {
            accountHelper.signInAnonimous();
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
        final int id_my_files = R.id.id_my_files;
        final int id_all_files = R.id.id_all_files;
        final int id_sing_up = R.id.id_sing_up;
        final int id_sing_in = R.id.id_sing_in;
        final int id_sing_out = R.id.id_sing_out;
        final int id_my_fav = R.id.id_fav;
        postAdapter.isStartPage = true;
        switch (id) {
            case id_my_files:
                //current_cat = "Мои файлы";
                current_cat = MyConstants.MY_ADS;
                dbManager.getMyAds(dbManager.getMyAdsNode());
                break;
            case id_my_fav:
                current_cat = MyConstants.MY_FAVS;
                dbManager.getMyAds(dbManager.getMyFavAdsNode());

                break;
            case id_all_files:
                //current_cat = "Лента";
                current_cat = MyConstants.ALL_PHOTOS;
                dbManager.getDataFromDb(current_cat, "",false);
                break;
            case id_sing_up:
                signUpDialog(R.string.sing_up, R.string.signup_button, R.string.google_sing_up, 0);
                // Toast.makeText(this, "Pressed id sign up", Toast.LENGTH_SHORT).show();
                break;
            case id_sing_in:
                signUpDialog(R.string.sign_in, R.string.signin_button, R.string.google_sign_in, 1);
                //Toast.makeText(this, "Pressed id sign in", Toast.LENGTH_SHORT).show();
                break;
            case id_sing_out:
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
        TextView tMessage = dialogView.findViewById(R.id.tvMessage);
        titleTextView.setText(title);
        Button singUpEmail = dialogView.findViewById(R.id.buttonSignUp);
        SignInButton signUpGoogle = dialogView.findViewById(R.id.bSignGoogle);
        Button forgetPassword = dialogView.findViewById(R.id.bForgetPassword);
        switch (index) {
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
                if (mAuth.getCurrentUser() != null) {
                    if (mAuth.getCurrentUser().isAnonymous()) {
                        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    if (index == 0) {
                                        accountHelper.signUp(edEmail.getText().toString(), edPassword.getText().toString());
                                    } else {
                                        accountHelper.SignIn(edEmail.getText().toString(), edPassword.getText().toString());
                                    }
                                }
                            }
                        });

                    }
                }
                dialog.dismiss();
            }
        });
        signUpGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    if (mAuth.getCurrentUser().isAnonymous()) {
                        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
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
        if (dialog.getWindow() != null)
            dialog.getWindow().

                    setBackgroundDrawableResource(android.R.color.transparent);

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

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    private void addAds() {
        MobileAds.initialize(this);
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
private void onToolbarItemClick(){
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.filter){
                    Intent intent=new Intent(MainActivity.this, FilterActivity.class);
                    startActivity(intent);
                }
                return false;
            }
        });
}

    @Override
    public boolean onQueryTextSubmit(String query) {

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        dbManager.getSearchResult(newText.toLowerCase());
        return true;
    }
}
