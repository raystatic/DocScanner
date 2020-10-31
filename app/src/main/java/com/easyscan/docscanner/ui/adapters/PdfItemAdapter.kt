package com.easyscan.docscanner.ui.adapters

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.easyscan.docscanner.R
import com.easyscan.docscanner.data.local.PdfFile
import kotlinx.android.synthetic.main.pdf_item_layout.view.*
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PdfItemAdapter(var pdfItemListener: PdfItemListener) : RecyclerView.Adapter<PdfItemAdapter.PdfItemViewHolder>(){

    private var data:List<PdfFile>?=null

    interface PdfItemListener{
        fun openPdf(file: File)
        fun rename(item: PdfFile?)
        fun deleteFile(item: PdfFile?)
        fun shareFile(item: PdfFile?)
    }

    fun setData(list: List<PdfFile>){
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
                text = item?.fileName
                ellipsize = TextUtils.TruncateAt.MARQUEE
                setSingleLine()
                marqueeRepeatLimit = 10
                isFocusable = true
                setHorizontallyScrolling(true)
                isFocusableInTouchMode = true
                requestFocus()
            }

            Timber.d("last modified time : ${item?.dateCreated}")
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)
            val dateString = formatter.format(item?.dateCreated?.toLong()?.let { Date(it) })
            tvPdfDateCreated.text = "Created on: $dateString"

            setOnClickListener {
                item?.file?.let { it1 -> pdfItemListener.openPdf(it1) }
            }

            imgPopup.apply {
                setOnClickListener {
                    val popup = PopupMenu(context, this)

                    popup.menuInflater
                        .inflate(R.menu.pdf_item_menu, popup.menu)

                    popup.setOnMenuItemClickListener {
                        when(it.itemId){
                            R.id.miRename ->{
                                pdfItemListener.rename(item)
                                return@setOnMenuItemClickListener true
                            }
                            R.id.miDelete -> {
                                pdfItemListener.deleteFile(item)
                                return@setOnMenuItemClickListener true
                            }
                            R.id.miShare -> {
                                pdfItemListener.shareFile(item)
                                return@setOnMenuItemClickListener true
                            }
                            else -> return@setOnMenuItemClickListener false
                        }
                    }

                    popup.show()
                }
            }

        }

    }
}