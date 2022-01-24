package com.sonya_shum.linkedphotoShSonya;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;


import com.sonya_shum.linkedphotoShSonya.Adapter.DataSender;
import com.sonya_shum.linkedphotoShSonya.Adapter.PostAdapter;
import com.sonya_shum.linkedphotoShSonya.accounthelper.AccountHelper;
import com.sonya_shum.linkedphotoShSonya.act.AdminActivity;
import com.sonya_shum.linkedphotoShSonya.act.AdsViewActivity;
import com.sonya_shum.linkedphotoShSonya.act.EditActivity;
import com.sonya_shum.linkedphotoShSonya.act.FollowersActivity;
import com.sonya_shum.linkedphotoShSonya.act.MyChatsActivity;
import com.sonya_shum.linkedphotoShSonya.act.PersonListActiviti;
import com.sonya_shum.linkedphotoShSonya.biling.BillingManager;
import com.sonya_shum.linkedphotoShSonya.databinding.ActivityMainBinding;
import com.sonya_shum.linkedphotoShSonya.databinding.MainContentBinding;
import com.sonya_shum.linkedphotoShSonya.databinding.NavHeaderBinding;
import com.sonya_shum.linkedphotoShSonya.db.DbManager;
import com.sonya_shum.linkedphotoShSonya.db.NewPost;
import com.sonya_shum.linkedphotoShSonya.db.User;
import com.sonya_shum.linkedphotoShSonya.dialog.SignDialog;
import com.sonya_shum.linkedphotoShSonya.filter.FilterActivity;
import com.sonya_shum.linkedphotoShSonya.filter.FilterManager;
import com.sonya_shum.linkedphotoShSonya.utils.CircleTransform;
import com.sonya_shum.linkedphotoShSonya.utils.ImagesManager;
import com.sonya_shum.linkedphotoShSonya.utils.MyConstants;
import com.sonya_shum.linkedphotoShSonya.utils.OnBitMapLoaded;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

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
    private Query mQuery;
    private final int EDIT_RES = 12;
    public static String MAUTh = "";
    public String current_cat = MyConstants.ALL_PHOTOS;
    private AccountHelper accountHelper;
    private FloatingActionButton newAdItem;
    private MenuItem myAdsItem, myFavsItem;
    private SharedPreferences preferences;
    private SignDialog signDialog;
    private ImagesManager imagesManager;
    private OnBitMapLoaded onBitMapLoaded;
    private String UserName;
    private String UserPhoto;
    private MenuItem myChats;
    private Toolbar toolbar;
    //private ActivityResultLauncher<Intent> editLauncher;
    private ActivityResultLauncher<Intent> signInLauncher;
    private BillingManager billingManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MyLog", "OnCreate");
        super.onCreate(savedInstanceState);
        rootBinding = ActivityMainBinding.inflate(getLayoutInflater());
        navHeader = NavHeaderBinding.inflate(getLayoutInflater(), rootBinding.navView, false);
        navHeader.NameAndLogo.setOnClickListener(onClickItem());
        mainContent = rootBinding.mainContent;
        setContentView(rootBinding.getRoot());
        addAds(mainContent.adView);
        init();
        onGoogleSignInResult();

        // onEditResult();

    }

    private View.OnClickListener onClickItem() {
        return view -> {
            if (view.getId() == R.id.NameAndLogo) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    if (currentUser.isAnonymous()) {
                        //кароч тут надо сделать диалоговое окно
                    }
                    Intent intent = new Intent(MainActivity.this, userActivity.class);
                    intent.putExtra("Uid", currentUser.getUid());
                    startActivity(intent);

                }
            }

        };
    }

    protected void onResume() {
        super.onResume();
        Log.d("MyLog", " onResume");
        dbManager.onResume(preferences);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Picasso.get().load(account.getPhotoUrl()).into(navHeader.imPhoto);
        }
        showFilterDialog();
        resumeCat();

    }

    //private  void onEditResult(){// инициализируе лаунчер и уже после  того как создастся обьявление -возвращаемся на mainActivity НУЖНО ДЛЯ НИЖНИЙ ПАНЕЛИ УРОК 120
//editLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
//    if(result.getData()!=null){
//        current_cat=result.getData().getStringExtra("cat");
//    }
//});
//}
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

    private void onGoogleSignInResult() {
        signInLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getData() != null) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                //GoogleSignInAccount account= null;
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        // Picasso.get().load(user.getImageId()).into(navHeader.imPhoto);
                        // Picasso.get().load(account.getPhotoUrl()).into(navHeader.imPhoto);
                        //String a=account.getEmail();
                        //accountHelper.createUserWithGoogle(a);
                        //Log.d("MyLog", "onActivityResuly : " + requestCode);
                        accountHelper.SignInFireBaseGoogle(account.getIdToken(), account);
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
//        super.onSaveInstanceState(outState, outPersistentState);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//}
    public void onStart() {
        super.onStart();
        Log.d("MyLog", " onStart");
        updateUI();
    }

    private void init() {
        preferences = getSharedPreferences(MyConstants.MAIN_PREF, MODE_PRIVATE);
        setOnItemClickCustom();
        billingManager=new BillingManager(this);
        if(isPurchased()){
            mainContent.adView.setVisibility(View.GONE);
        }
        // newAdItem = findViewById(R.id.fb);
//        drawerLayout.openDrawer(GravityCompat.START);
        mAuth = FirebaseAuth.getInstance();
        dbManager = new DbManager(this);
        accountHelper = new AccountHelper(mAuth, this, dbManager);
        dbManager.addObserver(accountHelper);
        //imagesManager = new ImagesManager(this, onBitMapLoaded);
        signDialog = new SignDialog(mAuth, this, accountHelper); //imagesManager создала что бы преедать его в sign in

        Menu menu = rootBinding.navView.getMenu();
        //dbManager.getDataFromDb("notes");
        initRcView();
        initNavView();
        initToolBar();
        initDrawer();
        postAdapter.setDbManager(dbManager);
        setNavViewStyle();
        setBottomNavListener();
        mainContent.filterDialogLayout.imCloseFilter.setOnClickListener(view -> {
            FilterManager.clearFilter(preferences);
            mainContent.filterDialogLayout.filterHideLayout.setVisibility(View.GONE);
            dbManager.clearFilter();
            updateUI();

        });

        //FFdbManager.readSubscription();
    }

    public void closeFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            if (f.isVisible()) getSupportFragmentManager().beginTransaction().remove(f).commit();
        }
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
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mainContent.toolbar.inflateMenu(R.menu.menu);
        SearchView searchView = (SearchView) mainContent.toolbar.getMenu().findItem(R.id.main_search).getActionView();
        searchView.setOnQueryTextListener(this);
       mainContent.toolbar.getMenu().findItem(R.id.go_chats).getActionView();
        //onOptionsItemSelected(mainContent.toolbar.getMenu().findItem(R.id.go_chats));
      //  myChats=mainContent.toolbar.getMenu().findItem(R.id.go_chats);
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
    public void closePixFragment() {
        // if (isImagesLoaded) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            if (f.isVisible())
                getSupportFragmentManager().beginTransaction().remove(f).commit();
        }
    }

    public void updateUI() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        mQuery = myRef.child(dbManager.USERS);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isAnonymous()) {
                // newAdItem.setVisibility(View.GONE); тут сделать невидимиы
                myFavsItem.setVisible(false);
                myAdsItem.setVisible(false);
                navHeader.tvEmail.setText(R.string.host);
            } else {
                ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            User user = null;
                            user = ds.child("user").getValue(User.class);
                            if (user.getId().equals(currentUser.getUid())) {
                                UserName = user.getName();
                                UserPhoto = user.getImageId();
                                navHeader.tvEmail.setText(user.getName());
                                //  Picasso.get().load(user.getImageId()).into(navHeader.UserPhoto);
                                Picasso.get().load(user.getImageId()).transform(new CircleTransform()).into(navHeader.UserPhoto);
                                return;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                mQuery.addListenerForSingleValueEvent(listener);
                //newAdItem.setVisibility(View.VISIBLE); Тут сделать невидимым
                myFavsItem.setVisible(true);
                myAdsItem.setVisible(true);
//                if(currentUser.getUid().equals(myRef)){
                //navHeader.tvEmail.setText(currentUser.getEmail());
            }
//        }
            MAUTh = mAuth.getUid();
            onResume();
            dbManager.isAdmin(new DbManager.ResultListener() {
                @Override
                public void onResult(boolean result) {
                    showAdminPanel(result);
                }
            });
        } else {
            accountHelper.signInAnonimous();
        }
    }

    private void showAdminPanel(boolean visible) {
        rootBinding.navView.getMenu().findItem(R.id.adminCatID).setVisible(visible);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.go_chats) {
            Intent intent2 = new Intent(MainActivity.this, MyChatsActivity.class);
            startActivity(intent2);

        }
        return true;
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
        final int id_admin = R.id.id_admin;
        final int my_subscriptions = R.id.my_subscriptions;
        final int my_followers = R.id.my_followers;
        final int id_remove_ad= R.id.id_remove_ads;
        postAdapter.isStartPage = true;
        switch (id) {
            case id_admin:
                startActivity(new Intent(MainActivity.this, AdminActivity.class));
                break;

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
            case my_subscriptions:
                Intent intent = new Intent(MainActivity.this, FollowersActivity.class);
                intent.putExtra("type", "my_subscriptions");
                startActivity(intent);
                break;
            case my_followers:
                Intent intent2 = new Intent(MainActivity.this, FollowersActivity.class);
                intent2.putExtra("type", "my_followers");
                startActivity(intent2);
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
                signDialog.showSignDialog(R.string.sign_in, R.string.signin_button, 1);
                //Toast.makeText(this, "Pressed id sign in", Toast.LENGTH_SHORT).show();
                break;
            case id_remove_ad:
                billingManager.startConection();
                break;
            case id_sing_out:
                navHeader.UserPhoto.setImageResource(android.R.color.transparent);
                accountHelper.SignOut();
                //Toast.makeText(this, "Pressed id sign out", Toast.LENGTH_SHORT).show();
                break;

        }
        rootBinding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signUpDialog(int title, int buttonTitle, int index) {

    }

    public void onClickEdit() {
        if (mAuth.getCurrentUser() != null) {
            if (mAuth.getCurrentUser().isEmailVerified()) {
                Intent i = new Intent(MainActivity.this, EditActivity.class);
                i.putExtra("userName", UserName);
                i.putExtra("userPhoto", UserPhoto);
                startActivity(i);
            } else {
                accountHelper.showDialogNotVarificate(R.string.alert, R.string.email_not_verified);
            }
        }

    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }


    private void setBottomNavListener() {
        rootBinding.mainContent.bNavMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.new_add) {
                    onClickEdit();
                } else if (item.getItemId() == R.id.filter) {
                    Intent intent = new Intent(MainActivity.this, FilterActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.id_my_ads) {
                    Intent intent = new Intent(MainActivity.this, PersonListActiviti.class);
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    // String =navHeader.tvEmail.getText().toString();
                    intent.putExtra("Uid", currentUser.getUid());
                    intent.putExtra("isSubscriber", "itIsCurrentUser");
//                    intent.putExtra("userName",navHeader.tvEmail.getText().toString());
//                    intent.putExtra("userPhoto", navHeader.UserPhoto.getDrawable().toString());
                    startActivity(intent);

                } else if (item.getItemId() == R.id.id_fav) {
                    current_cat = MyConstants.MY_FAVS;
                    dbManager.getMyAds(dbManager.getMyFavAdsNode());
                    mainContent.toolbar.setTitle(R.string.my_favs);
                }
                return true;
            }
        });

    }

    private void setNavViewStyle() {
        Menu menu = rootBinding.navView.getMenu();
        MenuItem categoryAccountItem = menu.findItem(R.id.accountCatId);
        MenuItem categoryAdmin = menu.findItem(R.id.adminCatID);
        SpannableString sp = new SpannableString(categoryAccountItem.getTitle());
        SpannableString sp1 = new SpannableString(categoryAdmin.getTitle());
        sp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green)), 0, sp.length(), 0);
        sp1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green)), 0, sp1.length(), 0);
        categoryAccountItem.setTitle(sp);
        categoryAdmin.setTitle(sp1);
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


    protected void onPause() {

        super.onPause();
        Log.d("MyLog", "OnPAuce");
    }
private boolean isPurchased(){
        return preferences.getBoolean(BillingManager.REMOVE_ADS_KEY,false);
}
    public ActivityResultLauncher<Intent> getSignInLauncher() {
        return signInLauncher;
    }
}
