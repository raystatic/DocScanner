package com.example.docscanner.other

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.docscanner.R

class DividerItemDecoration: RecyclerView.ItemDecoration{

    private var mDivider: Drawable?

    constructor(context: Context, drawable:Int){
        mDivider = ContextCompat.getDrawable(context,drawable)
    }

    constructor(context: Context){
        mDivider = ContextCompat.getDrawable(context,R.drawable.line_divider)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child: View = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top: Int = child.bottom + params.bottomMargin
            val bottom = top + mDivider?.intrinsicHeight!!
            mDivider?.setBounds(left, top, right, bottom)
            mDivider?.draw(c)
        }
    }

}