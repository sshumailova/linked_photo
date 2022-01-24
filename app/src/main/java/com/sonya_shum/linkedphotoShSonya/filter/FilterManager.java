package com.sonya_shum.linkedphotoShSonya.filter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.sonya_shum.linkedphotoShSonya.R;
import com.sonya_shum.linkedphotoShSonya.databinding.ActivityFilterBinding;

public class FilterManager {
    private static final String[] orderByKeyWords = {"country_", "city_"};

    public static void saveFilter(String saveData, SharedPreferences preferences,String prefName) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(prefName, saveData);
        editor.apply();
    }

    public static void clearFilter(SharedPreferences preferences) {
        preferences.edit().clear().apply();

    }

    public static String getFilterText(String filter) {
        String[] filterArray = filter.split("\\|");
        StringBuilder textFilter = new StringBuilder();
        for (int i = 0; i < filterArray.length; i++) {
            if (!filterArray[i].equals("empty")) {
                if (i != 0) textFilter.append(",");
                textFilter.append(filterArray[i]);
            }
        }
        return textFilter.toString();
    }

    public static String createTextFilter(ActivityFilterBinding rootElement, Context context) {
        String country = rootElement.tvCountry.getText().toString();
        String city = rootElement.tvCity.getText().toString();
        if (country.equals(context.getString(R.string.select_country_f_title))) {
            country = "empty";
        }
        if (city.equals(context.getString(R.string.select_city_f_title))) {
            city = "empty";
        }
        return country + "|" + city;
    }

    public static String createOrderByFilter(String filter) {
        String[] filterTempArray = filter.split("\\|");
        StringBuilder orderByFilter = new StringBuilder();
        for (int i = 0; i < filterTempArray.length; i++) {
            if (!filterTempArray[i].equals("empty") && !filterTempArray[i].equals("false")) {
             orderByFilter.append(orderByKeyWords[i]);
            }

        }
        orderByFilter.append("name_time");
        Log.d("MyLog","Order By: "+ orderByFilter.toString());
        return orderByFilter.toString();
}
    public static String createFilter(String filter) {
        String[] filterTempArray = filter.split("\\|");
        StringBuilder orderByFilter = new StringBuilder();
        for (String s : filterTempArray) {
            if (!s.equals("empty") && !s.equals("false")) {
                orderByFilter.append(s);
                orderByFilter.append("_");
            }

        }

        Log.d("MyLog","Filter: "+ orderByFilter.toString());
        return orderByFilter.toString();
    }
}