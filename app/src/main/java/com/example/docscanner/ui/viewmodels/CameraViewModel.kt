package com.example.docscanner.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.docscanner.data.models.Document

class CameraViewModel : ViewModel(){

    private val _docList = MutableLiveData<List<Document>>()

    val docList:LiveData<List<Document>>
        get() = _docList

    fun updateDocList(docList:List<Document>){
        _docList.postValue(docList)
    }

}