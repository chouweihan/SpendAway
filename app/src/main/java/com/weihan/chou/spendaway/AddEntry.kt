package com.weihan.chou.spendaway

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_add_entry.*
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import kotlin.collections.HashMap
import android.util.Base64
import java.text.DecimalFormat


const val CAMERA_REC = 152

class AddEntry : AppCompatActivity(), DialogDate.DialogListener {
    private lateinit var imagePath: String
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var image = false

    private var day = 0
    private var month = 0
    private var year = 0
    private lateinit var date: String

    override fun onReturnValue(day: Int, month: Int, year: Int) {
        this.day = day
        this.month = month
        this.year = year

        date = "$month/$day/$year"
        dateEV.setText(date)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry)
        loadingProgressBarAE.visibility = View.GONE
        mAuth = FirebaseAuth.getInstance();
        dateEV.isEnabled = false

    }


    private fun camera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, CAMERA_REC)
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REC && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageButton.setImageBitmap(imageBitmap)
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val b = baos.toByteArray()
            imagePath = Base64.encodeToString(b, Base64.DEFAULT)
            image = true
        }
    }

    private fun add() {
        loadingProgressBarAE.visibility = View.VISIBLE
        val location = locationEV.text.toString()
        val price = priceEV.text.toString()
        var content = contentEV.text.toString()


        if (content == "") {
            content = resources.getString(R.string.nocontent)
        }

        if (day == 0 || location == "" || price == "") {
            loadingProgressBarAE.visibility = View.GONE
            val errorDialog = DialogEmpty.newInstance(getString(R.string.errorBlankAE))
            errorDialog.show(supportFragmentManager, "errorFragment")
        } else {

            if(!image) {
                imagePath = "none"
            }

            val user = mAuth.currentUser
            database = FirebaseDatabase.getInstance().reference

            val key = database.child("users").child("${user?.uid}").push().key
            val dec = DecimalFormat("#.00")
            val priceFormatted = dec.format(price.toDouble()).toDouble()

            val entry = Entry(location, day, month, year, priceFormatted, content, image, imagePath, key)
            val entryValues = entry.toMap()
            val childUpdates = HashMap<String, Any>()
            childUpdates["/users/${user?.uid}/post/$key"] = entryValues
            database.updateChildren(childUpdates)

            loadingProgressBarAE.visibility = View.GONE

            val intent = Intent(this@AddEntry, MainPage::class.java)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }

    private fun pickDate() {
        val newFragment = DialogDate()
        newFragment.show(supportFragmentManager, "datePicker")
    }

    private fun cancel() {
        val intent = Intent(this@AddEntry, MainPage::class.java)
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    fun click(v : View) {
        when(v) {
            imageButton -> camera()
            imageButton2 -> pickDate()
            addButton -> add()
            cancelTV -> cancel()
        }
    }

}
