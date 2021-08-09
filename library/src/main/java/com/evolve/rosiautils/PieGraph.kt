package com.evolve.rosiautils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*

class PieGraph : View {

    var slices = ArrayList<PieSlice>()
        set(slices) {
            field = slices
            postInvalidate()
        }
    private val paint = Paint()
    private val path = Path()
    private val p = Path()
    private val rectF = RectF()
    private var indexSelected = -1
    var thickness = 50
        set(thickness) {
            field = thickness
            postInvalidate()
        }
    private var listener: OnSliceClickedListener? = null
    private val region = Region()

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    fun update() {
        postInvalidate()
    }

    public override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.TRANSPARENT)
        paint.reset()
        paint.isAntiAlias = true
        val midX: Float
        val midY: Float
        var radius: Float
        val innerRadius: Float
        path.reset()

        var currentAngle = 270f
        var currentSweep: Float
        var totalValue = 0f
        val padding = 1f

        midX = (width / 2).toFloat()
        midY = (height / 2).toFloat()
        radius = if (midX < midY) {
            midX
        } else {
            midY
        }
        radius -= padding
        innerRadius = radius - this.thickness

        for (slice in this.slices) {
            totalValue += slice.value
        }

        for ((count, slice) in this.slices.withIndex()) {
            p.reset()
            paint.color = slice.color
            currentSweep = slice.value / totalValue * 360
            rectF.setEmpty()
            rectF.set(midX - radius, midY - radius, midX + radius, midY + radius)
            p.arcTo(rectF, currentAngle + padding, currentSweep - padding)

            rectF.setEmpty()
            rectF.set(
                midX - innerRadius,
                midY - innerRadius,
                midX + innerRadius,
                midY + innerRadius
            )
            p.arcTo(
                rectF,
                currentAngle + padding + (currentSweep - padding),
                -(currentSweep - padding)
            )
            p.close()

            slice.path = p
            region.setEmpty()
            region.set(
                (midX - radius).toInt(),
                (midY - radius).toInt(),
                (midX + radius).toInt(),
                (midY + radius).toInt()
            )
            slice.region = region
            canvas.drawPath(p, paint)

            if (indexSelected == count && listener != null) {
                path.reset()
                paint.color = slice.color
                paint.color = Color.parseColor("#33B5E5")
                paint.alpha = 100

                if (this.slices.size > 1) {
                    rectF.setEmpty()
                    rectF.set(
                        midX - radius - padding * 2,
                        midY - radius - padding * 2,
                        midX + radius + padding * 2,
                        midY + radius + padding * 2
                    )
                    path.arcTo(rectF, currentAngle, currentSweep + padding)
                    rectF.setEmpty()
                    rectF.set(
                        midX - innerRadius + padding * 2,
                        midY - innerRadius + padding * 2,
                        midX + innerRadius - padding * 2,
                        midY + innerRadius - padding * 2
                    )
                    path.arcTo(
                        rectF,
                        currentAngle + currentSweep + padding,
                        -(currentSweep + padding)
                    )
                    path.close()
                } else {
                    path.addCircle(midX, midY, radius + padding, Path.Direction.CW)
                }

                canvas.drawPath(path, paint)
                paint.alpha = 255
            }

            val icon = slice.icon
            if (icon != null) {
                val rad = 2.0 * Math.PI * (currentAngle + currentSweep / 2).toDouble() / 360.0
                val left = Math.cos(rad) * (innerRadius + radius) / 2
                val top = Math.sin(rad) * (innerRadius + radius) / 2
                canvas.drawBitmap(
                    icon,
                    midX - icon.getWidth() / 2 + left.toFloat(),
                    midY - icon.getHeight() / 2 + top.toFloat(),
                    null
                )
            }

            currentAngle += currentSweep
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        try {
            val point = Point()
            point.x = event.x.toInt()
            point.y = event.y.toInt()

            for ((count, slice) in this.slices.withIndex()) {
                val r = Region()
                if (slice.path != null && slice.region != null) {
                    r.setPath(slice.path!!, slice.region!!)
                }

                if (r.contains(point.x, point.y) && event.action == MotionEvent.ACTION_DOWN) {
                    indexSelected = count
                } else if (event.action == MotionEvent.ACTION_UP) {
                    if (r.contains(point.x, point.y) && listener != null) {
                        if (indexSelected > -1) {
                            listener!!.onClick(indexSelected)
                        }
                        indexSelected = -1
                    }
                }
            }

            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                postInvalidate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }


        return true
    }

    fun getSlice(index: Int): PieSlice {
        return this.slices[index]
    }

    fun addSlice(slice: PieSlice) {
        this.slices.add(slice)
        postInvalidate()
    }

    fun setOnSliceClickedListener(listener: OnSliceClickedListener) {
        this.listener = listener
    }

    fun removeSlices() {
        for (i in this.slices.indices.reversed()) {
            this.slices.removeAt(i)
        }
        postInvalidate()
    }

    interface OnSliceClickedListener {
        fun onClick(index: Int)
    }
}
