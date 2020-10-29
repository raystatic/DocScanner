package com.example.docscanner.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.docscanner.data.models.Document
import com.example.docscanner.ui.adapters.EditImageAdapter
import com.example.docscanner.ui.fragments.EditImageFragment

class CameraViewModel : ViewModel(){

    private val _docList = MutableLiveData<List<Document>>()

    val docList:LiveData<List<Document>>
        get() = _docList

    fun updateDocList(docList:List<Document>){
        _docList.postValue(docList)
    }

    fun removeItemAtPosition(selectedPosition: Int) {
        val temp = _docList.value as MutableList
        if (temp.isNotEmpty()){
            temp.removeAt(selectedPosition)
            _docList.postValue(temp)
            if (EditImageAdapter.selectedPosition>0 && EditImageFragment.retakePicture==-1)
                EditImageAdapter.selectedPosition--
        }
    }

    val currentFragmentVisible = MutableLiveData<Int>()


}