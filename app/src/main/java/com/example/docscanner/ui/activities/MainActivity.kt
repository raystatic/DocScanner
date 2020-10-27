package com.example.docscanner.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.docscanner.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView.background = null

        bottomNavigationView.menu.getItem(1).isEnabled = false

        fab.setOnClickListener {
            navigateToCameraFragment()
        }

    }

    private fun navigateToCameraFragment() {
        startActivity(Intent(this, CameraActivity::class.java))
    }
}