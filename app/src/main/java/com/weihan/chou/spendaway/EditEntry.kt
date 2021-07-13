package com.weihan.chou.spendaway

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import drawable.DialogDelete
import kotlinx.android.synthetic.main.activity_edit_entry.*
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat


class EditEntry : AppCompatActivity(), DialogDate.DialogListener, DialogDelete.DialogListener {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var location: String
    private var day = 0
    private var month = 0
    private var year = 0
    private var price = 0.0
    private lateinit var content: String
    private var image: Boolean? = null
    private lateinit var imagePath: String
    private lateinit var key: String
    private lateinit var date: String


    override fun onConfirmDelete(ret: Int) {
        if (ret == 0) {
        } else if (ret == 1) {
            val user = mAuth.currentUser
            val query = database.child("users/${user?.uid}/post/$key")
            query.removeValue()
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }


    override fun onReturnValue(day: Int, month: Int, year: Int) {
        this.day = day
        this.month = month
        this.year = year

        date = "$month/$day/$year"
        dateEVEN.setText(date)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_entry)
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        intent?.let {
            location = it.getStringExtra("location")
            day = it.getIntExtra("day", 0)
            month = it.getIntExtra("month", 0)
            year = it.getIntExtra("year", 0)
            price = it.getDoubleExtra("price", 0.0)
            content = it.getStringExtra("content")
            image = it.getBooleanExtra("image", false)
            imagePath = it.getStringExtra("imagePath")
            key = it.getStringExtra("key")
        }

        if (day == 0 || year == 0 || month == 0 || image == null) {
            loadingProgressBarEN.visibility = View.GONE
            cancel()
        } else {
            if (image as Boolean) {
                imageButtonEN.setImageBitmap(getBitmap(imagePath))
            }
            date = "$month/$day/$year"
            locationEVEN.setText(location)
            dateEVEN.setText(date)
            priceEVEN.setText(price.toString())
            if (content != resources.getString(R.string.nocontent)) {
                contentEVEN.setText(content)
            }
            loadingProgressBarEN.visibility = View.GONE
        }

    }


    private fun camera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, CAMERA_REC)
            }
        }
    }

    private fun pickDate() {
        val newFragment = DialogDate()
        newFragment.show(supportFragmentManager, "datePicker")
    }

    private fun edit() {
        loadingProgressBarEN.visibility = View.VISIBLE
        location = locationEVEN.text.toString()
        var priceString = priceEVEN.text.toString()
        content = contentEVEN.text.toString()

        if (content == "") {
            content = resources.getString(R.string.nocontent)
        }

        if (location == "" || priceString == "") {
            loadingProgressBarEN.visibility = View.GONE
            val errorDialog = DialogEmpty.newInstance(getString(R.string.errorBlankAE))
            errorDialog.show(supportFragmentManager, "errorFragment")
        } else {
            val dec = DecimalFormat("#.00")
            price = priceString.toDouble()
            val priceFormatted = dec.format(price.toDouble()).toDouble()
            val user = mAuth.currentUser
            val entry = Entry(location, day, month, year, priceFormatted, content, image, imagePath, key)
            val entryValues = entry.toMap()
            val childUpdates = HashMap<String, Any>()
            childUpdates["/users/${user?.uid}/post/$key"] = entryValues
            database.updateChildren(childUpdates)

            loadingProgressBarEN.visibility = View.GONE
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }


    private fun delete() {
        val deleteDialog = DialogDelete()
        deleteDialog.show(supportFragmentManager, "dialogDelete")
    }

    private fun cancel() {
        val retIntent = Intent()
        setResult(Activity.RESULT_CANCELED, retIntent)
        finish()
    }


    private fun getBitmap(src: String): Bitmap {
        val encodeByte = Base64.decode(src, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
    }

    fun click(v: View) {
        when (v) {
            imageButtonEN -> camera()
            imageButton2EN -> pickDate()
            addButtonEN -> edit()
            cancelTVEN -> cancel()
            deleteButtonEN -> delete()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REC && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageButtonEN.setImageBitmap(imageBitmap)
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val b = baos.toByteArray()
            imagePath = Base64.encodeToString(b, Base64.DEFAULT)
            image = true
        }
    }


}
