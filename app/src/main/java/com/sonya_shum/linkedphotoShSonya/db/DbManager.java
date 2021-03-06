package com.sonya_shum.linkedphotoShSonya.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.sonya_shum.linkedphotoShSonya.Adapter.DataSender;
import com.sonya_shum.linkedphotoShSonya.Adapter.PostAdapter;
import com.sonya_shum.linkedphotoShSonya.Adapter.Subscribers;
import com.sonya_shum.linkedphotoShSonya.Observer;
import com.sonya_shum.linkedphotoShSonya.R;
import com.sonya_shum.linkedphotoShSonya.Status.StatusItem;
import com.sonya_shum.linkedphotoShSonya.act.MainAppClass;
import com.sonya_shum.linkedphotoShSonya.comments.Comment;
import com.sonya_shum.linkedphotoShSonya.dagger.App;
import com.sonya_shum.linkedphotoShSonya.utils.Comments;
import com.sonya_shum.linkedphotoShSonya.utils.MyConstants;
import com.sonya_shum.linkedphotoShSonya.utils.WayToChat;
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
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class DbManager {
    public static final String STATUS = "status";
    public static final String FILTER1 = "filter1";
    public static final String FILTER2 = "filter2";
    public static final String MAIN_ADS_PATH = "main_ads_path";
    public static final String USERS = "users";
    public static final String CHATS = "chats";
    public static final String MY_FAv_PATh = "my_fav";
    public static final String FAv_ADS_PATh = "fav_path";
    public static final String USER_FAV_ID = "iser_fav_id";
    public static final String ORDER_BY_CAT_NAME_TIME = "/cat_name_time";
    public static final String ORDER_BY_NAME_TIME = "/name_time";
    public static final String TOTAL_VIEWS = "/status/status/totalViews";
    public static final String VISIBILITY = "visibility";
    public static final String ACCEPTED = "accepted";
    public static final String DECLINED = "declined";
    private Subscribers subscribesrsInt;
    private Context context;
    private Query mQuery;;
    private Query mQueryUser;
    private List<NewPost> newPostList;
    private DataSender dataSender;
    private Observer observer;
    private WayToChat wayToChat;
    private Comments comments;
private MainAppClass mainAppClass;
    private long cat_ads_counter = 0;
    String text;
    private DatabaseReference mainNode;
    private DatabaseReference users;
    private DatabaseReference chats;
    private String filter;
    private String orderByFilter;
    private int deleteImageCounter = 0;
    private String searchText = "";
    public List<User> usersList;// ???????????? ???????????? ?????????????? ???????? ?? ????
    public List<String> listSubscribes;// ???????????? ??????????????????????
    public List<Observer> subscribes;
    private boolean One = false;
    private DatabaseReference messagesDatabaseReference;
    public List<String> MyChatUsers;


    public DbManager(Context context) {
        if (context instanceof DataSender) {
            this.dataSender = (DataSender) context;
        }
        if (context instanceof Observer) {
            this.observer = (Observer) context;
        }
        if (context instanceof WayToChat) {
            this.wayToChat = (WayToChat) context;
        }
        if (context instanceof Comments) {
            this.comments = (Comments) context;
        }
//        if (context instanceof Subscribers) {
//            this.subscribesInt = (Subscribers) context;
//        }
        this.context = context;
        newPostList = new ArrayList<>();
        //((App)getApplication()).getComponent().inject(this);
        mainAppClass =new MainAppClass();
        mainNode = mainAppClass.getMainDbRef();
        users = mainAppClass.getUserDbRef();
        chats = mainAppClass.getChatDbRef();

        usersList = new ArrayList<>();
        listSubscribes = new ArrayList<>();
        subscribes = new ArrayList<>();
        MyChatUsers = new ArrayList<>();

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
        readDataUpdate();// ???? ???????????? readDataUpdate(MyConstans.DIF_CAT)
    }

    public void changeMyNotes(String orderBy) {
        mQuery = mainNode.orderByChild(orderBy).equalTo(mainAppClass.getAuth().getUid());
    }

    public void getMySubscription(String orderBy) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mQuery = users.child(currentUser.getUid()).child("subscriptions").child("userUid");
        //readSubscription();
    }

    public void getCurrentUser(String uid) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mQueryUser = users.child(uid);
        if (mQueryUser != null) {
            One = true;
        }
    }

    public void getAllOwnerAds(String uid) {
        mQuery = mainNode.orderByChild(uid + "/post/uid").equalTo(uid);
        readDataUpdate();// ???? ???????????? readDataUpdate(MyConstans.DIF_CAT)
    }

    public void getAdminAds() {
        mQuery = mainNode.orderByChild("status/status/visibility").equalTo("waiting");
        readDataUpdate();// ???? ???????????? readDataUpdate(MyConstans.DIF_CAT)

    }

    public void getDataFromDb(String cat, String lastTitleTime) {
        if (mainAppClass.getAuth().getUid() == null) {
            return;
        }
        String filterToUse = getFilterToUse();
        String orderBy = getOrderBy(cat, filterToUse);
        cat += "_";
        cat = useCategory(orderBy, filterToUse, cat);
        orderBy = getOrderByFilter(orderBy, cat, filterToUse);
        Log.d("MyLog", "Filter by : " + cat + filter + lastTitleTime);
        String name = searchText;
        if (!lastTitleTime.isEmpty()) {
            name = "";
            lastTitleTime = searchText.isEmpty() ? lastTitleTime.split("_")[1] : lastTitleTime;
        }
        mQuery = mainNode.orderByChild(orderBy).startAt(cat + filter + searchText).endAt(cat + filter + name + lastTitleTime + "\uf8ff").limitToLast(MyConstants.ADS_LIMIT);

        readDataUpdate();
    }

    private String getFilterToUse() {
        boolean a = searchText.isEmpty();
        String s = searchText;
        Log.d("MyLog", " searchT " + a + " Sea " + s + "ddd");
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

    private String getOrderByFilter(String orderBy, String cat, String filterToUse) {
        if (!orderByFilter.isEmpty()) {
            return (cat.isEmpty()) ? filterToUse + "/" + orderByFilter : filterToUse + "/cat_" + orderByFilter;
        } else {
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

    public void creatUser(FirebaseUser firebaseUser, String name) {
        User user = new User();
        String key = FirebaseDatabase.getInstance().getReference().push().getKey();
        user.setKey(key);
        user.setId(firebaseUser.getUid());
        user.setName(name);
        mainAppClass.getMainDbRef().child(key).setValue(user);

    }

    public void deleteItem(final NewPost newPost, ResultListener resultListener) {
        StorageReference sRef = null;
        switch (deleteImageCounter) {
            case 0:
                if (!newPost.getImageId().equals("empty")) {
                    sRef = mainAppClass.getFs().getReferenceFromUrl(newPost.getImageId());
                }// ?????????????? ???????????? ?????????????? ?????????????????? ???? ???????? ????????????????
                else {
                    deleteImageCounter++;
                    deleteItem(newPost,resultListener);
                }
                ;
                break;
            case 1:
                if (!newPost.getImageId2().equals("empty")) {
                    sRef = mainAppClass.getFs().getReferenceFromUrl(newPost.getImageId2());
                }// ?????????????? ???????????? ?????????????? ?????????????????? ???? ???????? ????????????????
                else {
                    deleteImageCounter++;
                    deleteItem(newPost,resultListener);
                }
                ;
                break;
            case 2:
                if (!newPost.getImageId3().equals("empty")) {
                    sRef = mainAppClass.getFs().getReferenceFromUrl(newPost.getImageId3());
                }// ?????????????? ???????????? ?????????????? ?????????????????? ???? ???????? ????????????????
                else {
                    deleteDBItem(newPost,resultListener);
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
                    deleteItem(newPost,resultListener);
                } else {
                    deleteDBItem(newPost,resultListener);
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

    private void deleteDBItem(NewPost newPost,ResultListener resultListener) {
        DatabaseReference dbRef = mainAppClass.getDb().getReference(DbManager.MAIN_ADS_PATH);
        dbRef.child(newPost.getKey()).child(STATUS).removeValue();
        if (mainAppClass.getAuth().getUid() == null) {
            return;
        }
        dbRef.child(newPost.getKey()).child(mainAppClass.getAuth().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                resultListener.onResult(true);
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
//            Toast.makeText(context, "???????? ????????????????????????????????????!", Toast.LENGTH_SHORT).show();
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

    public void updateTotalCounter(final String counterPath, String key, String counter) {// ?????????? ???????????????? ???????????????????? ?????? ??????????????????
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
                            newPost.setCommCounter(ds.child("comments").getChildrenCount());
                        }
                        if (favUid != null && newPost != null) {
                            newPost.setFav(true);


                        }
                    }
                    if (newPost != null && statusItem != null) {
                        newPost.setTotal_views(statusItem.totalViews);
                        String visibility=(newPost.getUid().equals(mainAppClass.getAuth().getUid())) ? ACCEPTED : statusItem.visibility;
                        newPost.setVisibility(visibility);
                            newPostList.add(newPost);
                    }
                }
   dataSender.onDataRecived(newPostList); //?? ???????? ???? 119 ?????????? ???????? ??????!
                 //newPostList.clear();
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

    public void updateFieldSubcrib(List<NewPost> list, final PostAdapter.ViewHolderData holder, String uid) {
        for (int i = 0; i < list.size(); i++) {
            NewPost newPost = list.get(i);
            if (newPost.getUid().equals(uid)) {
                holder.binding.AddSub.setVisibility(View.GONE);
            }

        }
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

    public String getMySubc() {
        return "subscriptions" + "/" + mainAppClass.getAuth().getUid() + "/" + "userUid: ";
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public List<User> ListOfUsers() {
        mQueryUser = users.child("users");
//        fillingUserList();
        return usersList;
    }

    public void loadAllUsers(Observer observer) {
        if (One == true) {
            mQueryUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<User> users = new ArrayList<>();
                    if (usersList.size() > 0) {
                        usersList.clear();
                    }
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        users.add(user);
                    }

                    observer.handleEvent(users);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
//             });
            });
            One = false;
        } else {
            users.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<User> users = new ArrayList<>();
                    if (usersList.size() > 0) {
                        usersList.clear();
                    }
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.child("user").getValue(User.class);
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
    }


    public void loadFollowers() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        List<User> usersSub = new ArrayList<>();
        mQuery = users.orderByChild("subscriptions" + "/" + currentUser.getUid() + "/" + "userUid").equalTo(currentUser.getUid());
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.child("user").getValue(User.class);
                    usersSub.add(user);

                }
                observer.handleEvent(usersSub);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void loadComments() {
        List<Comment> commentsUSer = new ArrayList<>();
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Comment comment = ds.getValue(Comment.class);
                    commentsUSer.add(comment);
                }
                comments.onCommentsLoadedd(commentsUSer);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void loadSubscription(List<String> listSubscribes) {
        List<User> usersSub = new ArrayList<>();
        for (int i = 0; i < listSubscribes.size(); i++) {
            int finalI = i;
            users.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.child("user").getValue(User.class);
                        if (user.getId().equals(listSubscribes.get(finalI))) {
                            Log.d("MyLog", "user " + user.getId() + " subsc" + listSubscribes.get(finalI));
                            usersSub.add(user);
                            break;

                        }
                    }
                    if (finalI == listSubscribes.size() - 1) {
                        Log.d("MyLog", "userSubSize" + usersSub.size());
                        observer.handleEvent(usersSub);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
//
            });
        }

        // observer.handleEvent(usersSub);
    }

    public void readSubscription() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            users.child(currentUser.getUid()).child("subscriptions").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (listSubscribes.size() > 0) {
                        listSubscribes.clear();
                    }
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String uidSubc = (String) ds.child("userUid").getValue().toString();
                        // String favUid = (String) ds.child(FAv_ADS_PATh).child(uid).child(USER_FAV_ID).getValue();
                        listSubscribes.add(uidSubc);
                        Log.d("MyLog", "Data sub size" + snapshot.getChildrenCount() + uidSubc);
                    }
                    subscribesrsInt.onDataSubcRecived(listSubscribes); // ???? ?????????? ???????????????????? ??????????????
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }


    public boolean IsUserInDb(String email) {
        usersList = ListOfUsers();
        for (int i = 0; i < usersList.size(); i++) {
            if (usersList.get(i).getEmail().equals(email)) {
                return true;// ???????? ?? ???????????? ???????????? ???????? ?????????? ?? ?????????????? ?????????? ???????????? - ???? ?????????????????? true
            }
        }
        return false;
    }

    public void addUser(User user) {
        this.usersList.add(user);
        mainAppClass.getUserDbRef().child(user.getId()).child("user").setValue(user);
//mainAppClass.getFs().getReference("ImagesUserLogo")
    }

    public void removeUser(User user) {
        this.usersList.remove(user);
        //?????? ?????? ???? ???????? ?????????????? ???????????????? ???? ???????? ????????????
    }

    public void addObserver(Observer observer) {
        this.subscribes.add(observer);
    }

    public interface ResultListener {
        void onResult(boolean result);
    }

    public void setOnSubscriptions(Subscribers subscribersInt) {
        this.subscribesrsInt = subscribersInt;
    }

    public void AddSubscription(String uid) {
        // readSubscription();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
//            mainAppClass.getUserDbRef()
            FirebaseDatabase.getInstance().getReference(DbManager.USERS).child(currentUser.getUid()).child("subscriptions").child(uid).child("userUid").setValue(uid);
        }

    }

    public void removeSubscription(String uid) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
//            mainAppClass.getUserDbRef()
            FirebaseDatabase.getInstance().getReference(DbManager.USERS).child(currentUser.getUid()).child("subscriptions").child(uid).removeValue();
//            {
//                @Override
//                public void onComplete(@NonNull @NotNull Task<Void> task) {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(context, "Subscription remove!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }););
        }
    }

    //    public boolean isSubscription(String uid) {
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        mQuery = users.child(currentUser.getUid()).child("subscriptions");
//        readSubscription();
//        if (listSubscribes.size() == 0) {
//            return false;
//        }
//        for(int i=0;i<listSubscribes.size();i++)
//        {
//            Log.d("MyLog" , "subUid "+ listSubscribes.get(i));
//        }
//        if (listSubscribes.contains(uid)) {
//            return true;
//        }
//        return false;
//    }
    public void findOrCreateReference(String sender, String recipientUserId) {
        Log.d("MyLog", "sender " + sender);// ?????? ???????????? ???????? ???? ?? ???????? ?????????? ???????? ???????? ?????? - ????????????
        Log.d("MyLog", "recipient " + recipientUserId);
        String way1 = sender + "_" + recipientUserId;// ?????? ???????????? ???????? ???? ?? ???????? ?????????? ???????? ???????? ?????? - ????????????
        String way2 = recipientUserId + "_" + sender;// ?????? ???????????? ???????? ???? ?? ???????? ?????????? ???????? ???????? ?????? - ????????????
        //DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference(DbManager.CHATS);
        chats.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child(way1).exists()) {
                    messagesDatabaseReference = chats.child(way1);
                    Log.d("MyLog", "mesDR " + "sender OK");
                } else if (snapshot.child(way2).exists()) {
                    messagesDatabaseReference = chats.child(way2);
                    Log.d("MyLog", "mesDR " + "recip OK");
                } else {
                    chats.child((sender + "/" + recipientUserId).toString());
                    messagesDatabaseReference = chats.child(sender + "_" + recipientUserId);
                    //messagesDatabaseReference.setValue("messages");
                    Log.d("MyLog", "mesDR " + messagesDatabaseReference);
                }
                wayToChat.way(messagesDatabaseReference);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void createComment(Comment comment, NewPost newPost) {
        Log.d("MyLog", "comWay " + FirebaseDatabase.getInstance().getReference(DbManager.MAIN_ADS_PATH).child(newPost.getKey()).child("comments"));
        FirebaseDatabase.getInstance().getReference(DbManager.MAIN_ADS_PATH).child(newPost.getKey()).child("comments").child(comment.getTime()).setValue(comment);

    }

    public void wayForComment(NewPost newPost) {
        mQuery = FirebaseDatabase.getInstance().getReference(DbManager.MAIN_ADS_PATH).child(newPost.getKey()).child("comments");
    }

    public void receiveMyChatsUsers(String uid) {// ?????? ?????????????? uid ???????????? ?? ???????????????? ?? ???????? ??????, ?????????????????? ?????????????????? subscribers ?????? ???? ???????????????? list<string> uid ????????????
        chats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (MyChatUsers.size() > 0) {
                    MyChatUsers.clear();
                }
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String way = ds.getKey().toString();
                    String[] retval = way.split("_", 2);
                    String sender = retval[0];
                    String recipientUserId = retval[1];
                    if (sender.equals(uid)) {
                        MyChatUsers.add(recipientUserId);
                    } else if (recipientUserId.equals(uid)) {
                        MyChatUsers.add(sender);
                    }
                }
//                if (MyChatUsers.size()== 0) {
//                    MyChatUsers.add("empty");}
                subscribesrsInt.onDataSubcRecived(MyChatUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
//    public DatabaseReference getMessagesDatabaseReference(String sender, String recipientUserId){
//        findOrCreateReference(sender,recipientUserId);
//        Log.d("MyLog", "getMess " + messagesDatabaseReference);
//        return messagesDatabaseReference;
//    }

        });
    }
}