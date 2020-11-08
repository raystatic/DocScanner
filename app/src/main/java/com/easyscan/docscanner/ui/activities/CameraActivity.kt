package com.easyscan.docscanner.ui.activities


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.easyscan.docscanner.R
import com.easyscan.docscanner.data.models.Document
import com.easyscan.docscanner.other.CameraXUtility
import com.easyscan.docscanner.other.Constants
import com.easyscan.docscanner.ui.fragments.CaptureImageFragment
import com.easyscan.docscanner.ui.fragments.EditImageFragment
import com.easyscan.docscanner.ui.viewmodels.CameraViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_camera.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CameraActivity : AppCompatActivity(),
        CaptureImageFragment.CaptureImageInteractor,
        EditImageFragment.EditImageInteractor
{
    private val vm: CameraViewModel by  viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        shouldOpenFromGallery = intent?.getBooleanExtra(Constants.OPEN_FROM_GALLERY, false) ?: false


    }


    override fun onThumbClicked() {
        cameraNavHostFragment.findNavController().navigate(R.id.action_to_editImageFragment)
    }

    override fun onNavigateToCapture() {
        if (shouldOpenFromGallery){
            finish()
        }else{
            cameraNavHostFragment.findNavController().navigate(R.id.action_to_captureImageFragment)
        }
    }

    override fun onFinishFromEditImage(docList: List<Document>) {
        if (docList.isNotEmpty()){
            vm.deleteTempFiles(docList)
        }
        finish()
    }

    override fun onFinishFromCaptureImage(docList: List<Document>) {
        if (docList.isNotEmpty()){
            vm.deleteTempFiles(docList)
        }
        finish()
    }

    override fun onBackPressed() {
        if (cameraNavHostFragment.findNavController().currentDestination?.id == R.id.captureImageFragment){
            Timber.d("temp files: ${CaptureImageFragment.tempDocList}")
            if (CaptureImageFragment.tempDocList.isNotEmpty()){
                vm.deleteTempFiles(CaptureImageFragment.tempDocList)
            }
            finish()
        }
        else if (shouldOpenFromGallery){
            finish()
        }else
            super.onBackPressed()
    }

    companion object{
        var shouldOpenFromGallery:Boolean = false
    }
}