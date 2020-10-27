package com.example.docscanner.other

import android.Manifest
import android.content.Context
import pub.devrel.easypermissions.EasyPermissions

object Utility {

    fun hasCameraPermission(context: Context) =
            EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )


}