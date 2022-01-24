package com.sonya_shum.linkedphotoShSonya.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sonya_shum.linkedphotoShSonya.Adapter.SelectCountryAdapter
import com.sonya_shum.linkedphotoShSonya.R
import android.widget.SearchView.OnQueryTextListener as OnQueryTextListener1

object DialogHelper {
    fun showDialog(context: Context, list: ArrayList<String>,tvText:TextView) {
        val bulder = AlertDialog.Builder(context)
        val dialog = bulder.create();
        val rootView = LayoutInflater.from(context).inflate(R.layout.select_country_dialog, null)
        val rcView = rootView.findViewById<RecyclerView>(R.id.rcViewCountry)
        rcView.layoutManager = LinearLayoutManager(context)
        val adapter = SelectCountryAdapter(dialog,tvText)
        val sv = rootView.findViewById<SearchView>(R.id.svCountry)
        setSearchView(sv, adapter, list)
        rcView.adapter = adapter
        adapter.update(list)
        dialog.setView(rootView)
        dialog.show()

    }

    private fun setSearchView(sv: SearchView?, adapter: SelectCountryAdapter, list: ArrayList<String>) {
            sv?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
val tempList= CountryManager.filterListData(list, newText)
                    adapter.update(tempList)
                    return true
                }


            })
        }

}