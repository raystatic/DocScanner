package com.easyscan.docscanner.other

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import com.easyscan.docscanner.R
import com.itextpdf.text.Rectangle
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.abs


object CameraXUtility {

    const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    const val PHOTO_EXTENSION = ".jpg"

    fun getBitmapFromUri(uri: Uri, context: Context): Bitmap {
        return MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }

    fun sharePdf(uri: Uri, context: Context){
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.type = "application/pdf"
        context.startActivity(intent)
    }

    fun insertBitmapAtUri(uri: Uri, context: Context, bitmap: Bitmap) {
        MediaStore.Images.Media.insertImage(context.contentResolver, bitmap,"title",null)
    }

    fun isContainsSpecialCharacter(str:String): Boolean {
        val p: Pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE)
        val m: Matcher = p.matcher("I am a string")
        return m.find()
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

    fun toGrayscale(bmpOriginal: Bitmap): Bitmap? {
        val height: Int = bmpOriginal.height
        val width: Int = bmpOriginal.width
        val bmpGrayscale = Bitmap.createBitmap(width, height, bmpOriginal.config)
        val c = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayscale
    }

    fun calculateFitSize(
        originalWidth: Float,
        originalHeight: Float,
        documentSize: Rectangle
    ): Rectangle? {
        val widthChange: Float = (originalWidth - documentSize.width) / originalWidth
        val heightChange: Float =
            (originalHeight - documentSize.height) / originalHeight
        val changeFactor = widthChange.coerceAtLeast(heightChange)
        val newWidth = originalWidth - originalWidth * changeFactor
        val newHeight = originalHeight - originalHeight * changeFactor
        return Rectangle(
            abs(newWidth),
            abs(newHeight)
        )
    }

}