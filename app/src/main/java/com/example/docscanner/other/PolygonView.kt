package com.example.docscanner.other

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.docscanner.R
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs

class PolygonView: FrameLayout{

    private var ctx:Context
    private lateinit var paint: Paint
    private lateinit var pointer1:ImageView
    private lateinit var pointer2:ImageView
    private lateinit var pointer3:ImageView
    private lateinit var pointer4:ImageView
    private lateinit var midPointer13:ImageView
    private lateinit var midPointer12:ImageView
    private lateinit var midPointer34:ImageView
    private lateinit var midPointer24:ImageView
    private lateinit var polygonView: PolygonView

    constructor(context:Context): super(context){
        ctx = context
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet){
        ctx = context
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyle:Int): super(context, attributeSet, defStyle){
        ctx = context
        init()
    }

    private fun init() {
        polygonView = this
        pointer1 = getImageView(0,0)
        pointer2 = getImageView(width,0)
        pointer3 = getImageView(0, height)
        pointer4 = getImageView(width, height)

        midPointer13 = getImageView(0,height / 2)
        midPointer13.setOnTouchListener(MidPointTouchListenerImpl(pointer1, pointer3))

        midPointer12 = getImageView(0,height / 2)
        midPointer12.setOnTouchListener(MidPointTouchListenerImpl(pointer1, pointer2))

        midPointer34 = getImageView(0,height / 2)
        midPointer34.setOnTouchListener(MidPointTouchListenerImpl(pointer3, pointer4))

        midPointer24 = getImageView(0,height / 2)
        midPointer24.setOnTouchListener(MidPointTouchListenerImpl(pointer2, pointer4))

        addView(pointer1)
        addView(pointer2)
        addView(midPointer13)
        addView(midPointer12)
        addView(midPointer34)
        addView(midPointer24)
        addView(pointer3)
        addView(pointer4)

        initPaint()
    }

    override fun attachViewToParent(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.attachViewToParent(child, index, params)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawLine(pointer1.x + pointer1.width / 2, pointer1.y + pointer1.height / 2, pointer3.x + pointer3.width / 2, pointer3.y + pointer3.height / 2, paint)
        canvas.drawLine(pointer1.x + pointer1.width / 2, pointer1.y + pointer1.height / 2, pointer2.x + pointer2.width / 2, pointer2.y + pointer2.height / 2, paint)
        canvas.drawLine(pointer2.x + pointer2.width / 2, pointer2.y + pointer2.height / 2, pointer4.x + pointer4.width / 2, pointer4.y + pointer4.height / 2, paint)
        canvas.drawLine(pointer3.x + pointer3.width / 2, pointer3.y + pointer3.height / 2, pointer4.x + pointer4.width / 2, pointer4.y + pointer4.height / 2, paint)
        midPointer13.x = pointer3.x - (pointer3.x - pointer1.x) / 2
        midPointer13.y = pointer3.y - (pointer3.y - pointer1.y) / 2
        midPointer24.x = pointer4.x - (pointer4.x - pointer2.x) / 2
        midPointer24.y = pointer4.y - (pointer4.y - pointer2.y) / 2
        midPointer34.x = pointer4.x - (pointer4.x - pointer3.x) / 2
        midPointer34.y = pointer4.y - (pointer4.y - pointer3.y) / 2
        midPointer12.x = pointer2.x - (pointer2.x - pointer1.x) / 2
        midPointer12.y = pointer2.y - (pointer2.y - pointer1.y) / 2
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    private fun initPaint() {
        paint = Paint()
        paint.color = resources.getColor(R.color.blue)
        paint.strokeWidth = 2f
        paint.isAntiAlias = true
    }

    private fun getImageView(x: Int, y: Int): ImageView {
        val imageView = ImageView(context)
        val layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        imageView.layoutParams = layoutParams
        imageView.setImageResource(R.drawable.circle)
        imageView.x = x.toFloat()
        imageView.y = y.toFloat()
        imageView.setOnTouchListener(TouchListenerImpl())
        return imageView
    }

    fun setPoints(pointFMap: Map<Int, PointF>?) {
        if (pointFMap?.size == 4) {
            setPointsCoordinates(pointFMap)
        }
    }

    private fun setPointsCoordinates(pointFMap: Map<Int, PointF>?) {
        pointer1.x = (pointFMap!![0] ?: error("")).x
        pointer1.y = (pointFMap[0] ?: error("")).y
        pointer2.x = (pointFMap[1] ?: error("")).x
        pointer2.y = (pointFMap[1] ?: error("")).y
        pointer3.x = (pointFMap[2] ?: error("")).x
        pointer3.y = (pointFMap[2] ?: error("")).y
        pointer4.x = (pointFMap[3] ?: error("")).x
        pointer4.y = (pointFMap[3] ?: error("")).y
    }

    fun isValidShape(pointFMap: Map<Int, PointF>?): Boolean {
        return pointFMap?.size == 4
    }

    private fun getPoints(): Map<Int, PointF>? {
        val points: MutableList<PointF> = ArrayList()
        points.add(PointF(pointer1.x, pointer1.y))
        points.add(PointF(pointer2.x, pointer2.y))
        points.add(PointF(pointer3.x, pointer3.y))
        points.add(PointF(pointer4.x, pointer4.y))
        return getOrderedPoints(points)
    }

    fun getOrderedPoints(points:List<PointF>):Map<Int,PointF>?{
        val centerPoint = PointF()
        val size = points.size
        points.forEach {
            centerPoint.x = it.x / size
            centerPoint.y = it.y / size
        }

        val orderedPoints = HashMap<Int, PointF>()

        points.forEach {
            var index = -1;
            if (it.x < centerPoint.x && it.y < centerPoint.y){
                index = 0
            }else if (it.x > centerPoint.x && it.y < centerPoint.y){
                index = 1
            }else if (it.x < centerPoint.x && it.y > centerPoint.y){
                index = 2
            }else if(it.x > centerPoint.x && it.y > centerPoint.y){
                index = 3
            }
            orderedPoints[index] = it
        }

        return orderedPoints
    }

    inner class MidPointTouchListenerImpl(private val mainPointer1: ImageView, private val mainPointer2: ImageView) : OnTouchListener {
        private var downPT = PointF() // Record Mouse Position When Pressed Down
        private var startPT = PointF() // Record Start Position of 'img'
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val mv = PointF(event.x - downPT.x, event.y - downPT.y)
                    if (abs(mainPointer1.x - mainPointer2.x) > abs(mainPointer1.y - mainPointer2.y)) {
                        if (mainPointer2.y + mv.y + v.height < polygonView.height && mainPointer2.y + mv.y > 0) {
                            v.x = (startPT.y + mv.y)
                            startPT = PointF(v.x, v.y)
                            mainPointer2.y = (mainPointer2.y + mv.y)
                        }
                        if (mainPointer1.y + mv.y + v.height < polygonView.height && mainPointer1.y + mv.y > 0) {
                            v.x = (startPT.y + mv.y)
                            startPT = PointF(v.x, v.y)
                            mainPointer1.y = (mainPointer1.y + mv.y)
                        }
                    } else {
                        if (mainPointer2.x + mv.x + v.width < polygonView.width && mainPointer2.x + mv.x > 0) {
                            v.x = (startPT.x + mv.x)
                            startPT = PointF(v.x, v.y)
                            mainPointer2.x = (mainPointer2.x + mv.x)
                        }
                        if (mainPointer1.x + mv.x + v.width < polygonView.width && mainPointer1.x + mv.x > 0) {
                            v.x = (startPT.x + mv.x)
                            startPT = PointF(v.x, v.y)
                            mainPointer1.x = (mainPointer1.x + mv.x)
                        }
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    downPT.x = event.x
                    downPT.y = event.y
                    startPT = PointF(v.x, v.y)
                }
                MotionEvent.ACTION_UP -> {
                    var color = 0
                    color = if (isValidShape(getPoints())) {
                        resources.getColor(R.color.blue)
                    } else {
                        resources.getColor(R.color.orange)
                    }
                    paint.color = color
                }
                else -> {
                }
            }
            polygonView.invalidate()
            return true
        }
    }

    inner class TouchListenerImpl : OnTouchListener {
        private var downPT = PointF() // Record Mouse Position When Pressed Down
        private var startPT = PointF() // Record Start Position of 'img'
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val mv = PointF(event.x - downPT.x, event.y - downPT.y)
                    if (startPT.x + mv.x + v.width < polygonView.width && startPT.y + mv.y + v.height < polygonView.height && startPT.x + mv.x > 0 && startPT.y + mv.y > 0) {
                        v.x = (startPT.x + mv.x)
                        v.y = (startPT.y + mv.y)
                        startPT = PointF(v.x, v.y)
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    downPT.x = event.x
                    downPT.y = event.y
                    startPT = PointF(v.x, v.y)
                }
                MotionEvent.ACTION_UP -> {
                    var color = 0
                    color = if (isValidShape(getPoints())) {
                        resources.getColor(R.color.blue)
                    } else {
                        resources.getColor(R.color.orange)
                    }
                    paint.color = color
                }
                else -> {
                }
            }
            polygonView.invalidate()
            return true
        }
    }

}
