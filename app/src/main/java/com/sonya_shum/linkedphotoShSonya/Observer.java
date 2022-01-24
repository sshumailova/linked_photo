package com.sonya_shum.linkedphotoShSonya;

import com.sonya_shum.linkedphotoShSonya.db.User;

import java.util.List;

public interface Observer {
    public void handleEvent(List<User> users);
}
