package com.android.linkedphotoShSonya.act;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.linkedphotoShSonya.Adapter.Subscribers;
import com.android.linkedphotoShSonya.Adapter.UserAdapter;
import com.android.linkedphotoShSonya.Observer;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.chat.ChatActivity;
import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.db.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MyChatsActivity extends AppCompatActivity implements Subscribers, Observer {

    DatabaseReference usersDatabaseReference;
    private String userName;
    private ChildEventListener usersChildEvenListener;
    private ArrayList<User> userArrayList;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private RecyclerView.LayoutManager userLayoutManager;
    private DbManager dbManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_chats);
        userArrayList = new ArrayList<>();
        init();
        //attachUserDatabaseReferenceListener();

    }

    public void init() {

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        dbManager = new DbManager(this);
        dbManager.setOnSubscriptions(this);
        //currentUser.getUid()
        dbManager.receiveMyChatsUsers(currentUser.getUid());
        buildRececlerView();
    }

    private void buildRececlerView() { // создаем receclerView
        userRecyclerView = findViewById(R.id.userListRecyclerView);
        userRecyclerView.setHasFixedSize(true);
        userRecyclerView.addItemDecoration(new DividerItemDecoration(userRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        userLayoutManager = new LinearLayoutManager(this);
//        userAdapter = new UserAdapter(userArrayList);
//        userRecyclerView.setLayoutManager(userLayoutManager);
//        userRecyclerView.setAdapter(userAdapter);
//        userAdapter.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
//            @Override
//            public void onUserClick(int position) {
//                goToChat(position);
//            }
//
//
//        });
    }
    @Override
    public void onDataSubcRecived(List<String> subcribers) {
        List<String> subsc = new ArrayList<>();

        for (int i = 0; i < subcribers.size(); i++) {
            Log.d("MyLog", "ChatsUsers" + subcribers.get(i));
            // получаю уже список юзеров
        }
        dbManager.loadSubscription(subcribers);
    }

    @Override
    public void handleEvent(List<User> users) {
        userArrayList = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            userArrayList.add(users.get(i));
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d("MyLog", "UserAarraySize " + userArrayList.size());
        userAdapter = new UserAdapter(userArrayList);
        userAdapter.setDbManager(dbManager);
        userRecyclerView.setLayoutManager(userLayoutManager);
        userRecyclerView.setAdapter(userAdapter);
        Log.d("MyLog", "handleEvent2");
        userAdapter.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                Intent intent = new Intent(MyChatsActivity.this,ChatActivity.class);
                intent.putExtra("recipientUserId", userArrayList.get(position).getId());
                intent.putExtra("recipientUserName", userArrayList.get(position).getName());
                intent.putExtra("sender",currentUser.getUid());
                intent.putExtra("userPhoto", userArrayList.get(position).getImageId());
                startActivity(intent);
            }


        });
    }
}