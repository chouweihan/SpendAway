package com.weihan.chou.spendaway

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.TextView
import de.hdodenhof.circleimageview.CircleImageView


class ListAdapter(private val ctx: Context, private val entryList: List<EntryPH>) :
    ArrayAdapter<EntryPH>(ctx, 0, entryList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {


        lateinit var rowView: View

        if (convertView == null) {
            val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowView = inflater.inflate(R.layout.list_layout, parent, false)
        } else
            rowView = convertView

        val description = rowView.findViewById(R.id.descID) as TextView
        val location = rowView.findViewById(R.id.locationID) as TextView
        val price = rowView.findViewById(R.id.priceID) as TextView
        val date = rowView.findViewById(R.id.dateID) as TextView

        val entry = entryList[position]

        val priceString = "${context.getString(R.string.price)}${entry.price}"
        val dateString = "${entry.month}/${entry.day}/${entry.year}"
        val imV = rowView.findViewById(R.id.imageID) as CircleImageView

        if (entry.image) {
            val imagePath = entry.imagePath
            val encodeByte = Base64.decode(imagePath, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
            imV.setImageBitmap(bitmap)
        } else {
            imV.setImageResource(R.drawable.starz)
        }

        description.text = entry.content
        location.text = entry.location
        price.text = priceString
        date.text = dateString

        return rowView
    }
}