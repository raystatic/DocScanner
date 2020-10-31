package com.easyscan.docscanner.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PdfFileDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdfFile(pdfFile: PdfFile)

    @Query("SELECT * FROM pdf_file ORDER BY date_created DESC")
    fun getAllPdfFiles():LiveData<List<PdfFile>>

    @Query("DELETE from pdf_file WHERE id=:pdfFileId")
    suspend fun deletePdfById(pdfFileId: String)

    @Query("DELETE from pdf_file")
    suspend fun deleteAllPdfFiles()

}