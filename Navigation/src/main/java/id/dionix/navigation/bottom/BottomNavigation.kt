package id.dionix.navigation.bottom

import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import id.dionix.navigation.R

class BottomNavigation(
    context: Context,
    attrs: AttributeSet?,
) : LinearLayout(context, attrs) {

    private var mOnItemSelected: (position: Int) -> Unit = {}

    var currentItem: Int = 0
        set(value) {
            if (childCount == 0) {
                return
            }
            (getChildAt(field) as BottomNavigationItem).isSelected = false
            (getChildAt(value) as BottomNavigationItem).isSelected = true
            field = value
            mOnItemSelected(value)
        }

    val itemCount: Int get() = childCount

    @ColorInt
    var color: Int = 0
        set(value) {
            field = value
            for (child in children) {
                (child as BottomNavigationItem).color = value
            }
        }

    @ColorInt
    var colorSelected: Int? = null
        set(value) {
            field = value
            for (child in children) {
                (child as BottomNavigationItem).colorSelected = value
            }
        }

    var iconMarginTop: Int = 0
        set(value) {
            field = value
            for (child in children) {
                (child as BottomNavigationItem).iconMarginTop = value
            }
        }

    var iconMarginBottom: Int = 0
        set(value) {
            field = value
            for (child in children) {
                (child as BottomNavigationItem).iconMarginBottom = value
            }
        }

    var labelMarginTop: Int = 0
        set(value) {
            field = value
            for (child in children) {
                (child as BottomNavigationItem).labelMarginTop = value
            }
        }

    var labelMarginBottom: Int = 0
        set(value) {
            field = value
            for (child in children) {
                (child as BottomNavigationItem).labelMarginBottom = value
            }
        }

    var typeface: Typeface = Typeface.DEFAULT
        set(value) {
            field = value
            for (child in children) {
                (child as BottomNavigationItem).labelTypeface = value
            }
        }

    fun setOnItemSelectedListener(listener: (position: Int) -> Unit) {
        mOnItemSelected = listener
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (child !is BottomNavigationItem) {
            return
        }
        super.addView(child, index, params)
        child.color = color
        child.colorSelected = colorSelected
        child.iconMarginTop = iconMarginTop
        child.iconMarginBottom = iconMarginBottom
        child.labelMarginTop = labelMarginTop
        child.labelMarginBottom = labelMarginBottom
        child.labelTypeface = typeface

        val position = childCount - 1
        child.scaleOnClick {
            currentItem = position
        }
        currentItem = currentItem
    }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        context.obtainStyledAttributes(attrs, R.styleable.BottomNavigation).apply {
            try {
                currentItem = getInt(R.styleable.BottomNavigation_currentItem, 0)
                color = getColor(R.styleable.BottomNavigation_color, 0)
                colorSelected = getColor(R.styleable.BottomNavigation_colorSelected, 0)
                iconMarginTop = getDimension(R.styleable.BottomNavigation_iconMarginTop, 0f).toInt()
                iconMarginBottom = getDimension(R.styleable.BottomNavigation_iconMarginBottom, 0f).toInt()
                labelMarginTop = getDimension(R.styleable.BottomNavigation_labelMarginTop, 0f).toInt()
                labelMarginBottom = getDimension(R.styleable.BottomNavigation_labelMarginBottom, 0f).toInt()

                typeface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getFont(R.styleable.BottomNavigation_fontFamily) ?: Typeface.DEFAULT
                } else {
                    getResourceId(R.styleable.BottomNavigation_fontFamily, 0).let {
                        if (it == 0) Typeface.DEFAULT
                        else try {
                            ResourcesCompat.getFont(context, it) ?: Typeface.DEFAULT
                        } catch (_: Exception) {
                            Typeface.DEFAULT
                        }
                    }
                }
            } finally {
                recycle()
            }
        }
    }

    private fun View.scaleOnClick(
        scaleOnDown: Float = 0.95f,
        scaleOnUp: Float = 1f,
        durationOnDown: Long = 100,
        durationOnUp: Long = 50,
        rippleEnabled: Boolean = true,
        onPreClick: (view: View) -> Boolean = { false },
        onClick: (view: View) -> Unit = {}
    ) {
        val rect = Rect()
        var isOutside = false

        setOnTouchListener { v, event ->
            if (onPreClick(this)) {
                return@setOnTouchListener false
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().setDuration(durationOnDown).scaleX(scaleOnDown)
                    v.animate().setDuration(durationOnDown).scaleY(scaleOnDown)
                    isOutside = false
                    rect.set(v.left, v.top, v.right, v.bottom)
                    if (rippleEnabled) {
                        v.isPressed = true
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isOutside && !rect.contains(
                            (v.left + event.x).toInt(),
                            (v.top + event.y).toInt()
                        )
                    ) {
                        isOutside = true
                        v.animate().setDuration(durationOnUp).scaleX(scaleOnUp)
                        v.animate().setDuration(durationOnUp).scaleY(scaleOnUp)
                        if (rippleEnabled) {
                            v.isPressed = false
                        }
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    v.animate().setDuration(durationOnUp).scaleX(scaleOnUp)
                    v.animate().setDuration(durationOnUp).scaleY(scaleOnUp)
                    if (rippleEnabled) {
                        v.isPressed = false
                    }
                    return@setOnTouchListener false
                }
                MotionEvent.ACTION_UP -> {
                    if (!isOutside) {
                        v.performClick()
                        v.animate().setDuration(durationOnUp).scaleX(scaleOnUp)
                        v.animate().setDuration(durationOnUp).scaleY(scaleOnUp)
                    }
                    if (rippleEnabled) {
                        v.isPressed = false
                    }
                }
            }
            return@setOnTouchListener true
        }

        setOnClickListener {
            if (!onPreClick(it)) {
                onClick(it)
            }
        }
    }

}