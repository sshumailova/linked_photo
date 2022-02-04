package com.sonya_shum.linkedphotoShSonya.dagger;

import com.sonya_shum.linkedphotoShSonya.MainActivity;
import com.sonya_shum.linkedphotoShSonya.act.AdminActivity;
import com.sonya_shum.linkedphotoShSonya.act.EditActivity;
import com.sonya_shum.linkedphotoShSonya.act.FollowersActivity;
import com.sonya_shum.linkedphotoShSonya.act.MainAppClass;
import com.sonya_shum.linkedphotoShSonya.act.MyChatsActivity;
import com.sonya_shum.linkedphotoShSonya.act.PersonListActiviti;
import com.sonya_shum.linkedphotoShSonya.act.ShowLayoutActivity;
import com.sonya_shum.linkedphotoShSonya.act.UserListActivity;
import com.sonya_shum.linkedphotoShSonya.chat.ChatActivity;
import com.sonya_shum.linkedphotoShSonya.comments.CommentsActivity;
import com.sonya_shum.linkedphotoShSonya.dagger.module.AppModule;
import com.sonya_shum.linkedphotoShSonya.dagger.module.MainAppClassModule;
import com.sonya_shum.linkedphotoShSonya.db.DbManager;
import com.sonya_shum.linkedphotoShSonya.userActivity;

import dagger.Component;

@Component
        (modules = {AppModule.class, MainAppClassModule.class})

 public interface AppComponent {
    void inject(EditActivity activity);
void inject(PersonListActiviti  activiti);
void inject(ChatActivity  activiti);
void inject(MyChatsActivity  activiti);
void inject(ShowLayoutActivity activiti);
void inject(UserListActivity activiti);
void inject(CommentsActivity activiti);
void inject(userActivity activiti);
void inject(MainActivity activiti);

}

