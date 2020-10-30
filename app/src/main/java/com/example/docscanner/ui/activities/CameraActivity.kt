package com.example.docscanner.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.docscanner.R
import com.example.docscanner.ui.fragments.CaptureImageFragment
import com.example.docscanner.ui.fragments.EditImageFragment
import com.example.docscanner.ui.viewmodels.CameraViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_camera.*

@AndroidEntryPoint
class CameraActivity : AppCompatActivity(),
        CaptureImageFragment.CaptureImageInteractor,
        EditImageFragment.EditImageInteractor
{
    private val vm:CameraViewModel by  viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

    }

    override fun onThumbClicked() {
        cameraNavHostFragment.findNavController().navigate(R.id.action_to_editImageFragment)
    }

    override fun onNavigateToCapture() {
        cameraNavHostFragment.findNavController().navigate(R.id.action_to_captureImageFragment)
    }

    override fun onFinish() {
        finish()
    }

    override fun onBackPressed() {
        if (cameraNavHostFragment.findNavController().currentDestination?.id == R.id.captureImageFragment)
            finish()
        else
            super.onBackPressed()
    }
}