package com.example.docscanner.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.docscanner.R
import com.example.docscanner.data.models.Document
import com.example.docscanner.other.CameraXUtility
import com.example.docscanner.other.ViewExtension.hide
import com.example.docscanner.other.ViewExtension.show
import com.example.docscanner.ui.viewmodels.CameraViewModel
import kotlinx.android.synthetic.main.activity_open_from_gallery.*
import kotlinx.android.synthetic.main.create_pdf_confirmation.view.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OpenFromGalleryActivity : AppCompatActivity() {

    private val _docList = ArrayList<Document>()

    private val vm:CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_from_gallery)

        openGallery()

        subscribeToObservers()

    }

    private fun subscribeToObservers() {
        vm.isPdfCreating.observe(this, androidx.lifecycle.Observer {isPdfCreating ->
            if (isPdfCreating){
                linGalleryCreatingPdf.show()
            }else{
                linGalleryCreatingPdf.hide()
                Toast.makeText(this, "PDF created", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == Activity.RESULT_OK){
            _docList.clear()
            Timber.d("imagesList data: ${data?.data} ${data?.clipData}")
            val imagesPath: List<String>? = data?.getStringExtra("data")?.split("\\|")
            Timber.d("imagesList: $imagesPath")

            data?.data.let {
                it?.let { it1 ->
                    _docList.add(Document(CameraXUtility.getBitmapFromUri(it1,this),it1))
                }
            }

            val imagesSize = data?.clipData?.itemCount

            if (imagesSize != null) {
                for (i in 0 until imagesSize){
                    data.clipData?.getItemAt(i)?.uri?.let {
                        _docList.add(Document(CameraXUtility.getBitmapFromUri(it,this),it))
                    }
                }
            }

            if (_docList.isNotEmpty()){
                val filePath = "${CameraXUtility.getOutputDirectory(this)}/${SimpleDateFormat(
                    CameraXUtility.FILENAME, Locale.US
                ).format(System.currentTimeMillis())}.pdf"
                vm.createPdf(_docList,filePath)
            }
        }
    }
}