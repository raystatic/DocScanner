package com.example.docscanner.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docscanner.data.local.PdfFile
import com.example.docscanner.data.repositories.PdfFileRepository
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel @ViewModelInject constructor(
    private val pdfFileRepository: PdfFileRepository
) : ViewModel(){


    val pdfFiles = pdfFileRepository.getAllPdfFiles()

    fun insertPdfFile(pdfFile:PdfFile) = viewModelScope.launch {
        pdfFileRepository.insertPdfFile(pdfFile)
    }

    fun deletePdfFileById(id: String) = viewModelScope.launch {
        pdfFileRepository.deletePdfFileById(id)
    }

    fun deleteAllPdfFiles() = viewModelScope.launch {
        pdfFileRepository.deleteAllPdfFiles()
    }

    fun fetchPdfFiles(dir:File){
        val filesList = ArrayList<File>()
        val files = dir.listFiles()
        files?.forEach {file ->
            if (file.isDirectory){
                fetchPdfFiles(file)
            }else{
                if (file.name.endsWith(".pdf")){
                    filesList.add(file)
                    val pdfFile = PdfFile(fileName = file.name,dateCreated = file.lastModified().toString(),file = file, id = file.absolutePath)
                    insertPdfFile(pdfFile)
                }
            }
        }
    }

}