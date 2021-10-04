package com.android.linkedphotoShSonya.db;

import java.io.Serializable;

public class User  implements Serializable {
    private String name;
    private String id;
    private String key;
    private String email;
    private String imageId;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", email='" + email + '\'' +
                '}';
    }


        public String getImageId() {
        return imageId;
   }

   public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}
