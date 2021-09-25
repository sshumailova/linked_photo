package com.android.linkedphotoShSonya.db;

import java.io.Serializable;

public class User  implements Serializable {
    private String name;
    private String id;
    private String key;

    //private String imageId;
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

//    public String getImageId() {
//        return imageId;
//    }

//    public void setImageId(String imageId) {
//        this.imageId = imageId;
//    }
}
