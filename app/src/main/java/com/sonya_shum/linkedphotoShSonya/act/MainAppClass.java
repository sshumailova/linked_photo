package com.sonya_shum.linkedphotoShSonya.act;

import android.app.Application;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.sonya_shum.linkedphotoShSonya.db.DbManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class MainAppClass{
        //extends Application {
    private FirebaseDatabase db;
    private FirebaseStorage fs;
    private FirebaseAuth auth;
    private boolean isAdmin=false;
//

    public MainAppClass(){
        db = FirebaseDatabase.getInstance();
        fs = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public DatabaseReference getMainDbRef() {
        return db.getReference(DbManager.MAIN_ADS_PATH);
    }

    public DatabaseReference getUserDbRef() {
        return db.getReference(DbManager.USERS);
    }
    public DatabaseReference getChatDbRef(){
        return db.getReference(DbManager.CHATS);
    }

    public FirebaseDatabase getDb() {
        return db;
    }

    public FirebaseStorage getFs() {
        return fs;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }
public StorageReference getStorageRefChatImages(){
        return fs.getReference().child("chat_images");
}
    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    public  FirebaseUser getCurrentUser(){
        return auth.getCurrentUser();
    }
}
