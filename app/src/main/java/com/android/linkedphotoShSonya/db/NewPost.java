package com.android.linkedphotoShSonya.db;

import java.io.Serializable;

public class NewPost implements Serializable {
    private String imageId;
    private String imageId2;
    private String imageId3;
    private String country;
    private String city;
    private String disc;
    private String key;
    private String uid;
    private String time;
    private String cat;
    private String name;
    private String logoUser;
    private String total_views;
    private long favCounter = 0;
    private boolean isFav = false;

    public String getImageId3() {
        return imageId3;
    }

    public void setImageId3(String imageId3) {
        this.imageId3 = imageId3;
    }

    public String getImageId2() {
        return imageId2;
    }

    public void setImageId2(String imageId2) {
        this.imageId2 = imageId2;
    }

    public String getTotal_views() {
        return total_views;
    }

    public void setTotal_views(String total_views) {
        this.total_views = total_views;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisc() {
        return disc;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDisc(String disc) {
        this.disc = disc;
    }

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


    public long getFavCounter() {
        return favCounter;
    }

    public void setFavCounter(long favCounter) {
        this.favCounter = favCounter;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setFav(boolean fav) {
        isFav = fav;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLogoUser() {
        return logoUser;
    }

    public void setLogoUser(String logoUser) {
        this.logoUser = logoUser;
    }
}
