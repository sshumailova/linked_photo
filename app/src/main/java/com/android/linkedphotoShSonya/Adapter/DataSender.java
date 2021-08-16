package com.android.linkedphotoShSonya.Adapter;

import com.android.linkedphotoShSonya.db.NewPost;

import java.util.List;

public interface DataSender {
    public void onDataRecived(List<NewPost> listData);
}
