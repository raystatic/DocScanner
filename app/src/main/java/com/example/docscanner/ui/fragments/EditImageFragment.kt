package com.example.docscanner.ui.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.example.docscanner.R
import com.example.docscanner.data.models.Document
import com.example.docscanner.other.ViewExtension.show
import com.example.docscanner.ui.adapters.EditImageAdapter
import com.example.docscanner.ui.viewmodels.CameraViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.edit_images_fragment.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class EditImageFragment : Fragment(R.layout.edit_images_fragment),EditImageAdapter.EditImageListener {

    private lateinit var editImageAdapter: EditImageAdapter
    private val vm: CameraViewModel by activityViewModels()

    private lateinit var callback: EditImageInteractor

    @Inject
    lateinit var glide:RequestManager


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
//                        glide.apply {
//                            load(it[EditImageAdapter.selectedPosition].bitmap)
//                                    .into(imgEditDoc)
//                        }
                        sourceFrame.post {
                            it[EditImageAdapter.selectedPosition].bitmap?.let { bitmap ->
                                loadBitmap(bitmap)
                            }
                        }                    }else{
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
//        glide.apply {
//            load(document.bitmap)
//                    .into(imgEditDoc)
//        }
        sourceFrame.post {
            document.bitmap?.let {
                loadBitmap(it)
            }
        }
    }

    interface EditImageInteractor{
        fun onNavigateToCapture()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as EditImageInteractor

        vm.currentFragmentVisible.value = 1
    }

}