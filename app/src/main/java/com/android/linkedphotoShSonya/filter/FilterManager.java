package com.android.linkedphotoShSonya.filter;

import android.content.SharedPreferences;

import com.android.linkedphotoShSonya.utils.MyConstants;

public class FilterManager {
    public static void saveFilter(String saveData, SharedPreferences preferences){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MyConstants.MAIN_FILTER, saveData);
        editor.apply();
    }
    public static void clearFilter(SharedPreferences preferences){
        saveFilter("empty",preferences);
    }
    public static String getFilterText(String filter){
        String[] filterArray=filter.split("\\|");
        StringBuilder textFilter= new StringBuilder();
        for(int i=0;i<filterArray.length;i++){
            if(!filterArray[i].equals("empty")){
                if(i!=0) textFilter.append(",");
                textFilter.append(filterArray[i]);
            }
        }
        return textFilter.toString();
    }
}
