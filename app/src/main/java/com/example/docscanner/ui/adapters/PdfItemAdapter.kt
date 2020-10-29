package com.example.docscanner.ui.adapters

import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.docscanner.R
import kotlinx.android.synthetic.main.pdf_item_layout.view.*
import timber.log.Timber
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*

class PdfItemAdapter(var pdfItemListener: PdfItemListener) : RecyclerView.Adapter<PdfItemAdapter.PdfItemViewHolder>(){

    private var data:List<File>?=null

    interface PdfItemListener{
        fun openPdf(file: File)
    }

    fun setData(list: List<File>){
        data = list
        notifyDataSetChanged()
    }

    inner class PdfItemViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfItemViewHolder  = PdfItemViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.pdf_item_layout, parent, false)
    )

    override fun getItemCount(): Int = data?.size ?: 0

    override fun onBindViewHolder(holder: PdfItemViewHolder, position: Int) {

        val item = data?.get(position)

        holder.itemView.apply {
            tvPdfTitle.apply {
                text = item?.name
                ellipsize = TextUtils.TruncateAt.MARQUEE
                setSingleLine()
                marqueeRepeatLimit = 10
                isFocusable = true
                setHorizontallyScrolling(true)
                isFocusableInTouchMode = true
                requestFocus()
            }

            Timber.d("last modified time : ${item?.lastModified()}")
            val formatter = SimpleDateFormat("dd/MM/yyyy")
            val dateString = formatter.format( Date(item?.lastModified()!!))
            tvPdfDateCreated.text = "Last Modified: $dateString"

            setOnClickListener {
                item.let { it1 -> pdfItemListener.openPdf(it1) }
            }

        }

    }
}