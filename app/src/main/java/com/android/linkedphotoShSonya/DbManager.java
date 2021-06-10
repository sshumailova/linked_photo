package com.android.linkedphotoShSonya;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.linkedphotoShSonya.Adapter.DataSender;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DbManager {
    private Context context;
    private Query mQuery;
    private List<NewPost> newPostList;
    private DataSender dataSender;
    private  FirebaseDatabase db;
    private FirebaseStorage fs;
    private int cat_ads_counter=0;
    String text;
    private int deleteImageCounter=0;
    public DbManager(DataSender dataSender,Context context) {

        this.dataSender = dataSender;
        this.context = context;
        newPostList=new ArrayList<>();
        db = FirebaseDatabase.getInstance();
        fs=FirebaseStorage.getInstance();
    }

    public void getDataFromDb(String path) {
//if(newPostList.size()>0){newPostList.clear();}
        DatabaseReference dbRef = db.getReference(path);
        mQuery = dbRef.orderByChild("post/time");
readDataUpdate();
    }
    public void getMyDataFromDb(String uid,String path) {
        //int k=newPostList.size();
      if(newPostList.size()>0){newPostList.clear();}
        DatabaseReference dbRef = db.getReference(path);
        mQuery = dbRef.orderByChild("post/uid").equalTo(uid);
        readMyAdsDataUpdate(uid);
        int k=newPostList.size();
    }
public void deleteItem(final NewPost newPost){
    StorageReference sRef=null;
    switch (deleteImageCounter){
        case 0:
           if(!newPost.getImageId().equals("empty")){ sRef=fs.getReferenceFromUrl(newPost.getImageId());}// создаем ссылку которая указывает на нашу картинку
            else {deleteImageCounter++;
            deleteItem(newPost);};
            break;
        case 1:
            if(!newPost.getImageId2().equals("empty")){
            sRef=fs.getReferenceFromUrl(newPost.getImageId2());}// создаем ссылку которая указывает на нашу картинку
            else {deleteImageCounter++;
                deleteItem(newPost);};
            break;
        case 2:
            if(!newPost.getImageId3().equals("empty")){
                sRef=fs.getReferenceFromUrl(newPost.getImageId3());}// создаем ссылку которая указывает на нашу картинку
            else {
                deleteDBItem(newPost);
          sRef=null;}
    }
    if(sRef==null){return;}
    sRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void unused) {
            deleteImageCounter++;
            if(deleteImageCounter<3){
               deleteItem(newPost);
            }
            else {
                deleteDBItem(newPost);
               // sRef=null;
                deleteImageCounter=0;
            }
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull  Exception e) {
            Toast.makeText(context, "An error accurred,image not delete", Toast.LENGTH_SHORT).show();
        }
    });

}
public void updateTotalViews(final  NewPost newPost){
        DatabaseReference dRef=FirebaseDatabase.getInstance().getReference("notes");
        int total_views;
        try {
            total_views=Integer.parseInt(newPost.getTotal_views());

        }
        catch (NumberFormatException e){
            total_views=0;
        }
        total_views++;
        dRef.child(newPost.getKey()).child("post/total_views").setValue(String.valueOf(total_views));
}
    public void readDataUpdate() {
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(newPostList.size()>0){newPostList.clear();}
                                for(DataSnapshot ds:snapshot.getChildren()){
                    NewPost newPost=ds.child("post").getValue(NewPost.class);
                    newPostList.add(newPost);

                    //Log.d("MyLog","Text: "+ newPost.getDisc());
                }

                                dataSender.onDataRecived(newPostList);
                               // newPostList.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void deleteDBItem(NewPost newPost){
        DatabaseReference dbRef=db.getReference(newPost.getCat());
        dbRef.child(newPost.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, R.string.item_deleted, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                Toast.makeText(context, "post not delete", Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void readMyAdsDataUpdate(String uid ) {
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds:snapshot.getChildren()){
                    NewPost newPost=ds.child("post").getValue(NewPost.class);
                   // text=newPost.getDisc().toString();
                    newPostList.add(newPost);

                    //Log.d("MyLog","Text: "+ newPost.getDisc());
                }

               // readMyAdsDataUpdate(uid);
                dataSender.onDataRecived(newPostList);
              //  newPostList.clear();
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
        System.out.println(text);
    }}