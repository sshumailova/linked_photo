package com.android.linkedphotoShSonya.db;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.linkedphotoShSonya.Adapter.DataSender;
import com.android.linkedphotoShSonya.Adapter.PostAdapter;
import com.android.linkedphotoShSonya.MainActivity;
import com.android.linkedphotoShSonya.Observed;
import com.android.linkedphotoShSonya.Observer;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.Status.StatusItem;
import com.android.linkedphotoShSonya.act.EditActivity;
import com.android.linkedphotoShSonya.act.MainAppClass;
import com.android.linkedphotoShSonya.act.PersonListActiviti;
import com.android.linkedphotoShSonya.utils.MyConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DbManager {
    public static final String STATUS = "status";
    public static final String FILTER1 = "filter1";
    public static final String FILTER2 = "filter2";
    public static final String MAIN_ADS_PATH = "main_ads_path";
    public static final String USERS = "users";
    public static final String MY_FAv_PATh = "my_fav";
    public static final String FAv_ADS_PATh = "fav_path";
    public static final String USER_FAV_ID = "iser_fav_id";
    public static final String ORDER_BY_CAT_NAME_TIME = "/cat_name_time";
    public static final String ORDER_BY_NAME_TIME = "/name_time";
    public static final String TOTAL_VIEWS = "/status/status/totalViews";
    public static final String VISIBILITY = "visibility";
    public static final String ACCEPTED = "accepted";
    public static final String DECLINED = "declined";
    private Context context;
    private Query mQuery;
    private Query mQueryUser;
    private List<NewPost> newPostList;
    private DataSender dataSender;
    private Observer observer;
    private MainAppClass mainAppClass;
    private long cat_ads_counter = 0;
    String text;
    private DatabaseReference mainNode;
    private DatabaseReference users;
    private String filter;
    private String orderByFilter;
    private int deleteImageCounter = 0;
    private String searchText = "";
    public List<User> usersList;// список юзеров которые есть в бд
    public List<Observer> subscribes;// список подписчиков


    public DbManager(Context context) {
        if (context instanceof DataSender) {
            this.dataSender = (DataSender) context;
        }
        if (context instanceof Observer) {
            this.observer = (Observer) context;
        }
        this.context = context;
        newPostList = new ArrayList<>();
        mainAppClass = ((MainAppClass) context.getApplicationContext());
        mainNode = mainAppClass.getMainDbRef();
        users = mainAppClass.getUserDbRef();
        usersList = new ArrayList<>();
        subscribes = new ArrayList<>();

    }

    public void onResume(SharedPreferences preferences) {
        filter = preferences.getString(MyConstants.FILTER, "");
        orderByFilter = preferences.getString(MyConstants.ORDER_BY_FILTER, "");
    }

    public void isAdmin(ResultListener listener) {
        if (mainAppClass.getAuth().getUid() == null) {
            return;
        }
        DatabaseReference adminRef = mainAppClass.getDb().getReference(mainAppClass.getAuth().getUid());
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mainAppClass.setAdmin(snapshot.hasChildren());
                listener.onResult(snapshot.hasChildren());
                Log.d("MyLog", "Is Admin: " + snapshot.hasChildren());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mainAppClass.setAdmin(false);
            listener.onResult(false);
            }
        });
    }

    public void clearFilter() {
        filter = "";
        orderByFilter = "";
    }

    public void getMyAds(String orderBy) {
        mQuery = mainNode.orderByChild(orderBy).equalTo(mainAppClass.getAuth().getUid());
        readDataUpdate();// он делает readDataUpdate(MyConstans.DIF_CAT)
    }
    public void getAllOwnerAds(String uid) {
        mQuery = mainNode.orderByChild(uid+ "/post/uid").equalTo(uid);
        readDataUpdate();// он делает readDataUpdate(MyConstans.DIF_CAT)
    }
    public void getAdminAds() {
        mQuery = mainNode.orderByChild("status/status/visibility").equalTo("waiting");
        readDataUpdate();// он делает readDataUpdate(MyConstans.DIF_CAT)

    }

    public void getDataFromDb(String cat, String lastTitleTime) {
        if (mainAppClass.getAuth().getUid() == null) {
            return;
        }
        String filterToUse = getFilterToUse();
        String orderBy = getOrderBy(cat, filterToUse);
        cat += "_";
        cat = useCategory(orderBy, filterToUse, cat);
        orderBy= getOrderByFilter(orderBy,cat,filterToUse);
        Log.d("MyLog", "Filter by : " + cat + filter + lastTitleTime);
        String name = searchText;
        if (!lastTitleTime.isEmpty()) {
            name = "";
            lastTitleTime=searchText.isEmpty()? lastTitleTime.split("_")[1]: lastTitleTime;
        }
        mQuery = mainNode.orderByChild(orderBy).startAt(cat + filter + searchText).endAt(cat + filter + name + lastTitleTime + "\uf8ff").limitToLast(MyConstants.ADS_LIMIT);

        readDataUpdate();
    }

    private String getFilterToUse() {
        return searchText.isEmpty() ? STATUS + "/" + FILTER2 : "/" + FILTER1;
    }

    private String getOrderBy(String cat, String filterToUse) {
        return (cat.equals(MyConstants.ALL_PHOTOS)) ? filterToUse + ORDER_BY_NAME_TIME : filterToUse + ORDER_BY_CAT_NAME_TIME;
    }

    private String useCategory(String orderBy, String filterToUse, String cat) {
        if (orderBy.equals(filterToUse + ORDER_BY_NAME_TIME)) {
            return "";
        } else {
            return cat;
        }
    }
    private String getOrderByFilter(String orderBy,String cat,String filterToUse){
        if (!orderByFilter.isEmpty()) {
            return  (cat.isEmpty()) ? filterToUse + "/" + orderByFilter : filterToUse + "/cat_" + orderByFilter;
        }
        else {
            return orderBy;
        }
    }

    public void setAdVisibility(String key, String visibility, ResultListener resultListener) {
        DatabaseReference dRef = mainAppClass.getMainDbRef();
        dRef.child(key).child(STATUS).child(STATUS).child(VISIBILITY).setValue(visibility).addOnCompleteListener(task -> {
            if (task.isSuccessful()) resultListener.onResult(true);

        });

    }
//    public void getSearchResult(String searchText) {
//        if (mAuth.getUid() == null) {
//            return;
//        }
//        DatabaseReference dbRef = db.getReference(MAIN_ADS_PATH);
//        mQuery = dbRef.orderByChild("/status/" + orderByFilter).startAt(filter + searchText).endAt(filter + searchText + "\uf8ff").limitToLast(MyConstants.ADS_LIMIT);
//        readDataUpdate();
//
//    }

    //    public void creatUser(FirebaseUser firebaseUser, String name) {
//        User user = new User();
//        String key = FirebaseDatabase.getInstance().getReference().push().getKey();
//        user.setKey(key);
//        user.setId(firebaseUser.getUid());
//        user.setName(name);
//          mainAppClass.getMainDbRef().child(key).setValue(user);
//
//    }
    public void deleteItem(final NewPost newPost) {
        StorageReference sRef = null;
        switch (deleteImageCounter) {
            case 0:
                if (!newPost.getImageId().equals("empty")) {
                    sRef = mainAppClass.getFs().getReferenceFromUrl(newPost.getImageId());
                }// создаем ссылку которая указывает на нашу картинку
                else {
                    deleteImageCounter++;
                    deleteItem(newPost);
                }
                ;
                break;
            case 1:
                if (!newPost.getImageId2().equals("empty")) {
                    sRef = mainAppClass.getFs().getReferenceFromUrl(newPost.getImageId2());
                }// создаем ссылку которая указывает на нашу картинку
                else {
                    deleteImageCounter++;
                    deleteItem(newPost);
                }
                ;
                break;
            case 2:
                if (!newPost.getImageId3().equals("empty")) {
                    sRef = mainAppClass.getFs().getReferenceFromUrl(newPost.getImageId3());
                }// создаем ссылку которая указывает на нашу картинку
                else {
                    deleteDBItem(newPost);
                    sRef = null;
                }
        }
        if (sRef == null) {
            return;
        }
        sRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                deleteImageCounter++;
                if (deleteImageCounter < 3) {
                    deleteItem(newPost);
                } else {
                    deleteDBItem(newPost);
                    // sRef=null;
                    deleteImageCounter = 0;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "An error accurred,image not delete", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteDBItem(NewPost newPost) {
        DatabaseReference dbRef = mainAppClass.getDb().getReference(DbManager.MAIN_ADS_PATH);
        dbRef.child(newPost.getKey()).child(STATUS).removeValue();
        if (mainAppClass.getAuth().getUid() == null) {
            return;
        }
        dbRef.child(newPost.getKey()).child(mainAppClass.getAuth().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, R.string.item_deleted, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "post not delete", Toast.LENGTH_SHORT).show();
            }
        });

    }

//    public void getMyDataFromDb(String uid, String path) {
//        if (mAuth.getUid() == null) {
//            Toast.makeText(context, "НАдо зарегистрироваться!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (newPostList.size() > 0) {
//            newPostList.clear();
//        }
//        DatabaseReference dbRef = db.getReference(MAIN_ADS_PATH);
//        mQuery = dbRef.orderByChild(mAuth.getUid() + "/post/uid").equalTo(uid);
//        readMyAdsDataUpdate();
//        int k = newPostList.size();
//    }

    public void updateTotalCounter(final String counterPath, String key, String counter) {// когда открываю объявление для просмотра
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference(MAIN_ADS_PATH);
        int totalCounter;
        try {
            totalCounter = Integer.parseInt(counter);

        } catch (NumberFormatException e) {
            totalCounter = 0;
        }
        totalCounter++;

        dRef.child(key).child(counterPath).setValue(String.valueOf(totalCounter));
    }

    public void readDataUpdate() {
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (newPostList.size() > 0) {
                    newPostList.clear();
                }
                for (DataSnapshot ds : snapshot.getChildren()) {
                    NewPost newPost = null;
                    for (DataSnapshot ds2 : ds.getChildren()) {
                        if (newPost == null) newPost = ds2.child("post").getValue(NewPost.class);
                    }

                    StatusItem statusItem = ds.child(STATUS).child(STATUS).getValue(StatusItem.class);
                    String uid = mainAppClass.getAuth().getUid();
                    if (uid != null) {
                        String favUid = (String) ds.child(FAv_ADS_PATh).child(uid).child(USER_FAV_ID).getValue();
                        if (newPost != null) {
                            newPost.setFavCounter(ds.child(FAv_ADS_PATh).getChildrenCount());

                        }
                        if (favUid != null && newPost != null) {
                            newPost.setFav(true);


                        }
                    }
                    if (newPost != null && statusItem != null) {
                        newPost.setTotal_views(statusItem.totalViews);
                        if (statusItem.visibility.equals(ACCEPTED) || newPost.getUid().equals(mainAppClass.getAuth().getUid()) || mainAppClass.isAdmin()) {
                            newPostList.add(newPost);
                        }

                    }
                }
                dataSender.onDataRecived(newPostList); //у меня до 119 урока было это!
                // newPostList.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    public void readMyAdsDataUpdate() {
//        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    NewPost newPost = ds.child(mAuth.getUid() + "/post").getValue(NewPost.class);
//                    StatusItem statusItem = ds.child("status").getValue(StatusItem.class);
//                    if (newPost != null && statusItem != null) {
//                        newPost.setTotal_views(statusItem.totalViews);
//                    }
//                    newPostList.add(newPost);
//
//
//                }
//
//                // readMyAdsDataUpdate(uid);
//                dataSender.onDataRecived(newPostList);
//                //  newPostList.clear();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//        System.out.println(text);
//    }

    public void updateFav(final NewPost newPost, PostAdapter.ViewHolderData holder) {

        if (newPost.isFav()) {
            deleteFav(newPost, holder);
        } else {
            addFav(newPost, holder);
        }
    }

    public void addFav(NewPost newPost, final PostAdapter.ViewHolderData holder) {
        if (mainAppClass.getAuth().getUid() == null) {
            return;
        }
        DatabaseReference dRef = mainAppClass.getMainDbRef();
        dRef.child(newPost.getKey()).child(FAv_ADS_PATh).child(mainAppClass.getAuth().getUid()).child(USER_FAV_ID).
                setValue(mainAppClass.getAuth().getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {
                    holder.binding.imFav.setImageResource(R.drawable.ic_fav_selected);
                    newPost.setFav(true);

                }
            }
        });
    }

    public void deleteFav(NewPost newPost, final PostAdapter.ViewHolderData holder) {
        if (mainAppClass.getAuth().getUid() == null) {
            return;
        }
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference(MAIN_ADS_PATH);
        dRef.child(newPost.getKey()).child(FAv_ADS_PATh).child(mainAppClass.getAuth().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {
                    holder.binding.imFav.setImageResource(R.drawable.ic_fav_not_selected);
                    newPost.setFav(false);
                }
            }
        });
    }

    public String getMyAdsNode() {
        return mainAppClass.getAuth().getUid() + "/post/uid";
    }

    public String getMyFavAdsNode() {
        return FAv_ADS_PATh + "/" + mainAppClass.getAuth().getUid() + "/" + USER_FAV_ID;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    //    public void addUser(FirebaseUser currentUser) {
//      //  FirebaseUser currentUser = mainAppClass.getAuth().getCurrentUser();
//        mainAppClass.getUserDbRef().addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    User user = null;
//                    user = ds.getValue(User.class);
//                    if (user.getId().equals(currentUser.getUid())) {
//                        break;
//                    }
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    public List<User> ListOfUsers() {
        mQueryUser = users;
//        fillingUserList();
        return usersList;
    }

    public void loadAllUsers(Observer observer) {
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> users = new ArrayList<>();
//                if (usersList.size() > 0) {
//                    usersList.clear();
//                }
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    users.add(user);
                }

                observer.handleEvent(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
//
        });
    }

    public boolean IsUserInDb(String email) {
        usersList = ListOfUsers();
        for (int i = 0; i < usersList.size(); i++) {
            if (usersList.get(i).getEmail().equals(email)) {
                return true;// если в списке юзеров есть емайл с которым заожу сейчас - то возвращаю true
            }
        }
        return false;
    }

    public void addUser(User user) {
        this.usersList.add(user);
        mainAppClass.getUserDbRef().child(user.getId()).setValue(user);
//mainAppClass.getFs().getReference("ImagesUserLogo")
    }

    public void removeUser(User user) {
        this.usersList.remove(user);
        //тут так же надо сдлеать удаление из базы данных
    }

    public void addObserver(Observer observer) {
        this.subscribes.add(observer);
    }

    public interface ResultListener {
        void onResult(boolean result);
    }
}