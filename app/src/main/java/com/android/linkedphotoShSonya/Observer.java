package com.android.linkedphotoShSonya;

import com.android.linkedphotoShSonya.db.User;

import java.util.List;

public interface Observer {
    public void handleEvent(List<User> users);
}
