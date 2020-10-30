package com.example.docscanner.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber
import java.io.File

class HomeViewModel : ViewModel(){

    private val _pdfFiles = MutableLiveData<ArrayList<File>>()

    val pdfFiles:LiveData<ArrayList<File>>
        get() = _pdfFiles

    fun fetchPdfFiles(dir:File){
        val filesList = ArrayList<File>()
        val files = dir.listFiles()
        files?.forEach {file ->
            if (file.isDirectory){
                fetchPdfFiles(file)
            }else{
                if (file.name.endsWith(".pdf")){
                    filesList.add(file)
                }
            }
        }
        _pdfFiles.postValue(filesList)
    }

}