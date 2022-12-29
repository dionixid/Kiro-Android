package id.dionix.navigation.bottom

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import id.dionix.navigation.R

class BottomNavigationItem(
    context: Context,
    attrs: AttributeSet?,
) : LinearLayout(context, attrs) {

    private val ivIcon = AppCompatImageView(context)
    private val tvLabel = TextView(context)

    var icon: Drawable? = null
        set(value) {
            field = value
            if (!isSelected) {
                ivIcon.setImageDrawable(value)
            }
        }

    var iconSelected: Drawable? = null
        set(value) {
            field = value
            if (isSelected) {
                ivIcon.setImageDrawable(value)
            }
        }

    var label: CharSequence = ""
        set(value) {
            field = value
            if (!isSelected) {
                tvLabel.text = value
            }
        }

    @ColorInt
    var color: Int = 0
        set(value) {
            field = value
            if (!isSelected) {
                ivIcon.imageTintList = ColorStateList.valueOf(value)
                tvLabel.setTextColor(value)
            }
        }

    @ColorInt
    var colorSelected: Int? = null
        set(value) {
            field = value
            if (isSelected && value != null) {
                ivIcon.imageTintList = ColorStateList.valueOf(value)
                tvLabel.setTextColor(value)
            }
        }

    var iconMarginTop: Int
        set(value) {
            ivIcon.layoutParams = (ivIcon.layoutParams as MarginLayoutParams).apply {
                topMargin = value
            }
        }
        get() = (ivIcon.layoutParams as MarginLayoutParams).topMargin

    var iconMarginBottom: Int
        set(value) {
            ivIcon.layoutParams = (ivIcon.layoutParams as MarginLayoutParams).apply {
                bottomMargin = value
            }
        }
        get() = (ivIcon.layoutParams as MarginLayoutParams).topMargin

    var labelMarginTop: Int
        set(value) {
            tvLabel.layoutParams = (tvLabel.layoutParams as MarginLayoutParams).apply {
                topMargin = value
            }
        }
        get() = (tvLabel.layoutParams as MarginLayoutParams).topMargin

    var labelMarginBottom: Int
        set(value) {
            tvLabel.layoutParams = (tvLabel.layoutParams as MarginLayoutParams).apply {
                bottomMargin = value
            }
        }
        get() = (tvLabel.layoutParams as MarginLayoutParams).topMargin

    var labelTypeface: Typeface
        set(value) {
            tvLabel.typeface = value
        }
        get() = tvLabel.typeface

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            ivIcon.setImageDrawable(iconSelected ?: icon)
            ivIcon.imageTintList = ColorStateList.valueOf(colorSelected ?: color)
            tvLabel.setTextColor(colorSelected ?: color)
        } else {
            ivIcon.setImageDrawable(icon)
            ivIcon.imageTintList = ColorStateList.valueOf(color)
            tvLabel.setTextColor(color)
        }
    }

    init {
        ivIcon.apply {
            layoutParams = LayoutParams(24.dip, 24.dip).apply {
                this.topMargin = 13.dip
            }
        }

        tvLabel.apply {
            layoutParams =
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    this.topMargin = 12.dip
                }

            tvLabel.textSize = 14f
            tvLabel.maxLines = 1
            tvLabel.ellipsize = TextUtils.TruncateAt.END
        }

        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL

        addView(ivIcon)
        addView(tvLabel)

        context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationItem).apply {
            try {
                icon = getDrawable(R.styleable.BottomNavigationItem_icon)
                iconSelected = getDrawable(R.styleable.BottomNavigationItem_iconSelected)
                label = getString(R.styleable.BottomNavigationItem_label) ?: ""
            } finally {
                recycle()
            }
        }
    }

    private val Int.dip: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}