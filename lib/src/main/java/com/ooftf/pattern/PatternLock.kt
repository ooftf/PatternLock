package com.ooftf.pattern

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*

/**
 * Created by master on 2016/4/12.
 */
class PatternLock : View {
    var onSlideListener: OnSlideListener? = null
    internal var paintPoint: Paint
    internal var paintLine: Paint
    internal var points: MutableList<Point> = ArrayList()
    lateinit var normalDrawable: Drawable
    lateinit var selectedDrawable: Drawable
    lateinit var errorDrawable: Drawable
    internal var currentX: Float = 0.toFloat()
    internal var currentY: Float = 0.toFloat()
    internal var touching = false
    internal var iconSize = 0f
    private var allowRepeat = false
    /**
     * 错误状态下线的颜色
     */
    private var errorColor = Color.parseColor("#FF0000")
    /**
     * 正常状态下线的颜色
     */
    private var nomalColor = Color.parseColor("#00FFFF")
    /**
     * 已经选择的点
     */
    internal var selectedPoints: MutableList<Point>

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        obtainAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        obtainAttrs(attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        obtainAttrs(attrs)
    }

    init {
        initPoint()
        selectedPoints = ArrayList()
        paintPoint = Paint()
        paintPoint.isAntiAlias = true
        paintLine = Paint()
        paintLine.isAntiAlias = true
    }

    fun obtainAttrs(attrs: AttributeSet) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.PatternLock)
        normalDrawable = context.resources.getDrawable(attributes.getResourceId(R.styleable.PatternLock_nomalIconId, R.drawable.vector_pattern_lock_normal))
        selectedDrawable = context.resources.getDrawable(attributes.getResourceId(R.styleable.PatternLock_selectedIconId, R.drawable.vector_pattern_lock_selected))
        errorDrawable = context.resources.getDrawable(attributes.getResourceId(R.styleable.PatternLock_errorIconId, R.drawable.vector_pattern_lock_error))
        iconSize = attributes.getDimension(R.styleable.PatternLock_iconSize, dip2px(context, 56f).toFloat())
        paintLine.strokeWidth = attributes.getDimension(R.styleable.PatternLock_lineWidth, dip2px(context, 8f).toFloat())
        nomalColor = attributes.getColor(R.styleable.PatternLock_nomalLineColor, Color.parseColor("#00FFFF"));
        errorColor = attributes.getColor(R.styleable.PatternLock_errorLineColor, Color.parseColor("#FF0000"));
        allowRepeat = attributes.getBoolean(R.styleable.PatternLock_allowRepeat,false)
    }

    private fun initPoint() {
        for (i in 0..8) {
            points.add(Point(i))
        }
    }

    private fun heightRemovePadding(height: Int) = height - paddingTop - paddingBottom
    private fun widthRemovePadding(width: Int) = width - paddingLeft - paddingRight
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                reset()
                touching = true
                onSlideListener?.onStart()
            }
            MotionEvent.ACTION_UP -> {
                finishTouch()
            }
            MotionEvent.ACTION_CANCEL -> {
                finishTouch()
            }
        }
        currentX = event.x
        currentY = event.y
        intersectPoint(currentX, currentY)
        invalidate()
        return true
    }

    fun reset() {
        if (touching) return
        paintLine.color = nomalColor
        selectedPoints.forEach { it.state = PointState.NORMAL }
        selectedPoints.clear()
        invalidate()
    }

    fun error() {
        selectedPoints.forEach {
            it.state = PointState.ERROR
        }
        paintLine.color = errorColor
        invalidate()
    }

    private fun finishTouch() {
        touching = false
        onSlideListener?.onCompleted(getResult())
    }

    fun getResult(): ArrayList<Int> {
        val list = ArrayList<Int>()
        for (p in selectedPoints) {
            list.add(p.position)
        }
        return list
    }

    /**
     * 判断某个坐标是否触发到Point
     * @param currentX
     * @param currentY
     * @return
     */

    internal fun intersectPoint(currentX: Float, currentY: Float) {
        points.filter { it.isIntersect(currentX, currentY) }
                .filter { allowRepeat||!selectedPoints.contains(it)}
                .forEach {
                    it.state = PointState.SELECT
                    selectedPoints.add(it)
                }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (p in points) {
            p.onDraw(canvas)
        }
        drawLine(canvas)
    }

    private fun drawLine(canvas: Canvas) {
        for (i in selectedPoints.indices)
            if (i == selectedPoints.size - 1) {
                if (touching) {
                    canvas.drawLine(selectedPoints[i].getX(), selectedPoints[i].getY(), currentX, currentY, paintLine)
                }
            } else {
                canvas.drawLine(selectedPoints[i].getX(), selectedPoints[i].getY(), selectedPoints[i + 1].getX(), selectedPoints[i + 1].getY(), paintLine)
            }
    }

    /**
     * position 从0开始
     */
    internal inner class Point(var position: Int) {
        var state = PointState.NORMAL
        /**
         * 获取点位于第几行
         */
        fun getRow() = position / 3

        /**
         * 从0开始
         *
         * @return
         */
        fun getColumn() = position % 3

        fun getX() = paddingLeft + (getColumn() * 2 + 1f) * getRadius() + getColumnSpace() * getColumn()
        fun getY() = paddingTop + (getRow() * 2 + 1f) * getRadius() + getRowSpace() * getRow()
        fun getRadius() = iconSize / 2

        fun getColumnSpace() = (widthRemovePadding(width) - getRadius() * 6) / 2f
        fun getRowSpace() = (heightRemovePadding(height) - getRadius() * 6) / 2f
        fun getRect() = Rect((getX() - getRadius()).toInt(), (getY() - getRadius()).toInt(), (getX() + getRadius()).toInt(), (getY() + getRadius()).toInt())
        fun isIntersect(x: Float, y: Float) = MathUtil.distance(getX().toDouble(), getY().toDouble(), x.toDouble(), y.toDouble()) <= getRadius()
        fun onDraw(canvas: Canvas) {
            when (state) {
                PointState.NORMAL -> {
                    normalDrawable.bounds = getRect()
                    normalDrawable.draw(canvas)
                }
                PointState.SELECT -> {
                    selectedDrawable.bounds = getRect()
                    selectedDrawable.draw(canvas)
                }
                PointState.ERROR -> {
                    errorDrawable.bounds = getRect()
                    errorDrawable.draw(canvas)
                }
            }
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    enum class PointState {
        NORMAL, SELECT, ERROR
    }
}
