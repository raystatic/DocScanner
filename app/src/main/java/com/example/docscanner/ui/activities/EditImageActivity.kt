package com.example.docscanner.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.docscanner.R
import com.example.docscanner.data.models.Document
import com.example.docscanner.other.CameraXUtility
import com.example.docscanner.ui.adapters.EditImageAdapter
import com.example.docscanner.ui.viewmodels.CameraViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_edit_image.*
import java.util.ArrayList

@AndroidEntryPoint
class EditImageActivity : AppCompatActivity(), EditImageAdapter.EditImageListener {

    private lateinit var editImageAdapter: EditImageAdapter

    private val vm: CameraViewModel by viewModels()

    private var docList:ArrayList<Document>?= ArrayList<Document>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_image)

        intent?.let {
            docList = it.getParcelableArrayListExtra("docs")
        }

        setUpEditImageAdapter()

        subscribeToObservers()

        clickFirstItem()

    }

    private fun clickFirstItem() {
        rvImages.findViewHolderForAdapterPosition(0)?.itemView?.performClick()
    }

    private fun subscribeToObservers() {
        vm.docList.observe(this, Observer {
            it?.let {
                editImageAdapter.setData(it)
            }
        })
    }

    private fun setUpEditImageAdapter() {
        editImageAdapter = EditImageAdapter(this, this)
        rvImages.apply {
            layoutManager = LinearLayoutManager(this@EditImageActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = editImageAdapter
        }
    }

    override fun onImageClicked(document: Document) {
        imgEditDoc.setImageBitmap(document.bitmap)
    }
}