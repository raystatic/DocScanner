package com.example.docscanner.ui.fragments

import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.example.docscanner.R
import com.example.docscanner.data.models.Document
import com.example.docscanner.other.CameraXUtility
import com.example.docscanner.other.ViewExtension.hide
import com.example.docscanner.other.ViewExtension.show
import com.example.docscanner.ui.adapters.EditImageAdapter
import com.example.docscanner.ui.viewmodels.CameraViewModel
import com.itextpdf.text.Element
import com.itextpdf.text.Image
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfWriter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.create_pdf_confirmation.view.*
import kotlinx.android.synthetic.main.edit_images_fragment.*
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class EditImageFragment : Fragment(R.layout.edit_images_fragment),EditImageAdapter.EditImageListener {

    private lateinit var editImageAdapter: EditImageAdapter
    private val vm: CameraViewModel by activityViewModels()

    private lateinit var callback: EditImageInteractor

    @Inject
    lateinit var glide:RequestManager

    private lateinit var confirmDialogView:View
    private lateinit var confirmDialog:Dialog


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpEditImageAdapter()

        subscribeToObservers()

        imgBack.setOnClickListener {
            callback.onNavigateToCapture()
        }

        imgDelete.setOnClickListener {
            removeItemFromDocList()
        }

        imgDone.setOnClickListener {
            showConfirmationDialog()
        }


        imgRetake.setOnClickListener {
            retakePicture = EditImageAdapter.selectedPosition
            removeItemFromDocList()
            callback.onNavigateToCapture()
        }

    }

    private fun showConfirmationDialog() {
        confirmDialog = Dialog(requireContext())
        confirmDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.create_pdf_confirmation, null)

        confirmDialog.setContentView(confirmDialogView)

        confirmDialog.apply {
            setContentView(confirmDialogView)
            setCancelable(false)
        }
        confirmDialogView.apply {

            btnCancelConfirm.setOnClickListener {
                confirmDialog.cancel()
            }

            btnDoneConfirm.setOnClickListener {
                createPdf()
            }

        }

        confirmDialog.show()

        val window = confirmDialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

    }

    private fun createPdf() {
        vm.docList.observe(viewLifecycleOwner, Observer {
            it?.let {docs->
                if (docs.isNotEmpty()){

                    val filePath = "${CameraXUtility.getOutputDirectory(requireContext())}/${SimpleDateFormat(
                        CameraXUtility.FILENAME, Locale.US
                    ).format(System.currentTimeMillis())}.pdf"

                    vm.createPdf(docs,filePath)

                    vm.isPdfCreating.observe(viewLifecycleOwner, Observer { isPdfCreating ->
                        if (isPdfCreating){
                            confirmDialogView.linCreatingPdf.show()
                            confirmDialogView.btnCancelConfirm.isEnabled = false
                            confirmDialogView.btnDoneConfirm.isEnabled = false
                            confirmDialogView.btnCancelConfirm.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.custom_shadow_light_blue))
                            confirmDialogView.btnDoneConfirm.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.custom_shadow_light_blue))
                        }else{
                            confirmDialogView.btnCancelConfirm.isEnabled = true
                            confirmDialogView.btnDoneConfirm.isEnabled = true
                            confirmDialogView.btnCancelConfirm.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.custom_lightest_blue))
                            confirmDialogView.btnDoneConfirm.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.custom_shadow_dark_blue))
                            confirmDialogView.linCreatingPdf.hide()
                            confirmDialog.cancel()
                            callback.onFinish()
                        }
                    })
                }
            }
        })
    }

    private fun loadBitmapWithGlide(bitmap: Bitmap){
        glide.apply {
            load(bitmap)
                    .into(imgEditDoc)
        }
    }

    private fun getCroppedImage(bitmap: Bitmap){

        val resultBitmap = Bitmap.createBitmap(bitmap.width,bitmap.height, bitmap.config)

        val canvas = Canvas(resultBitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        val path =  Path()
        val points = polygonView.getPoints()
        path.lineTo(points?.get(0)?.x!!, points[0]?.y!!)
        path.lineTo(points[1]?.x!!, points[0]?.y!!)
        path.lineTo(points[2]?.x!!, points[0]?.y!!)
        path.lineTo(points[3]?.x!!, points[0]?.y!!)

        canvas.drawPath(path, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap,0f,0f,paint)

        glide.load(resultBitmap).into(imgEditDoc)

    }

    private fun loadBitmap(original:Bitmap){
        val scaledBitmap = scaledBitmap(original, sourceFrame.width, sourceFrame.height)
        imgEditDoc.setImageBitmap(scaledBitmap)
        val tempBitmap = (imgEditDoc.drawable as BitmapDrawable).bitmap
        val pointFs = getEdgePoints(tempBitmap)
        polygonView.setPoints(pointFs)
        polygonView.show()
        val padding = resources.getDimension(R.dimen.scanPadding).toInt()
        val layoutParams = FrameLayout.LayoutParams(tempBitmap.width + 2 * padding, tempBitmap.height + 2 * padding)
        layoutParams.gravity = Gravity.CENTER
        polygonView.layoutParams = layoutParams
    }

    private fun scaledBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap? {
        val m = Matrix()
        m.setRectToRect(RectF(0F, 0F, bitmap.width.toFloat(), bitmap.height.toFloat()), RectF(0F, 0F, width.toFloat(), height.toFloat()), Matrix.ScaleToFit.CENTER)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    private fun getEdgePoints(tempBitmap: Bitmap): Map<Int, PointF>? {
        val pointFs: List<PointF> = getContourEdgePoints(tempBitmap)
        return orderedValidEdgePoints(tempBitmap, pointFs)
    }

    private fun orderedValidEdgePoints(tempBitmap: Bitmap, pointFs: List<PointF>): Map<Int, PointF>? {
        var orderedPoints: Map<Int, PointF>? = polygonView.getOrderedPoints(pointFs)
        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap)
        }
        return orderedPoints
    }

    private fun getOutlinePoints(tempBitmap: Bitmap): Map<Int, PointF>? {
        val outlinePoints: MutableMap<Int, PointF> = HashMap()
        outlinePoints[0] = PointF(0f, 0f)
        outlinePoints[1] = PointF(tempBitmap.width.toFloat(), 0f)
        outlinePoints[2] = PointF(0f, tempBitmap.height.toFloat())
        outlinePoints[3] = PointF(tempBitmap.width.toFloat(), tempBitmap.height.toFloat())
        return outlinePoints
    }

    private fun getContourEdgePoints(tempBitmap: Bitmap): List<PointF> {
        val params = sourceFrame.layoutParams as FrameLayout.LayoutParams

        val x1 = params.leftMargin
        val x2 = params.leftMargin + tempBitmap.width
        val x3 = params.leftMargin
        val x4 = params.leftMargin + tempBitmap.width
        val y1 = params.topMargin
        val y2 = params.topMargin
        val y3 = params.topMargin + tempBitmap.height
        val y4 = params.topMargin + tempBitmap.height
        val pointFs: MutableList<PointF> = ArrayList()
        pointFs.add(PointF(x1.toFloat(), y1.toFloat()))
        pointFs.add(PointF(x2.toFloat(), y2.toFloat()))
        pointFs.add(PointF(x3.toFloat(), y3.toFloat()))
        pointFs.add(PointF(x4.toFloat(), y4.toFloat()))
        return pointFs
    }

    private fun removeItemFromDocList() {
        vm.removeItemAtPosition(EditImageAdapter.selectedPosition)
    }

    private fun subscribeToObservers() {
        vm.docList.observe(requireActivity(), Observer {
            it?.let {
                if (imgEditDoc!=null){
                    if (it.isNotEmpty()){
                        editImageAdapter.setData(it)
//                        sourceFrame.post {
//                            it[EditImageAdapter.selectedPosition].bitmap?.let { bitmap ->
//                                loadBitmap(bitmap)
//                                currentBitmap = bitmap
//                            }
//                        }
                        it[EditImageAdapter.selectedPosition].bitmap?.let { it1 ->
                            loadBitmapWithGlide(it1)
                            currentBitmap = it1
                        }
                    }else{
                        Toast.makeText(requireContext(), "Pages finished", Toast.LENGTH_SHORT).show()
                        callback.onNavigateToCapture()
                    }
                }
            }
        })
    }

    private fun setUpEditImageAdapter() {
        editImageAdapter = EditImageAdapter(requireContext(), this, glide)
        rvImages.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = editImageAdapter
        }
    }

    override fun onImageClicked(document: Document) {
//        sourceFrame.post {
//            document.bitmap?.let {
//                loadBitmap(it)
//                currentBitmap = it
//            }
//        }
        document.bitmap?.let {
            loadBitmapWithGlide(it)
        }
    }

    interface EditImageInteractor{
        fun onNavigateToCapture()
        fun onFinish()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as EditImageInteractor

        vm.currentFragmentVisible.value = 1
    }

    companion object{

        private var currentBitmap:Bitmap ?= null
        var retakePicture:Int = -1
    }

}