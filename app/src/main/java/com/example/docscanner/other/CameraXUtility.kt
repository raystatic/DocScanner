package com.example.docscanner.other

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import com.example.docscanner.R
import java.io.File

object CameraXUtility {

    const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    const val PHOTO_EXTENSION = ".jpg"

    fun getBitmapFromUri(uri: Uri, context: Context): Bitmap {
        return MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }


     fun getOutputDirectory(context: Context): File {
        val appContext = context.applicationContext
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

     fun rotatateImageIfRequired(bmp: Bitmap, savedUri: Uri?): Bitmap? {
        val exiInterface = ExifInterface(savedUri?.path.toString())
        val orientation = exiInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when(orientation){
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bmp,90)
            ExifInterface.ORIENTATION_ROTATE_180-> rotateImage(bmp,180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bmp,270)
            else -> bmp
        }
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

}