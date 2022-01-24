package com.sonya_shum.linkedphotoShSonya.chat;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sonya_shum.linkedphotoShSonya.R;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AwesomeMessageAdapter extends ArrayAdapter<AwesomeMessage> {

   private List<AwesomeMessage> messages;
   private Activity activity;


    public AwesomeMessageAdapter(Activity context, int resource, List<AwesomeMessage> messages) {
        super(context, resource, messages);
        this.messages=messages;
        this.activity=context;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        AwesomeMessage awesomeMessage = getItem(position);
        int layoutResourse = 0;
        int viewType = getItemViewType(position);
        if (viewType == 0) {
            layoutResourse = R.layout.my_message_item;
        } else {
            layoutResourse = R.layout.your_message_item;
        }


        if (convertView != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = layoutInflater.inflate(layoutResourse, parent, false);
        viewHolder = new ViewHolder(convertView);
        convertView.setTag(viewHolder);

    }
        if(awesomeMessage.getImageUrl()==null){
            Log.d("MyLog","awesomeMessageText"+awesomeMessage.getText());
           viewHolder.messageTextView.setVisibility(View.VISIBLE);
            viewHolder.photoImageView.setVisibility(View.GONE);
            viewHolder.messageTextView.setText(awesomeMessage.getText());

        }
        else {
           // viewHolder.messageTextView.setVisibility(View.GONE);
            viewHolder.photoImageView.setVisibility(View.VISIBLE);
           Glide.with(viewHolder.photoImageView.getContext()).load(awesomeMessage.getImageUrl()).into(viewHolder.photoImageView);
            viewHolder.messageTextView.setText(awesomeMessage.getText());
        }

        String dateInMillis = awesomeMessage.getTime();
        long time=Long.parseLong(dateInMillis);
        SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date(time);
        viewHolder.data.setText(formater.format(date));

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        int flag;
        AwesomeMessage awesomeMessage=messages.get(position);
        if(awesomeMessage.isMine()){
            flag=0;
        }
        else{
            flag=1;
        }
        return flag;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private class ViewHolder{
        private ImageView photoImageView;
        private TextView messageTextView;
        private TextView data;
        public ViewHolder(View view){
            photoImageView=view.findViewById(R.id.photoImageView);
            messageTextView=view.findViewById(R.id.messageTextView);
            data=view.findViewById(R.id.tvData);
        }

    }
}
