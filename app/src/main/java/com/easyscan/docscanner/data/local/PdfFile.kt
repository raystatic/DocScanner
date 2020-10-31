package com.easyscan.docscanner.data.local

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File
import java.util.*

@Entity(tableName = "pdf_file")
data class PdfFile(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id:String,

    @ColumnInfo(name = "filename")
    val fileName:String,

    @ColumnInfo(name = "date_created")
    val dateCreated:String,

    @ColumnInfo(name = "file")
    var  file:File
)