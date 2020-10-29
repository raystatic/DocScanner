package com.example.docscanner.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.docscanner.R
import com.example.docscanner.data.models.Document
import com.example.docscanner.other.CameraXUtility
import com.example.docscanner.other.Constants
import com.example.docscanner.other.Utility
import com.example.docscanner.other.ViewExtension.hide
import com.example.docscanner.other.ViewExtension.show
import com.example.docscanner.ui.viewmodels.CameraViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.capture_image_fragment.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class CaptureImageFragment: Fragment(R.layout.capture_image_fragment), EasyPermissions.PermissionCallbacks{

    private val vm: CameraViewModel by activityViewModels()

    private lateinit var imageCapture: ImageCapture
    private lateinit var outputDirectory: File
    private var docList = ArrayList<Document>()

    private lateinit var callback: CaptureImageInteractor

    @Inject
    lateinit var glide: RequestManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestRequiredPermissions()

        outputDirectory = CameraXUtility.getOutputDirectory(requireContext())
        startCamera()

        fabCapture.setOnClickListener {
            takePhoto()
        }

        docThumb.setOnClickListener {
            Timber.d("capture image: $callback")
            callback.onThumbClicked()
        }

        subscribeToObservers()

    }

    private fun subscribeToObservers() {

        vm.docList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {docs ->
                if (docs.isNotEmpty()){
                    docList = docs as ArrayList
                    val document = docList[docList.size -1]
                    glide.apply {
                        load(document.bitmap)
                                .into(docThumb)
                    }
                    if (isImageRetaken){
                        isImageRetaken = false
                        callback.onThumbClicked()
                    }
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
        if (Utility.hasCameraPermission(requireContext()))
            return

        EasyPermissions.requestPermissions(
                this,
                Constants.ASK_PERMISSION,
                Constants.REQUEST_PERMISSION_CODE,
                android.Manifest.permission.CAMERA
        )
    }



    private fun takePhoto() {
        fabCapture.isEnabled = false
        captureProgress.show()
        val imageCapture = imageCapture

        val photoFile = File(
                outputDirectory,
                SimpleDateFormat(
                        CameraXUtility.FILENAME, Locale.US
                ).format(System.currentTimeMillis()) + CameraXUtility.PHOTO_EXTENSION)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
                outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Timber.d("Photo capture failed: ${exc.message} : $exc")
                fabCapture.isEnabled = true
                captureProgress.hide()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                val msg = "Photo capture succeeded: $savedUri"
                Timber.d(msg)
                val bmp = CameraXUtility.getBitmapFromUri(savedUri, requireContext())
                val bitmap = CameraXUtility.rotatateImageIfRequired(bmp, savedUri)
                //bitmap?.let { CameraXUtility.insertBitmapAtUri(savedUri,requireContext(), it) }
                val doc = Document(bitmap,savedUri)
                if (EditImageFragment.retakePicture != -1){
                    docList.add(EditImageFragment.retakePicture, doc)
                    EditImageFragment.retakePicture = -1
                    isImageRetaken = true
                }else{
                    docList.add(doc)
                }
                vm.updateDocList(docList)
                fabCapture.isEnabled = true
                captureProgress.hide()
            }
        })
    }



    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

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

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    interface CaptureImageInteractor {
        fun onThumbClicked()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as CaptureImageInteractor

        vm.currentFragmentVisible.value = 0
    }

    companion object{
        private var isImageRetaken = false
    }

}
