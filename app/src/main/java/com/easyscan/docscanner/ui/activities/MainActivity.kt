package com.easyscan.docscanner.ui.activities

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.easyscan.docscanner.R
import com.easyscan.docscanner.data.local.PdfFile
import com.easyscan.docscanner.other.CameraXUtility
import com.easyscan.docscanner.other.Constants
import com.easyscan.docscanner.other.ViewExtension.hide
import com.easyscan.docscanner.other.ViewExtension.show
import com.easyscan.docscanner.ui.adapters.PdfItemAdapter
import com.easyscan.docscanner.ui.viewmodels.HomeViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.create_pdf_confirmation.view.*
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

    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateToFirebase()

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

    private fun updateToFirebase() {
        try {
            val pInfo: PackageInfo =
                this.packageManager.getPackageInfo(this.packageName, 0)
            val version: String = pInfo.versionName
            val deviceDetails=  hashMapOf(
                "os version" to System.getProperty("os.version"),
                "api level" to android.os.Build.VERSION.SDK_INT,
                "device" to android.os.Build.DEVICE,
                "model" to android.os.Build.MODEL,
                "product" to android.os.Build.PRODUCT,
                "current version" to version
            )


            val uniqueId = Settings.Secure.getString(this.contentResolver,
                Settings.Secure.ANDROID_ID)
            db.collection("devices").document(uniqueId)
                .set(deviceDetails)
                .addOnSuccessListener { Timber.d("device details updated!") }
                .addOnFailureListener { e -> Timber.d("device details cannot be updated due to ${e.localizedMessage}") }

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
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
//        startActivity(Intent(this,OpenFromGalleryActivity::class.java))
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra(Constants.OPEN_FROM_GALLERY, true)
        startActivity(intent)
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
            addItemDecoration(DividerItemDecoration(this@MainActivity,LinearLayout.VERTICAL))
        }
    }

    private fun subscribeToObservers() {
        vm.pdfFiles.observe(this, Observer {
            it?.let {files->
                if (files.isNotEmpty()){
                    linNoFiles.hide()
                    rvPdfFiles.show()
                    pdfItemAdapter.setData(files)
                }else{
                    rvPdfFiles.hide()
                    linNoFiles.show()
                }

            }
        })

        vm.isFileRenaming.observe(this, Observer { isFileRenaming ->
            if (isFileRenaming){
                if (renameDialog.isShowing){
                    renameDialogView.linRenamingFile.show()
                    renameDialogView.btnGoBack.isEnabled = false
                    renameDialogView.btnRename.isEnabled = false
                    renameDialogView.btnGoBack.setBackgroundColor(ContextCompat.getColor(this,R.color.custom_shadow_light_blue))
                    renameDialogView.btnRename.setBackgroundColor(ContextCompat.getColor(this,R.color.custom_shadow_light_blue))
                }
            }else{
                if (renameDialog.isShowing){
                    renameDialogView.linRenamingFile.hide()
                    renameDialogView.btnGoBack.isEnabled = true
                    renameDialogView.btnRename.isEnabled = true
                    renameDialogView.btnGoBack.setBackgroundColor(ContextCompat.getColor(this,R.color.custom_lightest_blue))
                    renameDialogView.btnRename.setBackgroundColor(ContextCompat.getColor(this,R.color.custom_shadow_dark_blue))
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
                    deleteDialogView.btnCancelConfirm.setBackgroundColor(ContextCompat.getColor(this,R.color.custom_shadow_light_blue))
                    deleteDialogView.btnDoneConfirm.setBackgroundColor(ContextCompat.getColor(this,R.color.custom_shadow_light_blue))
                }
            }else{
                if (deleteDialog.isShowing){
                    deleteDialogView.linCreatingPdf.hide()
                    deleteDialogView.btnCancelConfirm.isEnabled = true
                    deleteDialogView.btnDoneConfirm.isEnabled = true
                    deleteDialogView.btnCancelConfirm.setBackgroundColor(ContextCompat.getColor(this,R.color.custom_lightest_blue))
                    deleteDialogView.btnDoneConfirm.setBackgroundColor(ContextCompat.getColor(this,R.color.custom_shadow_dark_blue))
                    deleteDialog.cancel()
                }
            }
        })
    }


    private fun navigateToCameraFragment() {
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra(Constants.OPEN_FROM_GALLERY, false)
        startActivity(intent)
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

    override fun shareFile(item: PdfFile?) {
        CameraXUtility.sharePdf(FileProvider.getUriForFile(this,"${application.packageName}.provider",item?.file!!), this)
    }

    override fun deleteFile(item: PdfFile?) {
        showDeleteConfirmDialog(item)
    }

    private fun showDeleteConfirmDialog(pdfFile: PdfFile?) {

        deleteDialogView.apply {

            tvConfirmTitle.text = Constants.DELETE_PDF_TITLE
            tvConfirmProgress.text = Constants.DELETING_FILE

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