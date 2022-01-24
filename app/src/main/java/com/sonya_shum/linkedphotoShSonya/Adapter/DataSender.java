package com.sonya_shum.linkedphotoShSonya.Adapter;

import com.sonya_shum.linkedphotoShSonya.db.NewPost;

import java.util.List;

public interface DataSender {
    public void onDataRecived(List<NewPost> listData);
}
