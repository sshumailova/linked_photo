package com.sonya_shum.linkedphotoShSonya.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sonya_shum.linkedphotoShSonya.comments.CommentsActivity;
import com.sonya_shum.linkedphotoShSonya.act.PersonListActiviti;
import com.sonya_shum.linkedphotoShSonya.databinding.ItemAdsBinding;
import com.sonya_shum.linkedphotoShSonya.db.DbManager;
import com.sonya_shum.linkedphotoShSonya.act.EditActivity;
import com.sonya_shum.linkedphotoShSonya.MainActivity;
import com.sonya_shum.linkedphotoShSonya.db.NewPost;
import com.sonya_shum.linkedphotoShSonya.R;
import com.sonya_shum.linkedphotoShSonya.act.ShowLayoutActivity;
import com.sonya_shum.linkedphotoShSonya.utils.CircleTransform;
import com.sonya_shum.linkedphotoShSonya.utils.MyConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolderData> implements Subscribers {
    public static final String TAG = "MyLog";
    public static final String NEXT_PAGE = "nextPage";
    private List<NewPost> mainPostList;
    private Context context;
    private OnItemClickCustom onItemClickCustom;
    private DbManager dbManager;
    private int myViewType = 0;
    private int VIEW_TYPE_ADS = 0;
    private int VIEW_TYPE_END_BUTTON = 1;
    public boolean isStartPage = true;
    private int NEXT_ADS_B = 1;
    private boolean needClear = true;
    private List<String> subcribersList = new ArrayList<>();


    public PostAdapter(List<NewPost> arrayPost, Context context, OnItemClickCustom onItemClickCustom) {
        this.mainPostList = arrayPost;
        this.context = context;
        this.onItemClickCustom = onItemClickCustom;

    }

    @NonNull

    @Override
    public ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_END_BUTTON) {
            view = LayoutInflater.from(context).inflate(R.layout.end_ads_item, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_ads, parent, false);
        }
        Log.d("MyLog", "Item type: " + viewType);

        return new ViewHolderData(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolderData holder, int position) {
//???????????? ?????? ?????????????????? ?????????? ?? ????????
        if (NEXT_PAGE.equals(mainPostList.get(position).getUid())) {
            holder.setNextItemData();
        } else {
            if(mainPostList.get(position).getVisibility().equals(DbManager.ACCEPTED))
            holder.setData(mainPostList.get(position));
            else {
                holder.setStateWaiting();
            }
            setFavIfSelected(holder);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mainPostList.get(position).getUid().equals(NEXT_PAGE)) {
            myViewType = 1;
        } else {
            myViewType = 0;
        }
        return myViewType;
    }

    @Override
    public int getItemCount() {
        return mainPostList.size();
    }

    private void deleteDialog(final NewPost newPost, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delte_title);
        builder.setMessage(R.string.delte_message);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dbManager.deleteItem(newPost, result -> {
                    mainPostList.remove(position);
                    notifyItemRemoved(position);
                    if(mainPostList.size()==1){
                        dbManager.getDataFromDb(((MainActivity) context).current_cat, "");
                    }
                });

            }
        });
        builder.show();
    }

    @Override
    public void onDataSubcRecived(List<String> subcribers) {
        if (subcribersList.size() > 0) {
            subcribersList.clear();
        }
        subcribersList.addAll(subcribers);
    }


    public interface OnItemClickCustom {
        void onItemSelected(int position);
    }

    public void updateAdapter(List<NewPost> listData) {
        needClearAdapter(listData);
        addNextButton(listData);
        notifiDataChanged(listData);
        removeNextButton(listData);
    }

    private void notifiDataChanged(List<NewPost> listData) {// ?????????????????? ???????????? ?????????????? ?? ?????????????? ???????? ?????????? ????????????????
        int myArraySize = mainPostList.size() - 1;
        if (myArraySize == -1) {
            myArraySize = 0;
        }
        mainPostList.addAll(myArraySize, listData);
        if (myArraySize == 0) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeChanged(myArraySize, listData.size());
        }
    }

    private void addNextButton(List<NewPost> listData) {//?????????????????? ???????????? ???????? ?????????? next
        if (listData.size() == MyConstants.ADS_LIMIT) {
            NewPost tempPost = new NewPost();
            tempPost.setUid(NEXT_PAGE);
            listData.add(tempPost);
        }
    }

    private void removeNextButton(List<NewPost> listData) {// ?????????????? ???????????? ???????? ?????? ???? ??????????
        if (listData.size() < MyConstants.ADS_LIMIT - 1 && mainPostList.size() > 0 && mainPostList.get(mainPostList.size() - 1).getUid().equals(NEXT_PAGE)) {
            int pos = mainPostList.size() - 1;
            mainPostList.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    private void needClearAdapter(List<NewPost> listData) {
        if (needClear) {
            mainPostList.clear();
        } else {
            listData.remove(0);
        }
        needClear = true;
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
        dbManager.setOnSubscriptions(this);
        dbManager.readSubscription();
    }


    private void setFavIfSelected(ViewHolderData holder) {
        if (context instanceof MainActivity) {
            FirebaseUser user = ((MainActivity) context).getFirebaseAuth().getCurrentUser();
            if (mainPostList.get(holder.getAdapterPosition()).isFav() || user.isAnonymous()) {
                holder.binding.imFav.setImageResource(R.drawable.ic_fav_selected);
            } else {
                holder.binding.imFav.setImageResource(R.drawable.ic_fav_not_selected);
            }
        }
        if (context instanceof PersonListActiviti) {
            FirebaseUser user = ((PersonListActiviti) context).getmAuth().getCurrentUser();
            if (mainPostList.get(holder.getAdapterPosition()).isFav() || user.isAnonymous()) {
                holder.binding.imFav.setImageResource(R.drawable.ic_fav_selected);
            } else {
                holder.binding.imFav.setImageResource(R.drawable.ic_fav_not_selected);
            }
            holder.binding.NameAndLogo.setVisibility(View.GONE);
            holder.binding.AddSub.setVisibility(View.GONE);

        }
    }
    //ViewHolder class

    public class ViewHolderData extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ItemAdsBinding binding;
        private String uid;


        public ViewHolderData(@NonNull View itemView) {
            super(itemView);
            if (itemView.findViewById(R.id.editLayout) != null) {
                binding = ItemAdsBinding.bind(itemView);
            }
            itemView.setOnClickListener(this);
        }


        public void setNextItemData() {
            if (context instanceof MainActivity) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String lastTitleTime = mainPostList.get(mainPostList.size() - 2).getDisc().toLowerCase() + "_" +
                                mainPostList.get(mainPostList.size() - 2).getTime();
                        dbManager.getDataFromDb(((MainActivity) context).current_cat, lastTitleTime);
                        isStartPage = false;
                        needClear = false;
                    }
                });
            }
            if (context instanceof PersonListActiviti) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String lastTitleTime = mainPostList.get(mainPostList.size() - 2).getDisc().toLowerCase() + "_" +
                                mainPostList.get(mainPostList.size() - 2).getTime();
                        dbManager.getDataFromDb(((PersonListActiviti) context).current_cat, lastTitleTime);
                        isStartPage = false;
                        needClear = false;
                    }
                });
            }
        }

        public void setData(NewPost newPost) {
            //?????? ?????????????????? ???????????? - ?????????????????? ??????????????!
            itemView.setOnClickListener(this);
            actionIfAnonymous(newPost);
            Picasso.get().load(newPost.getImageId()).into(binding.imAds);
            binding.imAds.setScaleType(ImageView.ScaleType.CENTER_CROP);
            binding.tvQuantityLike.setText(String.valueOf(newPost.getFavCounter()));
            binding.tvComments.setText(String.valueOf(newPost.getCommCount()));
            binding.tvName.setText(newPost.getName());
            Picasso.get().load(newPost.getLogoUser()).into(binding.UserPhoto);
            binding.tvDisc1.setText(newPost.getDisc());
            binding.tvTotalViews.setText(newPost.getTotal_views());
            uid = newPost.getUid();
            isItSubc();
            //tvQuantityLike.setText((int) newPost.getFavCounter());
            //Picasso.get().load(newPost.getLogoUser()).into(binding.UserPhoto);
            Picasso.get().load(newPost.getLogoUser()).transform(new CircleTransform()).into(binding.UserPhoto);
            binding.deleteButton.setOnClickListener(onClickItem(newPost));
            binding.imEditItem.setOnClickListener(onClickItem(newPost));
            binding.imFav.setOnClickListener(onClickItem(newPost));
            binding.NameAndLogo.setOnClickListener(onClickItem(newPost));
            binding.addSub.setOnClickListener(onClickItem(newPost));
            binding.imComments.setOnClickListener(onClickItem(newPost));
        }
        public void setStateWaiting() {
            binding.imAds.setImageResource(R.drawable.ic_fav_selected);
            binding.imAds.setScaleType(ImageView.ScaleType.CENTER);
            binding.tvQuantityLike.setText(R.string.hidden);
            binding.tvComments.setText(R.string.hidden);
           binding.tvName.setText("");
            binding.tvDisc1.setText(R.string.moderation);
            binding.tvTotalViews.setText(R.string.hidden);
            itemView.setOnClickListener(v -> {
/// ???????????????????? ???? ??????????????????
               // Toast.makeText(this,R.string.post_moderation, Toast.LENGTH_SHORT).show();
            });
           
        }

        private void isItSubc() {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser.getUid().equals(uid)) {
                binding.addSub.setVisibility(View.GONE);
            } else if (subcribersList.size() == 0) {
                binding.addSub.setVisibility(View.VISIBLE);
            } else if (subcribersList.contains(uid)) {
                binding.addSub.setVisibility(View.GONE);
            } else {
                binding.addSub.setVisibility(View.VISIBLE);
            }
        }

        private View.OnClickListener onClickItem(NewPost newPost) {
            return view -> {
                if (view.getId() == R.id.deleteButton) {
                    deleteDialog(newPost, getAdapterPosition());
                } else if (view.getId() == R.id.imEditItem) {
                    onClickEdit(newPost);
                } else if (view.getId() == R.id.imFav) {
                    onClickFav(newPost);
                } else if (view.getId() == R.id.NameAndLogo) {
                    UserListPhotoActivity(newPost);
                    //dbManager.getAllOwnerAds(newPost.getUid());
                } else if (view.getId() == R.id.addSub) {
                    addSubscription(newPost);
                }
                if (view.getId() == R.id.imComments) {
                    goToCommentsActivity(newPost);
                }
            };
        }

        public void addSubscription(NewPost newPost) {
            dbManager.AddSubscription(newPost.getUid());
            //updateAdapter(getMainList());
            dbManager.updateFieldSubcrib(getMainList(), ViewHolderData.this, newPost.getUid());
            notifyDataSetChanged();
            //  binding.addSub.setVisibility(View.GONE);
        }

        public void goToCommentsActivity(NewPost newPost) {
            Intent intent = new Intent(context, CommentsActivity.class);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            intent.putExtra("currentUser", currentUser.getUid());
            intent.putExtra("post", newPost);

            context.startActivity(intent);

        }

        public void UserListPhotoActivity(NewPost newPost) {
            Intent intent = new Intent(context, PersonListActiviti.class);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser.getUid().equals(uid)) {
                intent.putExtra("isSubscriber", "itIsCurrentUser");
            } else if (binding.addSub.getVisibility() == View.GONE) {
                intent.putExtra("isSubscriber", "true");
            } else if (binding.addSub.getVisibility() == View.VISIBLE) {
                intent.putExtra("isSubscriber", "false");
            }

            intent.putExtra("Uid", newPost.getUid());
//            intent.putExtra("userName", newPost.getName());
//            intent.putExtra("userPhoto", newPost.getLogoUser());
            context.startActivity(intent);
        }

        public void onClickFav(NewPost newPost) {
            if (context instanceof MainActivity) {
                FirebaseUser user = ((MainActivity) context).getFirebaseAuth().getCurrentUser();
                if (user.isAnonymous()) {
                    return;
                }
                setFavCounter(newPost);// ?????? ?????????????? ???? ???????????? - ?????????????????????? ????????????????
                dbManager.updateFav(newPost, ViewHolderData.this);
            }
            if (context instanceof PersonListActiviti) {
                FirebaseUser user = ((PersonListActiviti) context).getmAuth().getCurrentUser();
                if (user.isAnonymous()) {
                    return;
                }
                setFavCounter(newPost);// ?????? ?????????????? ???? ???????????? - ?????????????????????? ????????????????
                dbManager.updateFav(newPost, ViewHolderData.this);
            }
        }

        private void onClickEdit(NewPost newPost) {
            Intent i = new Intent(context, EditActivity.class);
            i.putExtra(MyConstants.New_POST_INTENT, newPost);
            i.putExtra("userName",newPost.getName());
            i.putExtra("userPhoto",newPost.getLogoUser());
            i.putExtra(MyConstants.EDIT_STATE, true);
            context.startActivity(i);
        }

        private void actionIfAnonymous(NewPost newPost) {
            if (context instanceof MainActivity) {
                FirebaseUser user = ((MainActivity) context).getFirebaseAuth().getCurrentUser();
                if (user != null) {
                    binding.editLayout.setVisibility(newPost.getUid().equals(user.getUid()) ? View.VISIBLE : View.GONE);
                    binding.imFav.setVisibility(user.isAnonymous() ? View.VISIBLE : View.VISIBLE);
                    binding.tvQuantityLike.setVisibility(user.isAnonymous() ? View.VISIBLE : View.VISIBLE);
                    if (user.isAnonymous()) {
                        binding.imFav.setImageResource(R.drawable.ic_fav_selected);
                    }
                }
            }
            if (context instanceof PersonListActiviti) {
                FirebaseUser user = ((PersonListActiviti) context).getmAuth().getCurrentUser();
                if (user != null) {
                    binding.editLayout.setVisibility(newPost.getUid().equals(user.getUid()) ? View.VISIBLE : View.GONE);
                    binding.imFav.setVisibility(user.isAnonymous() ? View.VISIBLE : View.VISIBLE);
                    binding.tvQuantityLike.setVisibility(user.isAnonymous() ? View.VISIBLE : View.VISIBLE);
                    if (user.isAnonymous()) {
                        binding.imFav.setImageResource(R.drawable.ic_fav_selected);
                    }
                }
            }

        }

        @Override
        public void onClick(View view) {
            NewPost newPost = mainPostList.get(getAdapterPosition());
            dbManager.updateTotalCounter(DbManager.TOTAL_VIEWS, newPost.getKey(), newPost.getTotal_views());
            // dbManager.updateFav(newPost);
            int totalViews = Integer.parseInt(newPost.getTotal_views());
            totalViews++;
            //onItemClickCustom.onItemSelected(getAdapterPosition());
            Intent i = new Intent(context, ShowLayoutActivity.class);
            i.putExtra(MyConstants.New_POST_INTENT, newPost);
            i.putExtra(MyConstants.EDIT_STATE, true);
            context.startActivity(i);
            onItemClickCustom.onItemSelected(getAdapterPosition());

        }

        public void setFavCounter(NewPost newPost) {
            int fCounter = Integer.parseInt(binding.tvQuantityLike.getText().toString());
            fCounter = (newPost.isFav()) ? --fCounter : ++fCounter; //???????? ?????? ?????????????? - ???????????? 1 ??.?? ???????????????????? ???? ??????????????????, ??
            // ???????? ?????? ???? ??????????????????- ???????????????? 1 ??.?? ???????????????????? ??????????????????
            binding.tvQuantityLike.setText(String.valueOf(fCounter));
            newPost.setFavCounter((long) fCounter);
        }


        public void clearAdapter() {
            mainPostList.clear();
            notifyDataSetChanged();
        }

        public List<NewPost> getMainList() {
            return mainPostList;
        }


    }
}