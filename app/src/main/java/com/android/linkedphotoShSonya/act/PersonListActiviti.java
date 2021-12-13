package com.android.linkedphotoShSonya.act;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.android.linkedphotoShSonya.Observer;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.accounthelper.AccountHelper;
import com.android.linkedphotoShSonya.chat.ChatActivity;
import com.android.linkedphotoShSonya.databinding.PersonListActivitiBinding;
import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.db.NewPost;
import com.android.linkedphotoShSonya.db.User;
import com.android.linkedphotoShSonya.utils.CircleTransform;
import com.android.linkedphotoShSonya.utils.MyConstants;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PersonListActiviti extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SearchView.OnQueryTextListener, DataSender, Observer {

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
    private String userPhoto;

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
//            UserName = i.getStringExtra("userName");
//            UserPhoto = i.getStringExtra("userPhoto");

            String a = i.getStringExtra("isSubscriber");
            Log.d("MyLog","isSubscriber "+ a);
            if (a.equals("false")) {
                binding.addSubscription.setVisibility(View.VISIBLE);
                binding.removeSub.setVisibility(View.GONE);
            }
            if (a.equals("true")) {
                binding.addSubscription.setVisibility(View.GONE);
                binding.removeSub.setVisibility(View.VISIBLE);
            }
            if (a.equals("itIsCurrentUser")) {
                binding.addSubscription.setVisibility(View.GONE);
                binding.removeSub.setVisibility(View.GONE);
            }
//            if(a.equals("my_followers")){
//
//                    binding.addSubscription.setVisibility(View.VISIBLE);
//                    binding.removeSub.setVisibility(View.GONE);
//                }


        }
        setOnItemClickCustom();
        mAuth = FirebaseAuth.getInstance();
        dbManager = new DbManager(this);
        users = FirebaseDatabase.getInstance().getReference(DbManager.USERS).child(uid);
        dbManager.getCurrentUser(uid);
        dbManager.loadAllUsers(this);
        // dbManager.setOnSubscriptions(this);
        // dbManager.readSubscription();
        // dbManager.readSubscription();
        // getDataSub();
        //accountHelper = new AccountHelper(mAuth, this, dbManager);
        //  dbManager.addObserver(accountHelper);
        initRcView();
        postAdapter.setDbManager(dbManager);
        showData();
        binding.removeSub.setOnClickListener(onClickItem());
        binding.addSubscription.setOnClickListener(onClickItem());
        binding.bStarnChat.setOnClickListener(onClickItem());
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


    public void AddSubscription() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PersonListActiviti.this);
        builder.setMessage(R.string.add_subscrip);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dbManager.AddSubscription(uid);
                binding.addSubscription.setVisibility(View.GONE);
                binding.removeSub.setVisibility(View.VISIBLE);
                //dbManager.readSubscription();
            }
        });
        builder.show();

        //  dbManager.readSubscription();

    }

    public void RemoveSubscription() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PersonListActiviti.this);
        builder.setMessage(R.string.remove_subscrip);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removeSub();
                binding.addSubscription.setVisibility(View.VISIBLE);
                binding.removeSub.setVisibility(View.GONE);
            }
        });
        builder.show();
//        dbManager.readSubscription();
//        showData();

    }

    public void removeSub() {
        dbManager.removeSubscription(uid);
        // dbManager.readSubscription();
    }

    private View.OnClickListener onClickItem() {
        return view -> {
            if (view.getId() == R.id.addSubscription) {
                AddSubscription();
                // dbManager.readSubscription();

            } else if (view.getId() == R.id.removeSub) {
                RemoveSubscription();
                // dbManager.readSubscription();

            }
            else if(view.getId()==R.id.bStarnChat){
                Log.d("MyLog", "GoTOCHat");
                FirebaseUser currentUser = mAuth.getCurrentUser();
                Intent intent=new Intent(this, ChatActivity.class);
                intent.putExtra("recipientUserId",uid);
                String name=binding.tvEmail.toString();
                intent.putExtra("recipientUserName", name);
                intent.putExtra("sender",currentUser.getUid());
                intent.putExtra("userPhoto", userPhoto);
                startActivity(intent);
            }
        };
    }

    @Override
    public void handleEvent(List<User> users) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            binding.tvEmail.setText(user.getName());
            userPhoto=user.getImageId();
            Picasso.get().load(user.getImageId()).transform(new CircleTransform()).into(binding.UserPhoto);
        }
    }
}

//    @Override
//    public void onDataSubcRecived(List<String> subcribers) {
//        Log.d("MyLog","SubListSizze" + subcribers.size());
//        if (subcribers.size() == 0) {
//            binding.addSubscription.setVisibility(View.VISIBLE);
//            binding.removeSub.setVisibility(View.GONE);
//        }
//       else if (subcribers.contains(uid)) {
//            binding.addSubscription.setVisibility(View.GONE);
//            binding.removeSub.setVisibility(View.VISIBLE);
//        } else {
//            binding.addSubscription.setVisibility(View.VISIBLE);
//            binding.removeSub.setVisibility(View.GONE);
//        }
//    }
