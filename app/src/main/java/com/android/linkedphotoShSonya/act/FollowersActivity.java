package com.android.linkedphotoShSonya.act;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.linkedphotoShSonya.Adapter.Subscribers;
import com.android.linkedphotoShSonya.Adapter.UserAdapter;
import com.android.linkedphotoShSonya.Observer;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.databinding.PersonListActivitiBinding;
import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.db.NewPost;
import com.android.linkedphotoShSonya.db.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class FollowersActivity extends AppCompatActivity implements Subscribers, Observer {
    private UserAdapter userAdapter;
    private DbManager dbManager;
    private ArrayList<User> userArrayList;
    private RecyclerView userRecyclerView;
    private TextView tvEmpty;
    private RecyclerView.LayoutManager userLayoutManager;
    private String type;
    private boolean isEmpty;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        init();
    }

    private void init() {
        tvEmpty=findViewById(R.id.tvEmptyFolow);
        if (getIntent() != null) {
            Intent i = getIntent();
            type=i.getStringExtra("type");}
        if(type.equals("my_subscriptions")) {
            dbManager = new DbManager(this);
            dbManager.setOnSubscriptions(this);
            dbManager.readSubscription();
            buildRececlerView();
        }
        if(type.equals("my_followers")){
            dbManager=new DbManager(this);

        }
    }

    private void buildRececlerView() {
        Log.d("MyLog", "build");// создаем receclerView
            userRecyclerView = findViewById(R.id.my_followers);
            //tvEmpty=findViewById(R.id.tvEmptyFolow);
            userRecyclerView.setHasFixedSize(true);
            userRecyclerView.addItemDecoration(new DividerItemDecoration(userRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
            userLayoutManager = new LinearLayoutManager(this);

        // userAdapter = new UserAdapter(userArrayList);
    }
    private void isListEmpty(List<String> list){
       tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
       userRecyclerView.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
        Log.d("MyLog", "isListEmpty");
    }
    @Override
    public void onDataSubcRecived(List<String> subcribers) {
        Log.d("MyLog", "onDataSubcRecived");
        isListEmpty(subcribers);
        dbManager.loadSubscription(subcribers);


    }

    @Override
    public void handleEvent(List<User> users) {
        Log.d("MyLog", "handleEvent");
        userArrayList = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            userArrayList.add(users.get(i));
        }
        //isListEmpty(userArrayList);
        Log.d("MyLog", "UserAarraySize "+userArrayList.size());
        userAdapter = new UserAdapter(userArrayList);
        userAdapter.setDbManager(dbManager);
        userRecyclerView.setLayoutManager(userLayoutManager);
        userRecyclerView.setAdapter(userAdapter);
        Log.d("MyLog", "handleEvent2");
        userAdapter.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                Intent intent=new Intent(FollowersActivity.this, PersonListActiviti.class);
                intent.putExtra("Uid",userArrayList.get(position).getId());
                intent.putExtra("userName",userArrayList.get(position).getName());
                intent.putExtra("userPhoto",userArrayList.get(position).getImageId());
                startActivity(intent);
            }


        });
    }
}
