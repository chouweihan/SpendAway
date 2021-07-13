package com.weihan.chou.spendaway

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.print.PrintManager
import android.support.v7.app.AlertDialog
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.nightonke.boommenu.BoomButtons.HamButton.Builder
import com.nightonke.boommenu.BoomButtons.OnBMClickListener
import kotlinx.android.synthetic.main.activity_main_page.*
import android.view.LayoutInflater
import android.widget.AdapterView.OnItemClickListener
import kotlinx.android.synthetic.main.dialogimage_layout.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat


const val ADD_REC = 99
const val EDIT_REC = 1023
const val SORT_REC = 2930

class MainPage : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private var list = ArrayList<EntryPH>()
    private var sortedList = ArrayList<EntryPH>()
    private lateinit var entry: EntryPH
    private lateinit var database: DatabaseReference
    private var totalAmt: Double = 0.0
    private var totalNum: Int = 0
    private var sortOption = 0
    private var minDateSet = false
    private var maxDateSet = false
    private var minDate = "0/0/0"
    private var maxDate = "0/0/0"
    private var minPriceSet = false
    private var maxPriceSet = false
    private var minPrice = 0.0
    private var maxPrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)
        ProgressBarMP.visibility = View.GONE
        menu()

        mAuth = FirebaseAuth.getInstance();

        lvEntry.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            entry = sortedList[position]
            val factory = LayoutInflater.from(this)
            val viewImage = factory.inflate(R.layout.dialogimage_layout, null)
            val image = viewImage.dialogImageID

            if (entry.im) {
                val imagePath = entry.imagePath
                val encodeByte = Base64.decode(imagePath, Base64.DEFAULT)
                val imSrc = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
                image.setImageBitmap(imSrc)
            } else {
                image.setImageResource(R.drawable.starz2)
            }

            val builder = AlertDialog.Builder(this)
                .setPositiveButton(
                    resources.getString(R.string.close)
                ) { _, _ ->
                    // closes dialog and does nothing
                }

            var dialog = builder.create()
            dialog.setView(viewImage, 0, 0, 0, 0)
            dialog.show()
        }


        //long click = delete dialog
        lvEntry.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->
                entry = sortedList[position]

                val pLoc = entry.location
                val pDay = entry.day
                val pMonth = entry.month
                val pYear = entry.year
                val pPrice = entry.price
                val pContent = entry.content
                val pIm = entry.image
                val pImP = entry.imagePath
                val pEntKey = entry.entryKey


                val intent = Intent(this@MainPage, EditEntry::class.java)
                intent.putExtra("location", pLoc)
                intent.putExtra("day", pDay)
                intent.putExtra("month", pMonth)
                intent.putExtra("year", pYear)
                intent.putExtra("price", pPrice)
                intent.putExtra("content", pContent)
                intent.putExtra("image", pIm)
                intent.putExtra("imagePath", pImP)
                intent.putExtra("key", pEntKey)

                startActivityForResult(intent, EDIT_REC)

                true
            }

        refreshList()
    }   //end on create


    private fun refreshList() {

        ProgressBarMP.visibility = View.VISIBLE
        Thread(Runnable {

            database = FirebaseDatabase.getInstance().reference
            val user = mAuth.currentUser
            val uid = user?.uid.toString()
            val posts = database.child("users/$uid/post")



            posts.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    list = ArrayList<EntryPH>()
                    sortedList = ArrayList<EntryPH>()
                    totalAmt = 0.0
                    totalNum = 0

                    for (i in dataSnapshot.children) {
                        val entryKey = i.child("entryKey").value.toString()
                        val content = i.child("content").value.toString()
                        val day = i.child("day").value.toString().toInt()
                        val month = i.child("month").value.toString().toInt()
                        val year = i.child("year").value.toString().toInt()
                        val image = i.child("image").value.toString().toBoolean()
                        val imagePath = i.child("imagePath").value.toString()
                        val location = i.child("location").value.toString()
                        val price = i.child("price").value.toString().toDouble()

                        val tempEntry = EntryPH(location, day, month, year, price, content, image, imagePath, entryKey)

                        if (sortDateConstraint("$month/$day/$year") && sortPriceConstraint(price)) {
                            totalAmt += price
                            totalNum++
                            list.add(tempEntry)
                        }
                    }


                    var tempList = when (sortOption) {
                        0 -> sortByDateDesc(list)
                        1 -> sortByDateDesc(list)
                        2 -> sortByPriceDesc(list)
                        3 -> sortByPriceDesc(list)
                        else -> null
                    }

                    for (i in tempList.orEmpty()) {
                        sortedList.add(i)
                    }

                    setAdapter()
                }

                override fun onCancelled(error: DatabaseError) {
                    //failed to read
                }
            })
        }).start()
    } // end refresh list

    fun setAdapter() {
        runOnUiThread {
            lvEntry.adapter = ListAdapter(this@MainPage, sortedList)
            setTotal()
            ProgressBarMP.visibility = View.GONE
        }
    }

    fun sortPriceConstraint(entryPr: Double) : Boolean {
        if (minPriceSet)
            if (entryPr < minPrice)
                return false


        if (maxPriceSet)
            if (entryPr > maxPrice)
                return false

        return true
    }

    fun sortDateConstraint(entryDa: String): Boolean {
        var dateFormat = SimpleDateFormat("MM/dd/yyyy")
        var dateSrc = dateFormat.parse(entryDa)
        var targetD1 = dateFormat.parse(minDate)
        var targetD2 = dateFormat.parse(maxDate)

        if (minDateSet)
            if (dateSrc < targetD1)
                return false

        if (maxDateSet)
            if (dateSrc > targetD2)
                return false

        return true
    } //end sort date constraint

    private fun refresh() {
        minDate = "0/0/0"
        maxDate = "0/0/0"
        maxDateSet = false
        minDateSet = false
        sortOption = 0
        minPriceSet = false
        maxPriceSet = false
        minPrice = 0.0
        maxPrice = 0.0
        refreshList()
    }

    fun sortByDateDesc(unsortedList: ArrayList<EntryPH>): List<EntryPH> {
        return if (sortOption == 0) {
             unsortedList.sortedWith(compareBy({ it.year }, { it.month }, { it.day })).reversed()
        } else {
            unsortedList.sortedWith(compareBy({ it.year }, { it.month }, { it.day }))
        }

    }

    fun sortByPriceDesc(unsortedList: ArrayList<EntryPH>): List<EntryPH> {
        return if (sortOption == 2) {
            unsortedList.sortedWith(compareBy { it.price }).reversed()
        } else
            unsortedList.sortedWith(compareBy { it.price })
    }

    private fun setTotal() {
        val dec = DecimalFormat("#.00")
        val amtDisplay = dec.format(totalAmt)
        val tAmtS = "${resources.getString(R.string.totalAmount)}$amtDisplay"
        val tNumS = "${resources.getString(R.string.totalNumber)}$totalNum"
        totalAmount.text = tAmtS
        totalNumber.text = tNumS
    }

    private fun menu() {

        val menuImage = resources.getStringArray(R.array.menuImage)
        val left = arrayOf(25, 25, 25, 25, 25)
        val top = arrayOf(25, 25, 25, 25, 25)

        for (i in 0 until menu.piecePlaceEnum.pieceNumber()) {
            val builder = Builder()
                .imageRect(Rect(left[i], top[i], 150, 150))
                .imagePadding(Rect(5, 5, 5, 5))
                .normalImageRes(resources.getIdentifier(menuImage[i], "drawable", packageName))
                .normalTextRes(resources.getIdentifier("menuOptions$i", "string", packageName))
                .subNormalTextRes(resources.getIdentifier("menuDesc$i", "string", packageName))
                .subTextSize(13)
                .textSize(20)
                .listener {
                    when (i) {
                        0 -> addEntry()
                        1 -> sortListActivity()
                        2 -> exportPdf()
                        3 -> refresh()
                        4 -> signOut()
                    }
                }
            menu.addBuilder(builder)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADD_REC || requestCode == EDIT_REC) {
            if (resultCode == Activity.RESULT_OK) {
                refreshList()
            } else {
                Toast.makeText(this@MainPage, "cancelled", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == SORT_REC) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    minDateSet = it.getBooleanExtra("dateMinSet", false)
                    maxDateSet = it.getBooleanExtra("dateMaxSet", false)
                    minPriceSet = it.getBooleanExtra("priceMinSet", false)
                    maxPriceSet = it.getBooleanExtra("priceMaxSet", false)
                    sortOption = it.getIntExtra("sortOption", 0)

                    if (minDateSet)
                        minDate = it.getStringExtra("dateMin")


                    if (maxDateSet)
                        maxDate = it.getStringExtra("dateMax")


                    if(minPriceSet)
                        minPrice = it.getDoubleExtra("priceMin", 0.0)


                    if(maxPriceSet)
                        maxPrice = it.getDoubleExtra("priceMax", 0.0)
                }

                refreshList()
            }
        }
    }

    private fun addEntry() {
        val intent = Intent(this, AddEntry::class.java)
        startActivityForResult(intent, ADD_REC)
    }

    private fun sortListActivity() {
        val intent = Intent(this@MainPage, SortingActivity::class.java)
        startActivityForResult(intent, SORT_REC)
    }

    private fun exportPdf() {
        if (sortedList.size == 0) {
            val errorDialog = DialogEmpty.newInstance(getString(R.string.cantexport))
            errorDialog.show(supportFragmentManager, "errorFragment")
        } else {
            val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
            printManager.print(
                "print_pdf", ViewPrintAdapter(this, sortedList, totalAmt), null
            )
        }
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                val intent = Intent(this@MainPage, MainActivity::class.java)
                startActivity(intent)
            }
    }
}
