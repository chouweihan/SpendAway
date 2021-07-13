package com.weihan.chou.spendaway

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker
import java.util.*


class DialogDate : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private var listener: DialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        listener?.onReturnValue(day, month + 1, year)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DialogListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface DialogListener {
        fun onReturnValue(day:Int, month:Int, year:Int)
    }
}

