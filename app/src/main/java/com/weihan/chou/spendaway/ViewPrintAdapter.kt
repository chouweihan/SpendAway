package com.weihan.chou.spendaway

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.*
import java.text.DecimalFormat


class ViewPrintAdapter(
    private val mContext: Context,
    private val list: ArrayList<EntryPH>,
    private val amount: Double
) : PrintDocumentAdapter() {

    private var mDocument: PrintedPdfDocument? = null
    private var size = list.size
    private var count = 4
    private var finish = false
    private var pageCount: Int = ((size + 3.9) / 4).toInt()


    override fun onLayout(
        oldAttributes: PrintAttributes, newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback, extras: Bundle
    ) {

        if (size < 4) {
            count = size
        }

        mDocument = PrintedPdfDocument(mContext, newAttributes)

        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }


        val builder = PrintDocumentInfo.Builder(mContext.getString(R.string.print_name_file))
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(pageCount)

        val info = builder.build()
        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<PageRange>, destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {

        var counter = 0

        for (i in 0 until pageCount) {
            // Start the page
            val page = mDocument!!.startPage(i)

            val textPaint = Paint()
            textPaint.setARGB(255, 0, 0, 0)
            textPaint.textSize = 30f


            val pageCanvas = page.canvas

            //for each page
            for (j in 0 until count) {

                if (counter == size) {
                    pageCanvas.drawText("Total: $${amount}", 50f, 730f, textPaint)
                    finish = true
                    break;
                }

                pageCanvas.drawText("${counter + 1}.", 50f, 50f + (170 * j), textPaint)
                pageCanvas.drawText("${list[counter].location}", 150f, 50f + (170 * j), textPaint)
                pageCanvas.drawText("${list[counter].content}", 150f, 85f + (170 * j), textPaint)
                pageCanvas.drawText("$${list[counter].price}", 150f, 120f + (170 * j), textPaint)
                pageCanvas.drawText(
                    "${list[counter].month}/${list[counter].day}/${list[counter].year}",
                    150f,
                    155f + (170 * j),
                    textPaint
                )
                counter++
            }

            //adds amount to end of last page
            if (!finish && counter == size) {
                val dec = DecimalFormat("#.00")
                val tAmt = dec.format(amount)
                pageCanvas.drawText("Total: $$tAmt", 50f, 730f, textPaint)
            }

            //finishes page
            mDocument!!.finishPage(page)

        }
        try {
            mDocument!!.writeTo(
                FileOutputStream(
                    destination.fileDescriptor
                )
            )
        } catch (e: IOException) {
            callback.onWriteFailed(e.toString())
            return
        } finally {
            mDocument!!.close()
            mDocument = null
        }
        callback.onWriteFinished(arrayOf(PageRange(0, pageCount - 1)))
    }
}

