package com.weihan.chou.spendaway

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment

private const val ARG_PARAM1 = "param1"

class DialogEmpty : DialogFragment() {

    private var display: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        display = arguments?.getString(ARG_PARAM1) ?: throw IllegalStateException("No args provided")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(display)
                .setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                         // closes dialog and does nothing
                    })

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            DialogEmpty().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}