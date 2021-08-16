package com.android.linkedphotoShSonya.utils

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList

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
}