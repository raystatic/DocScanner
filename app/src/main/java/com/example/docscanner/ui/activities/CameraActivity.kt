package com.example.docscanner.ui.activities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.docscanner.R
import com.example.docscanner.data.models.Document
import com.example.docscanner.other.CameraXUtility
import com.example.docscanner.other.CameraXUtility.FILENAME
import com.example.docscanner.other.CameraXUtility.PHOTO_EXTENSION
import com.example.docscanner.other.CameraXUtility.getBitmapFromUri
import com.example.docscanner.other.CameraXUtility.rotatateImageIfRequired
import com.example.docscanner.other.Constants
import com.example.docscanner.other.Utility
import com.example.docscanner.ui.viewmodels.CameraViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_camera.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest


@AndroidEntryPoint
class CameraActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private val vm:CameraViewModel by viewModels()

    private lateinit var imageCapture:ImageCapture
    private lateinit var outputDirectory: File
    private var docList = mutableListOf<Document>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        requestRequiredPermissions()

        outputDirectory = CameraXUtility.getOutputDirectory(this)
        startCamera()

        fabCapture.setOnClickListener {
            takePhoto()
        }

        subscribeToObservers()


    }

    private fun subscribeToObservers() {

        vm.docList.observe(this, androidx.lifecycle.Observer {
            it?.let {docs ->
                if (docs.isNotEmpty()){
                    docList = docs as MutableList
                    val bmp = getBitmapFromUri(docList[0].uri, this@CameraActivity)
                    val bitmap = rotatateImageIfRequired(bmp, docList[0].uri)
                    docThumb.setImageBitmap(bitmap)
                }
            }
        })

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            AppSettingsDialog.Builder(this).build().show()
        }else{
            requestRequiredPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun requestRequiredPermissions() {
        if (Utility.hasCameraPermission(this))
            return

        EasyPermissions.requestPermissions(
                this,
                Constants.ASK_PERMISSION,
                Constants.REQUEST_PERMISSION_CODE,
                android.Manifest.permission.CAMERA
        )
    }



    private fun takePhoto() {
        val imageCapture = imageCapture

        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                    FILENAME, Locale.US
            ).format(System.currentTimeMillis()) + PHOTO_EXTENSION)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Timber.d("Photo capture failed: ${exc.message} : $exc")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Timber.d(msg)
                    Toast.makeText(this@CameraActivity, msg, Toast.LENGTH_SHORT).show()
                    docList.add(0,Document(savedUri))
                    vm.updateDocList(docList)
                }
            })
    }



    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(view_finder.surfaceProvider)
                }


            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Timber.e("Use case binding failed: $exc")
            }

        }, ContextCompat.getMainExecutor(this))
    }

}