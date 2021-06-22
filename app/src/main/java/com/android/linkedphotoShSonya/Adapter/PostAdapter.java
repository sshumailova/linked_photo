package com.android.linkedphotoShSonya.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.linkedphotoShSonya.DbManager;
import com.android.linkedphotoShSonya.EditActivity;
import com.android.linkedphotoShSonya.MainActivity;
import com.android.linkedphotoShSonya.NewPost;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.ShowLayoutActivityActivity;
import com.android.linkedphotoShSonya.utils.MyConstants;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolderData> {
    private List<NewPost> arrayPost;
    private Context context;
    private OnItemClickCustom onItemClickCustom;
    private DbManager dbManager;

    public PostAdapter(List<NewPost> arrayPost, Context context, OnItemClickCustom onItemClickCustom) {
        this.arrayPost = arrayPost;
        this.context = context;
        this.onItemClickCustom = onItemClickCustom;

    }

    @NonNull

    @Override
    public ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ads, parent, false);
        return new ViewHolderData(view, onItemClickCustom);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolderData holder, int position) {
//заполнять item
        holder.setData(arrayPost.get(position));
    }

    @Override
    public int getItemCount() {
        return arrayPost.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvDisc;
        private TextView tvTotalView;
        private ImageView inAds;
        private LinearLayout editLayout;
        private ImageButton deleteButton;
        private ImageButton imEditItem;
        private OnItemClickCustom onItemClickCustom;

        public ViewHolderData(@NonNull View itemView, OnItemClickCustom onItemClickCustom) {
            super(itemView);
            tvDisc = itemView.findViewById(R.id.tvDisc1);
            inAds = itemView.findViewById(R.id.imAds);
            tvTotalView=itemView.findViewById(R.id.tvTotalViews);
            editLayout= itemView.findViewById(R.id.editLayout);
            deleteButton=itemView.findViewById(R.id.deleteButton);
            imEditItem=itemView.findViewById(R.id.imEditItem);
            this.onItemClickCustom = onItemClickCustom;
            itemView.setOnClickListener(this);
        }

        public void setData(NewPost newPost) {
            if(newPost.getUid().equals(MainActivity.MAUTh)){
                editLayout.setVisibility(View.VISIBLE);
            }
            else{
            editLayout.setVisibility(View.GONE);
            }
            Picasso.get().load(newPost.getImageId()).into(inAds);
            String textDisc = "";
            if (newPost.getDisc().length() > 15) {
                textDisc = newPost.getDisc().substring(0, 15) + "....";
                tvDisc.setText(textDisc);
            } else {
                tvDisc.setText(newPost.getDisc());
                tvTotalView.setText(newPost.getTotal_views());
            }
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteDialog(newPost,getAdapterPosition());
                }
            });
            imEditItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(context, EditActivity.class);
                    i.putExtra(MyConstants.IMAGE_ID,newPost.getImageId());
                    i.putExtra(MyConstants.IMAGE_ID2,newPost.getImageId2());
                    i.putExtra(MyConstants.IMAGE_ID3,newPost.getImageId3());
                    i.putExtra(MyConstants.DISC_ID,newPost.getDisc());
                    i.putExtra(MyConstants.KEY,newPost.getKey());
                    i.putExtra(MyConstants.UID,newPost.getUid());
                    i.putExtra(MyConstants.TIME,newPost.getTime());
                    i.putExtra(MyConstants.CAT,newPost.getCat());
                    i.putExtra(MyConstants.EDIT_STATE,true);
                    i.putExtra(MyConstants.TOTAL_VIEWS,newPost.getTotal_views());
                    context.startActivity(i);

                }
            });
        }


        @Override
        public void onClick(View view) {
            NewPost newPost=arrayPost.get(getAdapterPosition());
            dbManager.updateTotalViews(newPost);
            //onItemClickCustom.onItemSelected(getAdapterPosition());
            Intent i=new Intent(context, ShowLayoutActivityActivity.class);
            i.putExtra(MyConstants.IMAGE_ID,newPost.getImageId());
            i.putExtra(MyConstants.IMAGE_ID2,newPost.getImageId2());
            i.putExtra(MyConstants.IMAGE_ID3,newPost.getImageId3());
            i.putExtra(MyConstants.DISC_ID,newPost.getDisc());
            i.putExtra(MyConstants.TIME,newPost.getTime());
            i.putExtra(MyConstants.CAT,newPost.getCat());
            i.putExtra(MyConstants.EDIT_STATE,true);
            i.putExtra(MyConstants.TOTAL_VIEWS,newPost.getTotal_views());
            context.startActivity(i);
            onItemClickCustom.onItemSelected(getAdapterPosition());

        }
    }
private void deleteDialog(final NewPost newPost,int position){
    AlertDialog.Builder builder= new AlertDialog.Builder(context);
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
arrayPost.remove(position);
notifyItemRemoved(position);
        }
    });
    builder.show();
}
    public interface OnItemClickCustom {
        void onItemSelected(int position);
    }

    public void updateAdapter(List<NewPost> listData) {
        arrayPost.clear();
        arrayPost.addAll(listData);
        notifyDataSetChanged();
    }
    public void setDbManager(DbManager dbManager){
        this.dbManager=dbManager;

    }
}
