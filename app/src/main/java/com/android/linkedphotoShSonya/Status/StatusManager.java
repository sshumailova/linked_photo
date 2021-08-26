package com.android.linkedphotoShSonya.Status;

import com.android.linkedphotoShSonya.db.NewPost;

import java.util.Locale;

public class StatusManager {
    public static StatusItem fillStatusItem(NewPost post) {
        StatusItem stItem = new StatusItem();
        String time = post.getTime();
        String cat = post.getCat();
        String country = post.getCountry();
        String city = post.getCity();
        String disc = post.getDisc().toLowerCase();

        stItem.catTime = cat + "_" + time;
        stItem.filter_by_time = time;
        stItem.disc_time = disc + "_" + time;

        stItem.country_disc_time = country + "_" + disc + "_" + time;
        stItem.country_city_disc_time = country + "_" + city + "_" + disc + "_" + time;
        return stItem;
    }
}
