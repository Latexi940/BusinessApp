package com.example.businessapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class AddressListAdapter(context: Context, private val addresses: MutableList<Address>) :
    BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return addresses.size
    }

    override fun getItem(pos: Int): Any {
        return addresses[pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.address_list_item, parent, false)
        val thisAddress = addresses[pos]

        var tv = rowView.findViewById(R.id.tvStreet) as TextView
        tv.text = thisAddress.street

        tv = rowView.findViewById(R.id.tvPostCode) as TextView
        tv.text = thisAddress.postCode

        tv = rowView.findViewById(R.id.tvCity) as TextView
        tv.text = thisAddress.city

        return rowView

    }
}