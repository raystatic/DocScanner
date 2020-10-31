package com.easyscan.docscanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PdfFile::class],
    version = 1
)
@TypeConverters(
    Converters::class
)
abstract class DocScannerDb : RoomDatabase(){

    abstract fun getPdfDao():PdfFileDao

}