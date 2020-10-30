package com.example.docscanner.ui.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.docscanner.R
import com.example.docscanner.other.CameraXUtility
import com.example.docscanner.ui.adapters.PdfItemAdapter
import com.example.docscanner.ui.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.io.File


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), PdfItemAdapter.PdfItemListener {

    private val vm:HomeViewModel by viewModels()
    private lateinit var pdfItemAdapter: PdfItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpRv()

        subscribeToObservers()

        menu_cam.setOnClickListener {
            menu_action.close(true)
            navigateToCameraFragment()
        }

        menu_gallery.setOnClickListener {
            menu_action.close(true)
            openGallery()
        }

    }

    private fun openGallery() {
        startActivity(Intent(this,OpenFromGalleryActivity::class.java))
    }


    override fun onResume() {
        super.onResume()
        vm.fetchPdfFiles(CameraXUtility.getOutputDirectory(this))
    }

    private fun setUpRv() {
        pdfItemAdapter = PdfItemAdapter(this)
        rvPdfFiles.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = pdfItemAdapter
        }
    }

    private fun subscribeToObservers() {
        vm.pdfFiles.observe(this, Observer {
            it?.let {files->
                pdfItemAdapter.setData(files)
            }
        })
    }


    private fun navigateToCameraFragment() {
        startActivity(Intent(this, CameraActivity::class.java))
    }


    override fun openPdf(file: File) {
        val target = Intent(Intent.ACTION_VIEW)
        target.setDataAndType(FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file), "application/pdf")
        target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        val intent = Intent.createChooser(target, "Open File")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No application found to open pdf", Toast.LENGTH_SHORT).show()
        }
    }
}