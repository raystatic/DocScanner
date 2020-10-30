package com.example.docscanner.data.repositories

import com.example.docscanner.data.local.PdfFile
import com.example.docscanner.data.local.PdfFileDao
import javax.inject.Inject

class PdfFileRepository @Inject constructor(
    val pdfFileDao: PdfFileDao
){

    suspend fun insertPdfFile(pdfFile: PdfFile) = pdfFileDao.insertPdfFile(pdfFile)

    fun getAllPdfFiles() = pdfFileDao.getAllPdfFiles()

    suspend fun deletePdfFileById(id: String) = pdfFileDao.deletePdfById(id)

    suspend fun deleteAllPdfFiles() = pdfFileDao.deleteAllPdfFiles()

}