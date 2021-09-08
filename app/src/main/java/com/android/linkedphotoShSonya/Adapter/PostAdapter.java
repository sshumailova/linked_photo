package com.android.linkedphotoShSonya.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.EditActivity;
import com.android.linkedphotoShSonya.MainActivity;
import com.android.linkedphotoShSonya.db.NewPost;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.ShowLayoutActivityActivity;
import com.android.linkedphotoShSonya.utils.MyConstants;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolderData> {
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
private boolean needClear=true;


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

        return new ViewHolderData(view, onItemClickCustom);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolderData holder, int position) {
//кнопки для подгрузки вверх и вниз
        if (NEXT_PAGE.equals(mainPostList.get(position).getUid())) {
            holder.setNextItemData();
        } else {
            holder.setData(mainPostList.get(position));
            setFavIfSelected(holder);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mainPostList.get(position).getUid().equals(NEXT_PAGE) ) {
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
                dbManager.deleteItem(newPost);
                mainPostList.remove(position);
                notifyItemRemoved(position);
            }
        });
        builder.show();
    }

    public interface OnItemClickCustom {
        void onItemSelected(int position);
    }

    public void updateAdapter(List<NewPost> listData) {
        if(needClear) {
            mainPostList.clear();
        }
        else {
            listData.remove(0);
        }


        if (listData.size() == MyConstants.ADS_LIMIT) {
            NewPost tempPost = new NewPost();
            tempPost.setUid(NEXT_PAGE);
            listData.add(tempPost);
        }
        int myArraySize=mainPostList.size()-1;
        if(myArraySize==-1){
            myArraySize=0;
        }
        mainPostList.addAll(myArraySize,listData);
        if(myArraySize==0) {
            notifyDataSetChanged();
        }
        else {
            notifyItemRangeChanged(myArraySize,listData.size());
        }
        if(listData.size()<MyConstants.ADS_LIMIT-1&& mainPostList.size()>0){
            int pos=mainPostList.size()-1;
            mainPostList.remove(pos);
            notifyItemRemoved(pos);
        }
        needClear=true;

    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    private void setFavIfSelected(ViewHolderData holder) {
        if (mainPostList.get(holder.getAdapterPosition()).isFav()) {
            holder.imFav.setImageResource(R.drawable.ic_fav_selected);
        } else {
            holder.imFav.setImageResource(R.drawable.ic_fav_not_selected);
        }
    }
    //ViewHolder class

    public class ViewHolderData extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvDisc;
        private TextView tvTotalView;
        private ImageView inAds;
        private LinearLayout editLayout;
        public ImageButton deleteButton, imEditItem, imFav;
        private OnItemClickCustom onItemClickCustom;
        public TextView tvQuantityLike;


        public ViewHolderData(@NonNull View itemView, OnItemClickCustom onItemClickCustom) {
            super(itemView);
            tvDisc = itemView.findViewById(R.id.tvDisc1);
            inAds = itemView.findViewById(R.id.imAds);
            tvTotalView = itemView.findViewById(R.id.tvTotalViews);
            editLayout = itemView.findViewById(R.id.editLayout);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            imEditItem = itemView.findViewById(R.id.imEditItem);
            imFav = itemView.findViewById(R.id.imFav);
            this.onItemClickCustom = onItemClickCustom;
            itemView.setOnClickListener(this);
            tvQuantityLike = itemView.findViewById(R.id.tvQuantityLike);
        }



        public void setNextItemData() {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String lastTitleTime = mainPostList.get(mainPostList.size() - 2).getDisc().toLowerCase() + "_" +
                            mainPostList.get(mainPostList.size() - 2).getTime();
                    dbManager.getDataFromDb(((MainActivity) context).current_cat, lastTitleTime);
                    isStartPage = false;
                  needClear=false;
                }
            });
        }

        public void setData(NewPost newPost) {//тут показываю данные
            FirebaseUser user = ((MainActivity) context).getmAuth().getCurrentUser();
            if (user != null) {
                editLayout.setVisibility(newPost.getUid().equals(user.getUid()) ? View.VISIBLE : View.GONE);
                imFav.setVisibility(user.isAnonymous() ? View.GONE : View.VISIBLE);
                tvQuantityLike.setVisibility(user.isAnonymous() ? View.GONE : View.VISIBLE);
            }
            Picasso.get().load(newPost.getImageId()).into(inAds);
            tvQuantityLike.setText(String.valueOf(newPost.getFavCounter()));
            String textDisc = "";
            if (newPost.getDisc().length() > 15) {
                textDisc = newPost.getDisc().substring(0, 15) + "....";
                tvDisc.setText(textDisc);
            } else {
                tvDisc.setText(newPost.getDisc());
                tvTotalView.setText(newPost.getTotal_views());
                //tvQuantityLike.setText((int) newPost.getFavCounter());
            }
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteDialog(newPost, getAdapterPosition());
                }
            });
            imEditItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, EditActivity.class);
                    i.putExtra(MyConstants.New_POST_INTENT, newPost);
                    i.putExtra(MyConstants.EDIT_STATE, true);
                    context.startActivity(i);

                }
            });
            imFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setFavCounter(newPost, tvQuantityLike);// при нажатии на сердце - запускается проверка
                    dbManager.updateFav(newPost, ViewHolderData.this);


                }
            });
        }


        @Override
        public void onClick(View view) {
            NewPost newPost = mainPostList.get(getAdapterPosition());
            dbManager.updateTotalCounter(DbManager.TOTAL_VIEWS, newPost.getKey(), newPost.getTotal_views());
            // dbManager.updateFav(newPost);
            int totalViews = Integer.parseInt(newPost.getTotal_views());
            totalViews++;
            //onItemClickCustom.onItemSelected(getAdapterPosition());
            Intent i = new Intent(context, ShowLayoutActivityActivity.class);
            i.putExtra(MyConstants.New_POST_INTENT, newPost);
            i.putExtra(MyConstants.EDIT_STATE, true);
            context.startActivity(i);
            onItemClickCustom.onItemSelected(getAdapterPosition());

        }
    }

    public void clearAdapter() {
        mainPostList.clear();
        notifyDataSetChanged();
    }

    public List<NewPost> getMainList() {
        return mainPostList;
    }

    public static void setFavCounter(NewPost newPost, TextView tvQuantityLike) {
        int fCounter = Integer.parseInt(tvQuantityLike.getText().toString());
        fCounter = (newPost.isFav()) ? --fCounter : ++fCounter; //если это израное - отнять 1 т.к становится не избранным, а
        // если это не избранное- приавить 1 т.к становатся избранным
        tvQuantityLike.setText(String.valueOf(fCounter));
        newPost.setFavCounter((long) fCounter);
    }
}
