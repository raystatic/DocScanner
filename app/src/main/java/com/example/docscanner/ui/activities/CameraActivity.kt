package com.example.docscanner.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.docscanner.R
import com.example.docscanner.ui.fragments.CaptureImageFragment
import com.example.docscanner.ui.fragments.EditImageFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_camera.*
import timber.log.Timber

@AndroidEntryPoint
class CameraActivity : AppCompatActivity(),
        CaptureImageFragment.CaptureImageInteractor,
        EditImageFragment.EditImageInteractor
{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

    }
    override fun onThumbClicked() {
        Timber.d("thumnail clicked")
        cameraNavHostFragment.findNavController().navigate(R.id.action_to_editImageFragment)
    }

    override fun onNavigateToCapture() {
        Timber.d("backimage clicked")
        cameraNavHostFragment.findNavController().navigate(R.id.action_to_captureImageFragment)
    }
}