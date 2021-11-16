package com.android.linkedphotoShSonya.act;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.android.linkedphotoShSonya.Adapter.DataSender;
import com.android.linkedphotoShSonya.Adapter.ImageAdapter;
import com.android.linkedphotoShSonya.Adapter.PostAdapter;
import com.android.linkedphotoShSonya.Adapter.Subscribers;
import com.android.linkedphotoShSonya.accounthelper.AccountHelper;
import com.android.linkedphotoShSonya.databinding.PersonListActivitiBinding;
import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.db.NewPost;
import com.android.linkedphotoShSonya.db.User;
import com.android.linkedphotoShSonya.utils.CircleTransform;
import com.android.linkedphotoShSonya.utils.MyConstants;
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

public class PersonListActiviti extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SearchView.OnQueryTextListener, DataSender, Subscribers {

    private List<NewPost> newPostList;
    private MainAppClass mainAppClass;
    private Context context;
    private Query mQuery;
    private DatabaseReference mainNode;
    private DatabaseReference users;
    public List<User> usersList;
    private List<String> uidListSubscriptions;
    private DbManager dbManager;
    private List<String> imagesUris;
    private ImageAdapter imageAdapter;
    private int like;
    private NewPost newPost;
    private PersonListActivitiBinding binding;
    private FirebaseAuth mAuth;
    private String uid;
    private String UserName;
    private String UserPhoto;
    private PostAdapter.OnItemClickCustom onItemClickCustom;
    private PostAdapter postAdapter;
    private AccountHelper accountHelper;
    private List<NewPost> arrayPost;
    public String current_cat = MyConstants.ALL_PHOTOS;
    private Subscribers subscribers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = PersonListActivitiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        dbManager.readDataUpdate();
        Log.d("MyLog", " onREStart person");
    }

    public void onStart() {
        super.onStart();
        Log.d("MyLog", " onStart person");
    }

    private void init() {
        if (getIntent() != null) {
            Intent i = getIntent();
            uid = i.getStringExtra("Uid");
            UserName = i.getStringExtra("userName");
            UserPhoto = i.getStringExtra("userPhoto");
            binding.tvEmail.setText(UserName);
            Picasso.get().load(UserPhoto).transform(new CircleTransform()).into(binding.UserPhoto);
        }
        setOnItemClickCustom();
        mAuth = FirebaseAuth.getInstance();
        dbManager = new DbManager(this);
        dbManager.setOnSubscriptions(this);
        dbManager.readSubscription();
        // dbManager.readSubscription();
        // getDataSub();
        //accountHelper = new AccountHelper(mAuth, this, dbManager);
        //  dbManager.addObserver(accountHelper);
        initRcView();
        postAdapter.setDbManager(dbManager);
        showData();
        ///FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    private void initRcView() {
        binding.rcView.setLayoutManager(new GridLayoutManager(this, 2));//тут можно указать как будут выглядеть элементы в recyclerView обычно делают LinearLayoutManager
        arrayPost = new ArrayList<>();
        postAdapter = new PostAdapter(arrayPost, this, onItemClickCustom);
        binding.rcView.setAdapter(postAdapter);


    }

    //public void updateAdapter(List<NewPost> listData){
//        arrayPost.clear();
//        arrayPost.addAll(listData);
//}
    public void showData() {
        dbManager.getAllOwnerAds(uid);
    }


    @Override
    public void onDataRecived(List<NewPost> listData) {
        Collections.reverse(listData);
        postAdapter.updateAdapter(listData);
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    private void setOnItemClickCustom() {
        onItemClickCustom = new PostAdapter.OnItemClickCustom() {
            @Override
            public void onItemSelected(int position) {

            }
        };
    }

    protected void onResume() {
        super.onResume();
        Log.d("MyLog", "OnResume person");

    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        dbManager.setSearchText(newText);
        dbManager.getDataFromDb(current_cat, "");
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return true;
    }

    public void updateUI() {


    }


    public void OnclickAddSubscription(View view) {
        dbManager.AddSubscription(uid);
       // dbManager.readSubscription();

    }

    public void OnclickRemoveSubscription(View view) {
        dbManager.removeSubscription(uid);
        dbManager.readSubscription();

    }
    @Override
    public void onDataSubcRecived(List<String> subcribers) {
//        for (int i = 0; i <= subcribers.size(); i++) {
//            if (subcribers.get(i).toString().equals(uid)) {
//                binding.addSubscription.setVisibility(View.GONE);
//                binding.removeSub.setVisibility(View.VISIBLE);
//                break;
//            } else {
//                binding.addSubscription.setVisibility(View.VISIBLE);
//            }
//        }

        if (subcribers.contains(uid)) {
            binding.addSubscription.setVisibility(View.GONE);
            binding.removeSub.setVisibility(View.VISIBLE);
        } else {
            binding.addSubscription.setVisibility(View.VISIBLE);
            binding.removeSub.setVisibility(View.GONE);
        }
    }
}