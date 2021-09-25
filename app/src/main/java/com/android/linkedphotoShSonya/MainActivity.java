package com.android.linkedphotoShSonya;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.linkedphotoShSonya.Adapter.DataSender;
import com.android.linkedphotoShSonya.Adapter.PostAdapter;
import com.android.linkedphotoShSonya.accounthelper.AccountHelper;
import com.android.linkedphotoShSonya.act.AdsViewActivity;
import com.android.linkedphotoShSonya.act.EditActivity;
import com.android.linkedphotoShSonya.databinding.ActivityMainBinding;
import com.android.linkedphotoShSonya.databinding.MainContentBinding;
import com.android.linkedphotoShSonya.databinding.NavHeaderBinding;
import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.db.NewPost;
import com.android.linkedphotoShSonya.dialog.SignDialog;
import com.android.linkedphotoShSonya.filter.FilterActivity;
import com.android.linkedphotoShSonya.filter.FilterManager;
import com.android.linkedphotoShSonya.utils.MyConstants;
import com.google.android.gms.ads.AdRequest;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AdsViewActivity implements NavigationView.OnNavigationItemSelectedListener,
        SearchView.OnQueryTextListener, DataSender {
    private ActivityMainBinding rootBinding;
    private MainContentBinding mainContent;//класс который хранит в себе разметку
    private NavHeaderBinding navHeader;
    private FirebaseAuth mAuth;
    private AlertDialog dialog;
    private PostAdapter.OnItemClickCustom onItemClickCustom;
    private PostAdapter postAdapter;
    private DbManager dbManager;
    public static String MAUTh = "";
    public String current_cat = MyConstants.ALL_PHOTOS;
    private AccountHelper accountHelper;
    private FloatingActionButton newAdItem;
    private MenuItem myAdsItem, myFavsItem;
    private SharedPreferences preferences;
    private SignDialog signDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootBinding = ActivityMainBinding.inflate(getLayoutInflater());
        navHeader = NavHeaderBinding.inflate(getLayoutInflater(), rootBinding.navView, false);
        mainContent = rootBinding.mainContent;
        setContentView(rootBinding.getRoot());
        addAds(mainContent.adView);
        init();

    }

    protected void onResume() {
        super.onResume();
        dbManager.onResume(preferences);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Picasso.get().load(account.getPhotoUrl()).into(navHeader.imPhoto);
        }
        showFilterDialog();
        resumeCat();

    }

    private void resumeCat() {
        switch (current_cat) {
            case MyConstants.MY_ADS:
                dbManager.getMyAds(dbManager.getMyAdsNode());
                break;
            case MyConstants.MY_FAVS:
                dbManager.getMyAds(dbManager.getMyFavAdsNode());
                break;
            default:
                dbManager.getDataFromDb(current_cat, "");
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
                        Picasso.get().load(account.getPhotoUrl()).into(navHeader.imPhoto);
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
        preferences = getSharedPreferences(MyConstants.MAIN_PREF, MODE_PRIVATE);
        setOnItemClickCustom();
        newAdItem = findViewById(R.id.fb);
//        drawerLayout.openDrawer(GravityCompat.START);
        mAuth = FirebaseAuth.getInstance();
        accountHelper = new AccountHelper(mAuth, this);
        signDialog=new SignDialog(mAuth,this,accountHelper);
        Menu menu = rootBinding.navView.getMenu();
        dbManager = new DbManager(this);
        //dbManager.getDataFromDb("notes");
        initRcView();
        initNavView();
        initToolBar();
        initDrawer();
        postAdapter.setDbManager(dbManager);
        mainContent.filterDialogLayout.imCloseFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FilterManager.clearFilter(preferences);
                mainContent.filterDialogLayout.filterHideLayout.setVisibility(View.GONE);
                dbManager.clearFilter();
            }
        });
    }

    private void initRcView() {
        mainContent.rcView.setLayoutManager(new GridLayoutManager(this, 1));//тут можно указать как будут выглядеть элементы в recyclerView обычно делают LinearLayoutManager
        List<NewPost> arrayPost = new ArrayList<>();
        postAdapter = new PostAdapter(arrayPost, this, onItemClickCustom);
        mainContent.rcView.setAdapter(postAdapter);
    }

    private void initNavView() {
        myAdsItem = rootBinding.navView.getMenu().findItem(R.id.id_my_files);
        myFavsItem = rootBinding.navView.getMenu().findItem(R.id.id_fav);
        rootBinding.navView.addHeaderView(navHeader.getRoot());
        rootBinding.navView.setNavigationItemSelectedListener(this);

    }

    private void initToolBar() {
        mainContent.toolbar.inflateMenu(R.menu.menu);
        SearchView searchView = (SearchView) mainContent.toolbar.getMenu().findItem(R.id.main_search).getActionView();
        searchView.setOnQueryTextListener(this);
        onToolbarItemClick();
    }

    private void initDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, rootBinding.drawerLayout, mainContent.toolbar, R.string.toggle_open, R.string.toggle_close);
        rootBinding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void showFilterDialog() {
        String filter = preferences.getString(MyConstants.TEXT_FILTER, "empty");
        String orderBy = preferences.getString(MyConstants.ORDER_BY_FILTER, "empty");
        if (filter.equals("empty")) {
            mainContent.filterDialogLayout.filterHideLayout.setVisibility(View.GONE);
        } else {
            mainContent.filterDialogLayout.filterHideLayout.setVisibility(View.VISIBLE);
            mainContent.filterDialogLayout.tvFilterInfo.setText(FilterManager.getFilterText(filter));
        }

    }
//    private void setOnScrollListener() {
//        rcView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
//                if (!rcView.canScrollVertically(1)) {
//                    //Log.d("MyLog ", "Can not scroll down");
//                /*   dbManager.getDataFromDb(current_cat,postAdapter.getMainList().get(postAdapter.getMainList().size()-1).getTime());
//                    rcView.scrollToPosition(0);*/
//                } else if (!rcView.canScrollVertically(-1)) {
//                    Log.d("MyLog ", "Can not scroll down");
//                }
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//        });
//    }

    public void updateUI() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isAnonymous()) {
                newAdItem.setVisibility(View.GONE);
                myFavsItem.setVisible(false);
                myAdsItem.setVisible(false);
                navHeader.tvEmail.setText(R.string.host);
            } else {
                newAdItem.setVisibility(View.VISIBLE);
                myFavsItem.setVisible(true);
                myAdsItem.setVisible(true);
                navHeader.tvEmail.setText(currentUser.getEmail());
            }
            MAUTh = mAuth.getUid();
            onResume();
        } else {
            accountHelper.signInAnonimous();
        }
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
                mainContent.toolbar.setTitle(R.string.my_files);
                break;
            case id_my_fav:
                current_cat = MyConstants.MY_FAVS;
                dbManager.getMyAds(dbManager.getMyFavAdsNode());
                mainContent.toolbar.setTitle(R.string.my_favs);

                break;
            case id_all_files:
                //current_cat = "Лента";
                current_cat = MyConstants.ALL_PHOTOS;
                dbManager.getDataFromDb(current_cat, "");
                mainContent.toolbar.setTitle(R.string.all);
                break;
            case id_sing_up:
                signDialog.showSignDialog(R.string.sing_up, R.string.signup_button, 0);
                // Toast.makeText(this, "Pressed id sign up", Toast.LENGTH_SHORT).show();
                break;
            case id_sing_in:
                signDialog.showSignDialog(R.string.sign_in, R.string.signin_button,  1);
                //Toast.makeText(this, "Pressed id sign in", Toast.LENGTH_SHORT).show();
                break;
            case id_sing_out:
                navHeader.imPhoto.setImageResource(android.R.color.transparent);
                accountHelper.SignOut();
                //Toast.makeText(this, "Pressed id sign out", Toast.LENGTH_SHORT).show();
                break;

        }
        rootBinding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signUpDialog(int title, int buttonTitle, int index) {

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


    private void onToolbarItemClick() {
        mainContent.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.filter) {
                    Intent intent = new Intent(MainActivity.this, FilterActivity.class);
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
        dbManager.setSearchText(newText);
        dbManager.getDataFromDb(current_cat, "");
        return true;
    }

    @Override
    public void onDataRecived(List<NewPost> listData) {
        Collections.reverse(listData);
        postAdapter.updateAdapter(listData);
    }
}
