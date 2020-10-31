package com.example.docscanner.ui.activities

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.docscanner.R
import com.example.docscanner.data.local.PdfFile
import com.example.docscanner.other.CameraXUtility
import com.example.docscanner.other.Constants
import com.example.docscanner.other.ViewExtension.hide
import com.example.docscanner.other.ViewExtension.show
import com.example.docscanner.ui.adapters.PdfItemAdapter
import com.example.docscanner.ui.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.create_pdf_confirmation.view.*
import kotlinx.android.synthetic.main.rename_file_dialog.*
import kotlinx.android.synthetic.main.rename_file_dialog.view.*
import timber.log.Timber
import java.io.File
import java.io.IOException


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), PdfItemAdapter.PdfItemListener {

    private val vm:HomeViewModel by viewModels()
    private lateinit var pdfItemAdapter: PdfItemAdapter
    private lateinit var renameDialog:Dialog
    private lateinit var renameDialogView:View
    private lateinit var deleteDialog: Dialog
    private lateinit var deleteDialogView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpRv()

        initDialogs()

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

    private fun initDialogs() {
        renameDialog = Dialog(this)
        renameDialogView = LayoutInflater.from(this).inflate(R.layout.rename_file_dialog, null)
        renameDialog.setContentView(renameDialogView)

        deleteDialog = Dialog(this)
        deleteDialogView = LayoutInflater.from(this).inflate(R.layout.create_pdf_confirmation, null)
        deleteDialog.setContentView(deleteDialogView)
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

        vm.isFileRenaming.observe(this, Observer { isFileRenaming ->
            if (isFileRenaming){
                if (renameDialog.isShowing){
                    renameDialogView.linRenamingFile.show()
                    renameDialogView.btnGoBack.isEnabled = false
                    renameDialogView.btnRename.isEnabled = false
                }
            }else{
                if (renameDialog.isShowing){
                    renameDialogView.linRenamingFile.hide()
                    renameDialogView.btnGoBack.isEnabled = true
                    renameDialogView.btnRename.isEnabled = true
                    renameDialog.cancel()
                }
            }
        })

        vm.isFileDeleting.observe(this, Observer { isFileDeleting ->
            if (isFileDeleting){
                if (deleteDialog.isShowing){
                    deleteDialogView.linCreatingPdf.show()
                    deleteDialogView.btnCancelConfirm.isEnabled = false
                    deleteDialogView.btnDoneConfirm.isEnabled = false
                }
            }else{
                if (deleteDialog.isShowing){
                    deleteDialogView.linCreatingPdf.hide()
                    deleteDialogView.btnCancelConfirm.isEnabled = true
                    deleteDialogView.btnDoneConfirm.isEnabled = true
                    deleteDialog.cancel()
                }
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

    override fun rename(item: PdfFile?) {
        Timber.d("file details: ${item?.file?.path} ${item?.file}")

        renameDialogView.apply {
            val fileName = item?.fileName?.split(".")?.get(0)
            etFileName.setText(fileName.toString())

            btnGoBack.setOnClickListener {
                renameDialog.cancel()
            }

            btnRename.setOnClickListener {
                var name = etFileName.text.toString()

                if (name.isEmpty()){
                    etFileName.error = "Cannot be empty"
                    return@setOnClickListener
                }

                if (CameraXUtility.isContainsSpecialCharacter(name)){
                    etFileName.error = "Cannot contain special characters"
                    return@setOnClickListener
                }

                etFileName.setError(null)

                if (name.endsWith(".pdf")){
                    name = name.split(".pdf")[0]
                }

                Timber.d("filename: $name")

                val fromfile = File(CameraXUtility.getOutputDirectory(this@MainActivity),item?.fileName.toString())
                val newFile = File(CameraXUtility.getOutputDirectory(this@MainActivity),"$name.pdf")
                try {
                    Timber.d("file last modified: ${newFile.lastModified()}")
                    val newPdfFile = PdfFile(fileName = newFile.name,dateCreated = System.currentTimeMillis().toString(),file = newFile, id = newFile.absolutePath)
                    vm.renameFile(fromfile, newFile,item)
                    vm.insertPdfFile(newPdfFile)

                }catch (e:IOException){
                    Toast.makeText(this@MainActivity, Constants.SOMETHING_WENT_WRONG, Toast.LENGTH_SHORT).show()
                }
            }

        }

        renameDialog.show()

        val window = renameDialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

    }

    override fun deleteFile(item: PdfFile?) {
        showDeleteConfirmDialog(item)
    }

    private fun showDeleteConfirmDialog(pdfFile: PdfFile?) {

        deleteDialogView.apply {

            tvConfirmTitle.text = Constants.DELETE_PDF_TITLE

            btnCancelConfirm.setOnClickListener {
                deleteDialog.cancel()
            }

            btnDoneConfirm.setOnClickListener {
                vm.deletePdfFile(pdfFile)
            }

        }

        deleteDialog.show()
        val window = deleteDialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

    }
}