package com.example.businessapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isInvisible

class ContactListAdapter(context: Context, private val contacts: MutableList<Contact>) :
    BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return contacts.size
    }

    override fun getItem(pos: Int): Any {
        return contacts[pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.contact_list_item, parent, false)
        val thisAddress = contacts[pos]

        var tv = rowView.findViewById(R.id.tvType) as TextView
        tv.text = thisAddress.type

        tv = rowView.findViewById(R.id.tvValue) as TextView
        tv.text = thisAddress.value
        val icon = rowView.findViewById(R.id.contactTypeIcon) as ImageView

        if(thisAddress.isWebsite) {
            icon.setImageResource(R.drawable.ic_webpage)
        }else if(thisAddress.isPhoneNumber){
            icon.setImageResource(android.R.drawable.ic_menu_call)
        }else{
            icon.isInvisible = true
        }

        return rowView

    }
}