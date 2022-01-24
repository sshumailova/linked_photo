package com.sonya_shum.linkedphotoShSonya.Status;

import com.sonya_shum.linkedphotoShSonya.db.NewPost;

public class FilterManager {
    public static FilterItem fillFilter_1_2(NewPost post, boolean isFilter1) {
        FilterItem fItem = new FilterItem();
        String userName;
        userName = (isFilter1) ? post.getName().toLowerCase() : "";
        String time = post.getTime();
        String cat = post.getCat();
        String country = post.getCountry();
        String city = post.getCity();
        // String disc;
        //disc = (isFilter1) ? post.getDisc().toLowerCase() : "";
       // fItem.name_time = userName + "_" + time;
        if (!userName.equals("")) {
            fItem.name_time = userName + "_" + time;
            userName = "_" + userName;
        } else {
            fItem.name_time = time;
        }
        fItem.cat_name_Time = cat + userName + "_" + time;
        fItem.country_name_time = country + userName + "_" + time;
        fItem.country_city_name_time = country + "_" + city + userName + "_" + time;
        return fItem;
    }
}
