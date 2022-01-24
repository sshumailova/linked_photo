package com.sonya_shum.linkedphotoShSonya.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sonya_shum.linkedphotoShSonya.R;
import com.sonya_shum.linkedphotoShSonya.db.DbManager;
import com.sonya_shum.linkedphotoShSonya.utils.CircleTransform;
import com.sonya_shum.linkedphotoShSonya.utils.WayToChat;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements WayToChat {
    private ListView messageListView;
    private AwesomeMessageAdapter adapter;
    private ProgressBar progressBar;
    private ImageButton sendImageButton;
    private Button sendMessageButton;
    private EditText messageEditText;
private DatabaseReference commentsDatabaseReference;
    private String recipientUserId;
    private String sender;
    private String recipientUserName;
    private static final int RC_IMAGE_PICKER = 123;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference messagesDatabaseReference;
    private ChildEventListener messegesChildEvenListener;
    private DatabaseReference usersDatabaseReference;
    private ChildEventListener usersChildEvenListener;
    private FirebaseStorage storage;
    private DbManager dbManager;
    private StorageReference chatImagesStorageRefence;
    private Uri downloadUri =null;
    private Uri selectesImageUri;
    private String PhotoRecipient;
    private ImageView imageRecipient;
    private String recipientName;
    private TextView recipientNameTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        auth = FirebaseAuth.getInstance();
        init();
    }

    public void init() {
        Intent intent = getIntent();
        if (intent != null) {
            sender = intent.getStringExtra("sender");
            recipientUserId = intent.getStringExtra("recipientUserId");
            recipientUserName = intent.getStringExtra("recipientUserName");
            recipientName=intent.getStringExtra("recipientUserName");
          PhotoRecipient=intent.getStringExtra("userPhoto");

        }
        dbManager = new DbManager(this);
        setTitle("Chat with " + recipientUserName);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
      // dbManager.findOrCreateReference(sender, recipientUserId);
        dbManager.findOrCreateReference(sender,recipientUserId);
        //messagesDatabaseReference= database.getReference(DbManager.CHATS);
        usersDatabaseReference = database.getReference(DbManager.USERS);
        chatImagesStorageRefence = storage.getReference().child("chat_images");
        progressBar = findViewById(R.id.progressBar);
        sendImageButton = findViewById(R.id.sendPhotoButton);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messageEditText = findViewById(R.id.messageEditText);
        messageListView = findViewById(R.id.messageListView);
        imageRecipient=findViewById(R.id.UserPhotoChat);
        recipientNameTv=findViewById(R.id.tvEmailChat);
        recipientNameTv.setText(recipientName);
       Picasso.get().load(PhotoRecipient).transform(new CircleTransform()).into(imageRecipient);

        List<AwesomeMessage> awesomeMessages = new ArrayList<>();
        adapter = new AwesomeMessageAdapter(this, R.layout.message_item, awesomeMessages);
        messageListView.setAdapter(adapter);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().trim().length() > 0) {
                    sendMessageButton.setEnabled(true);
                } else {
                    sendMessageButton.setEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        messageEditText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(500)});
        sendMessageButton.setOnClickListener(onClickItem());
        sendImageButton.setOnClickListener(onClickItem());
//
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_IMAGE_PICKER && resultCode == RESULT_OK) {
            Uri selectesImageUri = data.getData();
            final StorageReference imageReference = chatImagesStorageRefence.child(selectesImageUri.getLastPathSegment());
            UploadTask uploadTask = imageReference.putFile(selectesImageUri);
            uploadTask = imageReference.putFile(selectesImageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {

                        downloadUri = task.getResult();
                        Log.d("MyLog", "OnActivityDowl" +downloadUri);
//                        AwesomeMessage message = new AwesomeMessage();
//                        message.setImageUrl(downloadUri.toString());
//                        message.setName(sender);
//                        message.setText(messageEditText.getText().toString());
//                        message.setSender(auth.getCurrentUser().getUid());
//                        message.setRecipient(recipientUserId);
//                        message.setTime(String.valueOf(System.currentTimeMillis()));
//                        messagesDatabaseReference.child("messages").child(message.getTime()).setValue(message);
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }
    }

    private View.OnClickListener onClickItem() {
        return view -> {
            if (view.getId() == R.id.sendMessageButton) {
               // String key = messagesDatabaseReference.push().getKey();
                AwesomeMessage message = new AwesomeMessage();
                message.setText(messageEditText.getText().toString());
                message.setName(sender);
                Log.d("MyLog", "downLoad"+ downloadUri);
                if(downloadUri==null){
                    message.setImageUrl(null);
                }else {
                message.setImageUrl(downloadUri.toString());
                    downloadUri=null;}
                message.setSender(auth.getCurrentUser().getUid());
                message.setRecipient(recipientUserId);
                message.setTime(String.valueOf(System.currentTimeMillis()));
                Log.d("MyLog", "Mess " + messagesDatabaseReference);
                messagesDatabaseReference.child("messages").child(message.getTime()).setValue(message);
                //StorageReference mRef = chatImagesStorageRefence.child(System.currentTimeMillis() + "my_Image");

                Log.d("MyLog", " Messages" + " message text: " + message.getText() + ", Name " + message.getName() + ", messageSender " + message.getSender() + ",messageRecipient " +
                        message.getRecipient());
                messageEditText.setText("");


            }
            if (view.getId() == R.id.sendPhotoButton) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Choose an image"), RC_IMAGE_PICKER);

            }
        };
    }


    @Override
    public void way(DatabaseReference way) {
        Log.d("MyLog", "WAY " + way);
        messagesDatabaseReference=way;
        messegesChildEvenListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                AwesomeMessage message = dataSnapshot.getValue(AwesomeMessage.class);
//                message.setText(s);
                if (message.getSender().equals(auth.getCurrentUser().getUid()) && message.getRecipient().equals(recipientUserId)) {
                    message.setMine(true);
                    adapter.add(message);
                } else if (message.getRecipient().equals(auth.getCurrentUser().getUid()) && message.getSender().equals(recipientUserId)) {
                    message.setMine(false);
                    adapter.add(message);
                }
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
        }

        ;
        Log.d("MyLog","MessDb" + messagesDatabaseReference);
        messagesDatabaseReference.child("messages").addChildEventListener(messegesChildEvenListener);
    }
}
