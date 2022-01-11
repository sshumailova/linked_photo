package com.android.linkedphotoShSonya.comments;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.linkedphotoShSonya.Adapter.PostAdapter;
import com.android.linkedphotoShSonya.Adapter.UserAdapter;
import com.android.linkedphotoShSonya.MainActivity;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.act.PersonListActiviti;
import com.android.linkedphotoShSonya.chat.AwesomeMessage;
import com.android.linkedphotoShSonya.chat.AwesomeMessageAdapter;
import com.android.linkedphotoShSonya.utils.CircleTransform;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentAdapterViewHolder>  {
    private List<Comment> comments;
    private UserAdapter.OnUserClickListener listener;



    public CommentAdapter(List<Comment> comments) {
        this.comments =comments;

    }

    public void setOnUserClickListener(UserAdapter.OnUserClickListener listener) {
        this.listener = listener;
    }

    public CommentAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Log.d("MyLog","CommentAdapter onCreate");
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_item, viewGroup, false);
       CommentAdapterViewHolder commentAdapterViewHolder= new CommentAdapterViewHolder(view,listener);
        return commentAdapterViewHolder;

    }


    public void onBindViewHolder(@NonNull  CommentAdapterViewHolder commentAdapterViewHolder, int i) {
        Log.d("MyLog","CommentAdapter onBind");
        Comment comment = comments.get(i);
        commentAdapterViewHolder.textComment.setText(comment.getTextComment());
         commentAdapterViewHolder.userNAme.setText(comment.getUserName());
            Picasso.get().load(comment.getImageIdSender()).transform(new CircleTransform()).into(commentAdapterViewHolder.imUser);
    }

    public int getItemCount() {
        return comments.size();
    }

    static class CommentAdapterViewHolder extends RecyclerView.ViewHolder{
        private ImageView imUser;
        private TextView userNAme;
        private TextView textComment;
        public CommentAdapterViewHolder(View view, final UserAdapter.OnUserClickListener listener){
            super(view);
            imUser=view.findViewById(R.id.imUser);
            userNAme=view.findViewById(R.id.userNAme);
            textComment=view.findViewById(R.id.textComment);
            imUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onUserClick(position);
                        }
                    }
                }
            });
        }

    }
}



//        public void setNextItemData() {
//            if (context instanceof MainActivity) {
//                itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        String lastTitleTime = mainPostList.get(mainPostList.size() - 2).getDisc().toLowerCase() + "_" +
//                                mainPostList.get(mainPostList.size() - 2).getTime();
//                        dbManager.getDataFromDb(((MainActivity) context).current_cat, lastTitleTime);
//                        isStartPage = false;
//                        needClear = false;
//                    }
//                });
//            }
//            if (context instanceof PersonListActiviti) {
//                itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        String lastTitleTime = mainPostList.get(mainPostList.size() - 2).getDisc().toLowerCase() + "_" +
//                                mainPostList.get(mainPostList.size() - 2).getTime();
//                        dbManager.getDataFromDb(((PersonListActiviti) context).current_cat, lastTitleTime);
//                        isStartPage = false;
//                        needClear = false;
//                    }
//                });
//            }
//        }


//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        CommentAdapter.ViewHolder viewHolder;
//        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//        Comment comment = getItem(position);
//        int layoutResourse = 0;
//        layoutResourse=R.layout.one_comment;
//        convertView = layoutInflater.inflate(layoutResourse, parent, false);
//        viewHolder = new CommentAdapter.ViewHolder(convertView);
////            viewHolder = new CommentAdapter.ViewHolder(convertView);
////        if (convertView != null) {
////            viewHolder = (CommentAdapter.ViewHolder) convertView.getTag();
////        } else {
////            convertView = layoutInflater.inflate(layoutResourse, parent, false);
////            viewHolder = new CommentAdapter.ViewHolder(convertView);
////            convertView.setTag(viewHolder);
////
////        }
////        boolean isText = comment.getTextComment() != null;
////        if (isText) {
//             //viewHolder.imUser;
//            viewHolder.textComment.setText(comment.getTextComment());
//            viewHolder.userNAme.setText(comment.getUserName());
//            Picasso.get().load(comment.getImageIdSender().toString()).transform(new CircleTransform()).into(viewHolder.imUser);
//        return convertView;
//    }
//
//    public int getViewTypeCount() {
//        return 2;
//    }






