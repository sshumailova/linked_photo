package com.android.linkedphotoShSonya.act;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import com.android.linkedphotoShSonya.Adapter.AdminAdapter;
import com.android.linkedphotoShSonya.Adapter.DataSender;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.databinding.ActivityAdminBinding;
import com.android.linkedphotoShSonya.db.DbManager;
import com.android.linkedphotoShSonya.db.NewPost;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity implements DataSender, AdminAdapter.Listener {
    private ActivityAdminBinding binding;
    private AdminAdapter adapter;
    private DbManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbManager.getAdminAds();
    }

    private void init() {
        dbManager = new DbManager(this);
        binding.rcView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminAdapter(this);
        binding.rcView.setAdapter(adapter);
    }

    @Override
    public void onDataRecived(List<NewPost> listData) {
        adapter.submitList(listData);
        isListEmpty(listData);

    }

    @Override
    public void onAccept(NewPost newPost) {
        changeAdVisibility(DbManager.ACCEPTED, newPost);
    }

    @Override
    public void onDecline(NewPost newPost) {
        changeAdVisibility(DbManager.DECLINED, newPost);
    }

    @Override
    public void onDelete(NewPost newPost) {
        dbManager.deleteItem(newPost);
        List<NewPost> tempList = new ArrayList<>(adapter.getCurrentList());
        tempList.remove(newPost);
        adapter.submitList(tempList);
isListEmpty(tempList);


    }

    private void changeAdVisibility(String visibility, NewPost newPost) {
        dbManager.setAdVisibility(newPost.getKey(), visibility, result -> {
            List<NewPost> tempList = new ArrayList<>(adapter.getCurrentList());
            tempList.remove(newPost);
            adapter.submitList(tempList);
            isListEmpty(tempList);
        });
    }
    private void isListEmpty(List<NewPost> list){

        binding.tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}