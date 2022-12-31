package id.dionix.kiro.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import id.dionix.kiro.R
import id.dionix.kiro.utility.dip

class CheckBox(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val ivCheck = AppCompatImageView(context)
    private val tvDescription = TextView(context)

    private var onCheckedChange: (isChecked: Boolean) -> Unit = {}

    var isChecked: Boolean = false
        set(value) {
            field = value
            ivCheck.apply {
                setImageResource(
                    if (value) R.drawable.ic_round_check_box
                    else R.drawable.ic_round_check_box_outline_blank
                )

                imageTintList = ColorStateList.valueOf(
                    if (disabled) {
                        context.getColor(R.color.disabled)
                    } else {
                        if (value) checkedTint
                        else uncheckedTint
                    }
                )
            }
        }

    var description: String
        set(value) {
            tvDescription.text = value
        }
        get() = tvDescription.text.toString()

    @ColorInt
    var checkedTint: Int = 0
        set(value) {
            field = value
            isChecked = isChecked
        }

    @ColorInt
    var uncheckedTint: Int = 0
        set(value) {
            field = value
            isChecked = isChecked
        }

    @ColorInt
    var textColor: Int = 0
        set(value) {
            field = value
            tvDescription.setTextColor(value)
        }

    var disabled: Boolean = false
        set(value) {
            field = value
            isChecked = isChecked
        }

    init {
        ivCheck.apply {
            layoutParams = LayoutParams(20.dip, 20.dip)
        }

        tvDescription.apply {
            layoutParams =
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    marginStart = 8.dip
                }
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            typeface = ResourcesCompat.getFont(context, R.font.dm_sans)
        }

        addView(ivCheck)
        addView(tvDescription)

        gravity = Gravity.CENTER_VERTICAL

        setOnClickListener {
            if (!disabled) {
                isChecked = !isChecked
                onCheckedChange(isChecked)
            }
        }

        context.obtainStyledAttributes(attrs, R.styleable.CheckBox).apply {
            try {
                checkedTint = getColor(
                    R.styleable.CheckBox_checkedTint,
                    context.getColor(R.color.secondary)
                )
                uncheckedTint = getColor(
                    R.styleable.CheckBox_uncheckedTint,
                    context.getColor(R.color.disabled)
                )
                isChecked = getBoolean(R.styleable.CheckBox_checked, false)
                description = getString(R.styleable.CheckBox_description) ?: ""
                textColor = getColor(
                    R.styleable.CheckBox_textColor,
                    context.getColor(R.color.text_default)
                )
                disabled = getBoolean(R.styleable.CheckBox_disabled, false)
            } finally {
                recycle()
            }
        }
    }

    fun setOnCheckedChangeListener(listener: (isChecked: Boolean) -> Unit) {
        onCheckedChange = listener
    }

}