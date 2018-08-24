package com.teambrella.android.ui.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View

class CircleView: View{
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)


     private val paint = Paint().apply { color = Color.WHITE
            isAntiAlias = true
     }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val y = 50f
        val radius = ((width.toFloat()/2)*(width.toFloat()/2) + y*y)/(2*y)
        canvas?.drawCircle(width.toFloat()/2, radius, radius, paint)
    }
}