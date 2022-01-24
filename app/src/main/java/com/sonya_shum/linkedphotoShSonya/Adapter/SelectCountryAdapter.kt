package com.sonya_shum.linkedphotoShSonya.Adapter

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sonya_shum.linkedphotoShSonya.R

class SelectCountryAdapter (val dialog:AlertDialog,val tvTextView: TextView): RecyclerView.Adapter<SelectCountryAdapter.ItemHolder>() {
    private val mainArray = ArrayList<String>()


    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
       holder.setData(mainArray[position])
        holder.itemView.setOnClickListener {
            Log.d("MyLog", "Item pressed: ${mainArray[position]}")
            tvTextView.text=mainArray[position]
            dialog.dismiss()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.select_country_item, parent, false)
        return ItemHolder(view)
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var tvTitle: TextView
        fun setData(name: String) {
            tvTitle = itemView.findViewById(R.id.tvCountry)
            tvTitle.text=name
        }
    }
    fun update(newList: ArrayList<String>){
        mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }
}

private fun View.setOnClickListener(d: Int) {

}
