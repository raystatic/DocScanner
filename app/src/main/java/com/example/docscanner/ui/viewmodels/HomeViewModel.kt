package com.example.docscanner.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docscanner.data.local.PdfFile
import com.example.docscanner.data.repositories.PdfFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class HomeViewModel @ViewModelInject constructor(
    private val pdfFileRepository: PdfFileRepository
) : ViewModel(){

    private val _isFileRenaming = MutableLiveData<Boolean>()
    private val _isFileDeleting = MutableLiveData<Boolean>()

    init {
        _isFileDeleting.postValue(false)
        _isFileRenaming.postValue(false)
    }

    val isFileRenaming:LiveData<Boolean>
        get() = _isFileRenaming

    val isFileDeleting:LiveData<Boolean>
        get() = _isFileDeleting

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

    fun renameFile(src: File?, dst: File, originalPdfFile: PdfFile?) = viewModelScope.launch {
        copyFileInBackground(src, dst, originalPdfFile)
    }

    @Throws(IOException::class)
    private suspend fun copyFileInBackground(src: File?, dst: File, originalPdfFile: PdfFile?)  = withContext(Dispatchers.IO) {
        _isFileRenaming.postValue(true)
        FileInputStream(src).use { `in` ->
            FileOutputStream(dst).use { out ->
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
        }

        deleteByFileInBackground(originalPdfFile)

        withContext(Dispatchers.Main){
            _isFileRenaming.postValue(false)
        }
    }

    fun deletePdfFile(pdfFile: PdfFile?) = viewModelScope.launch {
        deleteByFileInBackground(pdfFile)
    }

    private suspend fun deleteByFileInBackground(pdfFile: PdfFile?) = withContext(Dispatchers.IO){
        _isFileDeleting.postValue(true)
        val file = File(pdfFile?.file?.toURI())
        file.delete()
        pdfFile?.id?.let {
            deletePdfFileById(it)
        }

        withContext(Dispatchers.Main){
            _isFileDeleting.postValue(false)
        }
    }

}