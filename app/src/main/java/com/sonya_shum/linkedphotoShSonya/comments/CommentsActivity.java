package com.sonya_shum.linkedphotoShSonya.comments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sonya_shum.linkedphotoShSonya.Adapter.UserAdapter;
import com.sonya_shum.linkedphotoShSonya.Observer;
import com.sonya_shum.linkedphotoShSonya.R;
import com.sonya_shum.linkedphotoShSonya.act.MainAppClass;
import com.sonya_shum.linkedphotoShSonya.act.PersonListActiviti;
import com.sonya_shum.linkedphotoShSonya.dagger.App;
import com.sonya_shum.linkedphotoShSonya.db.DbManager;
import com.sonya_shum.linkedphotoShSonya.db.NewPost;
import com.sonya_shum.linkedphotoShSonya.db.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class CommentsActivity extends AppCompatActivity implements Observer {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String currentUserUid;
    private NewPost newPost;
    private EditText commentEditText;
    private Button sendCommentButton;
    private RecyclerView commentsListView;
    DatabaseReference comentsDatabaseReference;
    private ChildEventListener commentsChildEvenListener;
    private DbManager dbManager;
    private String senderUid;
    private String senderName;
    private String senderPhoto;
    private DatabaseReference commentsDbRef;
    private CommentAdapter adapter;
    private List<Comment> commentsList;
    private RecyclerView.LayoutManager commentLayoutManager;
    private ChildEventListener comentsChildEvenListener;
    @Inject
    MainAppClass mainAppClass;
    @Inject
    FirebaseAuth firebaseAuth;
    @Inject
    @Named("mainDb")
    DatabaseReference databaseReferenceMain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     ((App)getApplication()).getComponent().inject(this);
        setContentView(R.layout.activity_comments);
        auth = firebaseAuth;
        init();
    }

    public void init() {
        Intent intent = getIntent();
        if (intent != null) {
            currentUserUid = intent.getStringExtra("currentUser");
            newPost = (NewPost) intent.getSerializableExtra("post");
        }
        commentsDbRef = databaseReferenceMain.child(newPost.getKey()).child("comments");
        dbManager = new DbManager(this);
        commentsList = new ArrayList<>();
        commentEditText = findViewById(R.id.commentEditText);
        sendCommentButton = findViewById(R.id.sendCommentButton);
        // тут надо его заполнить
        dbManager.getCurrentUser(currentUserUid);
        dbManager.loadAllUsers(this);


//        dbManager.wayForComment(newPost);
//        dbManager.loadComments();

        //  progressBar.setVisibility(ProgressBar.INVISIBLE);
        commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().trim().length() > 0) {
                    sendCommentButton.setEnabled(true);
                } else {
                    sendCommentButton.setEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        commentEditText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(500)});
    }

    private View.OnClickListener onClickItem() {
        return view -> {
            if (view.getId() == R.id.sendCommentButton) {
                // String key = messagesDatabaseReference.push().getKey();
                Comment comment = new Comment();
                comment.setTextComment(commentEditText.getText().toString());
                //тут нужно получить этого юзера
                comment.setUserName(senderName);
                comment.setImageIdSender(senderPhoto);
                comment.setUidSender(senderUid);
                comment.setTime(String.valueOf(System.currentTimeMillis()));
                dbManager.createComment(comment, newPost);
                commentEditText.setText("");
            }
        };
    }

    private void buildRececlerView() { // создаем receclerView
        commentsListView = findViewById(R.id.commentsListView);
       commentsListView.addItemDecoration(new DividerItemDecoration(commentsListView.getContext(), DividerItemDecoration.VERTICAL));
         commentLayoutManager=new LinearLayoutManager(this);
        adapter = new CommentAdapter(commentsList);
        commentsListView.setLayoutManager(commentLayoutManager);
        commentsListView.setAdapter(adapter);
        adapter.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                goToActiviry(position);
            }


        });
    }

    private void goToActiviry(int position) {
        Intent intent = new Intent(CommentsActivity.this, PersonListActiviti.class);
        intent.putExtra("Uid", commentsList.get(position).getUidSender());
        FirebaseUser currentUser = mainAppClass.getCurrentUser();
        startActivity(intent);// тут еще нужна проверка подписчик ли это
    }

    private void attachUserDatabaseReferenceListener() {
        comentsDatabaseReference = databaseReferenceMain.child(newPost.getKey()).child("comments");
        if (comentsChildEvenListener == null) {
            comentsChildEvenListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    commentsList.add(comment);
                    adapter.notifyDataSetChanged();

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

            };
            comentsDatabaseReference.addChildEventListener(comentsChildEvenListener);
        }
    }

    @Override
    public void handleEvent(List<User> users) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            senderUid = user.getId();
            Log.d("MyLog","handle" +senderUid);
            senderName = user.getName();
            senderPhoto = user.getImageId();
//            login.setText(user.getName());
//            text.setText(user.getDescription());
//            nameBeforeChange = user.getName();
//            discBeforeChange = user.getDescription();
//            photoBeforeChange = user.getImageId();
//             Picasso.get().load(user.getImageId()).transform(new CircleTransform()).into(logo);
        }
        sendCommentButton.setOnClickListener(onClickItem());
        attachUserDatabaseReferenceListener();
        buildRececlerView();

    }

//    @Override
//    public void onCommentsLoadedd(List<Comment> comments) {
//        for (int i = 0; i < comments.size(); i++) {
//            commentsList.add(comments.get(i));
//            Log.d("MyLog","OnComments"+ comments.get(i));
//        }
//        Log.d("MyLog","OnCommentsSize"+ commentsList.size());
//        buildRececlerView();
    //adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        };
//       .child("messages").addChildEventListener(messegesChildEvenListener);
//    }
}
