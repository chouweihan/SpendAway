package com.weihan.chou.spendaway

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_sorting.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat


class SortingActivity : AppCompatActivity(), DialogDate.DialogListener {

    private var isDate = false
    private var min = false
    private var dateMinSet = false
    private var dateMaxSet = false
    private lateinit var dateMin: String
    private lateinit var dateMax: String
    private var rbChecked = 0
    private var priceMinSet = false
    private var priceMaxSet = false
    private lateinit var priceMin: String
    private lateinit var priceMax: String

    override fun onReturnValue(day: Int, month: Int, year: Int) {

        if (min) {
            dateMin = "$month/$day/$year"
            minDateTV.text = dateMin
            dateMinSet = true
        } else if (!min) {
            dateMax = "$month/$day/$year"
            maxDateTV.text = dateMax
            dateMaxSet = true
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sorting)
    }


    private fun switchDate() {
        isDate = !isDate

        val animGroup = findViewById<RelativeLayout>(R.id.animatedLayout)

        if (isDate) {
            animGroup.animate().apply {
                yBy(350f)
                duration = 500
            }.setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    layout_date.visibility = View.VISIBLE
                    dateSwitchID.isClickable = true
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                    dateSwitchID.isClickable = false
                }
            })

        } else {
            animGroup.animate().apply {
                yBy(-350f)
                duration = 500
            }.setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    dateSwitchID.isClickable = true
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                    layout_date.visibility = View.GONE
                    dateSwitchID.isClickable = false
                }
            })
        }

    }


    private fun apply() {
        val intent = Intent()
        val dec = DecimalFormat("#.00")
        var priceMinPass = 0.0
        var priceMaxPass = 0.0
        var pass = true
        var errorMsg = ""

        priceMax = maxCost.text.toString()
        priceMin = minCost.text.toString()



        if (priceMax != "") {
            priceMaxSet = true
            priceMaxPass = dec.format(priceMax.toDouble()).toDouble()
            intent.putExtra("priceMax", priceMaxPass)
        }

        if (priceMin != "") {
            priceMinSet = true
            priceMinPass = dec.format(priceMin.toDouble()).toDouble()
            intent.putExtra("priceMin", priceMinPass)
        }

        val radioButtonID = groupID.checkedRadioButtonId
        val radioButton = findViewById<RadioButton>(radioButtonID)
        val index = groupID.indexOfChild(radioButton)
        rbChecked = index

        intent.putExtra("sortOption", rbChecked)
        intent.putExtra("dateMinSet", dateMinSet)
        intent.putExtra("dateMaxSet", dateMaxSet)
        intent.putExtra("priceMaxSet", priceMaxSet)
        intent.putExtra("priceMinSet", priceMinSet)



        if (dateMinSet) {
            intent.putExtra("dateMin", dateMin)
        }

        if (dateMaxSet) {
            intent.putExtra("dateMax", dateMax)
        }

        if (dateMinSet && dateMaxSet) {

            var dateFormat = SimpleDateFormat("MM/dd/yyyy")
            var d1 = dateFormat.parse(dateMin)
            var d2 = dateFormat.parse(dateMax)

            if (d2 < d1) {
                errorMsg = getString(R.string.errorDateOptions)
                pass = false
            }
        }

        if (dateMaxSet && dateMinSet) {
            if (priceMaxPass < priceMinPass) {
                errorMsg = getString(R.string.errorMaxtoMin)
                pass = false
            }
        }

        if(pass) {
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            val errorDialog = DialogEmpty.newInstance(errorMsg)
            errorDialog.show(supportFragmentManager, "errorFragment")
        }
    }


    private fun pickDate(identity: Boolean) {
        min = identity
        val newFragment = DialogDate()
        newFragment.show(supportFragmentManager, "datePicker")
    }

    private fun reset() {
        dateMaxSet = false
        dateMinSet = false
        minDateTV.text = resources.getString(R.string.min_date)
        maxDateTV.text = resources.getString(R.string.max_date)
        rb1.isChecked = true
        rb2.isChecked = false
        rb3.isChecked = false
        rb4.isChecked = false
        rbChecked = 0
        priceMaxSet = false
        priceMinSet = false
        minCost.setText("")
        maxCost.setText("")
    }

    private fun cancel() {
        val intent = Intent()
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    fun click(v: View) {
        when (v) {
            dateSwitchID -> switchDate()
            minDateBtn -> pickDate(true)
            maxDateBtn -> pickDate(false)
            applyBtn -> apply()
            resetBtn -> reset()
            cancelPref -> cancel()
        }
    }
}
