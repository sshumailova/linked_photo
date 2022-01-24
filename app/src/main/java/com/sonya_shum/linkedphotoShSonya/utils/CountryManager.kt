package com.sonya_shum.linkedphotoShSonya.utils

import android.content.Context
import com.sonya_shum.linkedphotoShSonya.R
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*

object CountryManager {
    fun getAllCountries(context: Context): ArrayList<String> {
        val tempArray = ArrayList<String>()
        try {
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size: Int = inputStream.available()
            val byteArray = ByteArray(size)
            inputStream.read(byteArray)
            val jFile = String(byteArray)
            val jObjects = JSONObject(jFile)
            val countriesNames = jObjects.names();
            if (countriesNames != null) {
                for (n in 0 until countriesNames.length()) {
                    tempArray.add(countriesNames.getString(n))
                }

            }
        } catch (e: IOException) {

        }
        return tempArray;
    }

    fun filterListData(list: ArrayList<String>, searchText: String?): ArrayList<String> {
        val tempList=ArrayList<String>()
        if(searchText==null){
            tempList.add("No Result")
            return tempList;
        }
        for(selection :String in list){// сколько есть вэтом списке -столько раз и запускается
            if(selection.toLowerCase(Locale.ROOT).startsWith(searchText.toLowerCase(Locale.ROOT)))
                tempList.add(selection)
        }
        if(tempList.isEmpty()){
            tempList.add("No Result")
        }
        return tempList
    }
    fun getAllCites(context: Context,country:String): ArrayList<String> {
        val tempArray = ArrayList<String>()
        try {
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size: Int = inputStream.available()
            val byteArray = ByteArray(size)
            inputStream.read(byteArray)
            val jFile = String(byteArray)
            val jObjects = JSONObject(jFile)
            val cites = jObjects.getJSONArray(country);
                for (n in 0 until cites.length()) {
                    tempArray.add(cites.getString(n))
                }
        } catch (e: IOException) {

        }
        return tempArray;
    }
}