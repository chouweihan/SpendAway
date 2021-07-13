package drawable

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.weihan.chou.spendaway.R


class DialogDelete : DialogFragment() {
    private var listener: DialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(getString(R.string.confirmation))
                .setPositiveButton(
                    R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener?.onConfirmDelete(0)
                    })
                .setNegativeButton(R.string.delete,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener?.onConfirmDelete(1)
                    })

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DialogListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface DialogListener {
        fun onConfirmDelete(ret: Int)
    }
}