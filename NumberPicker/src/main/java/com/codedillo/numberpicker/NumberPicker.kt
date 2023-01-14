package com.codedillo.numberpicker

import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class NumberPicker(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val rvPicker = RecyclerView(context)
    private val numberSets = arrayListOf<NumberSet>()

    init {
        addView(rvPicker)
        rvPicker.itemAnimator = null
        rvPicker.setHasFixedSize(true)
        rvPicker.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        rvPicker.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
    }

    fun addNumberSet(numberSet: NumberSet) {
        numberSets.add(numberSet)
        rvPicker.adapter = PickerAdapter()
        rvPicker.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    fun getValue(tag: String): Float {
        numberSets.forEach {
            if (it.tag == tag) {
                return@getValue it.value.currentValue
            }
        }
        return -1f
    }

    private inner class PickerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            when (viewType) {
                0 -> {
                    val rlPicker = RelativeLayout(context)
                    val rvNumber = RecyclerView(context)
                    val ivDividerTop = AppCompatImageView(context)
                    val ivDividerBottom = AppCompatImageView(context)

                    rlPicker.addView(rvNumber)
                    rlPicker.addView(ivDividerTop)
                    rlPicker.addView(ivDividerBottom)

                    rlPicker.layoutParams = LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    rvNumber.tag = RV_NUMBER_TAG
                    ivDividerTop.tag = DIVIDER_TOP_TAG
                    ivDividerBottom.tag = DIVIDER_BOT_TAG

                    return ViewHolderNumber(rlPicker)
                }
                else -> {
                    val llPicker = LinearLayout(context)
                    val tvSeparator = TextView(context)

                    tvSeparator.tag = SEPARATOR_TAG
                    llPicker.apply {
                        layoutParams = LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        orientation = HORIZONTAL
                        gravity = Gravity.CENTER
                        addView(tvSeparator)
                    }

                    tvSeparator.layoutParams = LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = 2.dip
                    }

                    return ViewHolderSeparator(llPicker)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                0 -> {
                    (holder as? ViewHolderNumber)?.apply {
                        numberSet = numberSets[position / 2]
                    }
                }
                else -> {
                    (holder as? ViewHolderSeparator)?.apply {
                        textSeparator = numberSets[(position - 1) / 2].separator.text
                        textAppearanceSpec = numberSets[(position - 1) / 2].separator.appearance
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return 2 * numberSets.size - 1
        }

        override fun getItemViewType(position: Int): Int {
            return position % 2
        }

        inner class ViewHolderSeparator(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvSeparator = itemView.findViewWithTag<TextView?>(SEPARATOR_TAG)

            var textSeparator: String = ""
                set(value) {
                    field = value
                    tvSeparator?.text = value
                }

            var textAppearanceSpec: TextAppearance = TextAppearance()
                set(value) {
                    field = value
                    tvSeparator?.apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, value.size)
                        setTextColor(value.color)
                        typeface = value.typeface
                    }
                }

        }

        inner class ViewHolderNumber(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val rvNumber = itemView.findViewWithTag<RecyclerView?>(RV_NUMBER_TAG)
            private val ivDivTop = itemView.findViewWithTag<AppCompatImageView?>(DIVIDER_TOP_TAG)
            private val ivDivBot = itemView.findViewWithTag<AppCompatImageView?>(DIVIDER_BOT_TAG)

            private var numberAdapter = NumberAdapter()
            private var scrolledByUser = false

            private var currentNumberPosition = 0

            private var manager = object : LinearLayoutManager(context, VERTICAL, false) {

                private fun updateAppearance(state: RecyclerView.State?) {
                    val center = height / 2f
                    val d0 = 0f
                    val d1 = 0.7f * center
                    val d2 = 1f * center
                    val s0 = numberSet.value.scale
                    val s1 = 1f

                    for (i in 0 until childCount) {
                        getChildAt(i)?.let { child ->
                            val offset = center - child.measuredHeight / 2
                            val top = getDecoratedTop(child)
                            val bottom = getDecoratedBottom(child)
                            val childCenter = if (numberSet.isEndlessModeEnabled) {
                                (top + bottom) / 2f
                            } else {
                                when (getPosition(child)) {
                                    0 -> {
                                        (top + bottom + offset) / 2f
                                    }
                                    (state?.itemCount ?: itemCount) - 1 -> {
                                        (top + bottom - offset) / 2f
                                    }
                                    else -> {
                                        (top + bottom) / 2f
                                    }
                                }
                            }
                            val d = min(d1, abs(center - childCenter))
                            val scaleMultiplier = (d - d0) / (d1 - d0)
                            val calculatedScale = s0 + (s1 - s0) * scaleMultiplier

                            child.scaleX = calculatedScale
                            child.scaleY = calculatedScale

                            val da = min(max(d1, abs(center - childCenter)), d2)
                            child.alpha = 1f - (da - d1) / (d2 - d1)

                            val targetColor = Color(numberSet.value.appearance.colorFocus)
                            val diffColor = Color(numberSet.value.appearance.color) - targetColor
                            val calculatedColor = (targetColor + diffColor * scaleMultiplier).argb
                            (child as? LinearLayout)?.let {
                                (it.getChildAt(0) as? TextView)?.setTextColor(calculatedColor)
                            }
                        }
                    }
                }

                override fun scrollVerticallyBy(
                    dy: Int,
                    recycler: RecyclerView.Recycler,
                    state: RecyclerView.State
                ): Int {
                    val scrolled = super.scrollVerticallyBy(dy, recycler, state)
                    updateAppearance(state)
                    return scrolled
                }

                override fun scrollToPositionWithOffset(position: Int, offset: Int) {
                    super.scrollToPositionWithOffset(position, offset)
                    updateAppearance(null)
                }

                override fun supportsPredictiveItemAnimations(): Boolean {
                    return false
                }
            }

            private val decoration = object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    view.measure(0, 0)
                    val offset = (manager.height / 2) - (view.measuredHeight / 2)
                    when (parent.getChildLayoutPosition(view)) {
                        0 -> {
                            outRect.top = offset
                        }
                        state.itemCount - 1 -> {
                            outRect.bottom = offset
                        }
                    }
                }
            }

            init {
                val snapHelper = LinearSnapHelper()
                snapHelper.attachToRecyclerView(rvNumber)

                rvNumber.apply {
                    itemAnimator = null
                    overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                    setHasFixedSize(true)

                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrollStateChanged(
                            recyclerView: RecyclerView,
                            newState: Int
                        ) {
                            when (newState) {
                                RecyclerView.SCROLL_STATE_DRAGGING -> {
                                    scrolledByUser = true
                                }
                                RecyclerView.SCROLL_STATE_IDLE -> {
                                    snapHelper.findSnapView(manager)?.let {
                                        currentNumberPosition = manager.getPosition(it)
                                        numberSet.value.currentValue =
                                            numberAdapter.getNumber(currentNumberPosition)
                                        numberSet.value.onChanged(numberSet.value.currentValue, scrolledByUser)
                                    }
                                    scrolledByUser = false
                                }
                            }
                        }
                    })

                    addOnItemTouchListener(object: RecyclerView.OnItemTouchListener {
                        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
                        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

                        override fun onInterceptTouchEvent(
                            rv: RecyclerView,
                            e: MotionEvent
                        ): Boolean {
                            if (e.actionMasked == MotionEvent.ACTION_MOVE) {
                                parent.requestDisallowInterceptTouchEvent(true)
                            }
                            return false
                        }

                    })

                    layoutManager = manager
                    adapter = numberAdapter
                }
            }

            var numberSet: NumberSet = NumberSet()
                set(value) {
                    field = value

                    numberAdapter.numberSet = value
                    rvNumber.apply {
                        layoutParams = RelativeLayout.LayoutParams(
                            value.width,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setPadding(
                            value.leftSpacing,
                            paddingTop,
                            value.rightSpacing,
                            paddingBottom
                        )
                        if (!numberSet.isEndlessModeEnabled && itemDecorationCount == 0) {
                            addItemDecoration(decoration)
                        }

                    }

                    setCurrentValue(value.value.currentValue)

                    ivDivTop?.layoutParams = RelativeLayout.LayoutParams(
                        numberSet.divider.width,
                        numberSet.divider.height
                    ).apply {
                        addRule(RelativeLayout.CENTER_IN_PARENT)
                    }

                    ivDivBot?.layoutParams = RelativeLayout.LayoutParams(
                        numberSet.divider.width,
                        numberSet.divider.height
                    ).apply {
                        addRule(RelativeLayout.CENTER_IN_PARENT)
                    }

                    ivDivTop?.setImageDrawable(ColorDrawable(numberSet.divider.color))
                    ivDivBot?.setImageDrawable(ColorDrawable(numberSet.divider.color))

                    ivDivTop?.translationY = -numberSet.divider.topSpacing
                    ivDivBot?.translationY = numberSet.divider.bottomSpacing

                    value.value.setValueImpl = {
                        setCurrentValue(it)
                    }

                    value.value.invalidateImpl = {
                        numberAdapter.invalidate(currentNumberPosition)
                        setCurrentValue(value.value.currentValue, 1)
                    }
                }

            fun setCurrentValue(value: Float, smoothScrollDelta: Int = 1) {
                val center = Int.MAX_VALUE / 2
                val valueInCenter = center % numberAdapter.size

                val firstPos = if (numberSet.isEndlessModeEnabled) {
                    center - valueInCenter
                } else {
                    0
                }

                numberSet.value.currentValue = value
                if (numberSet.value.currentValue > numberAdapter.maxValue) {
                    numberSet.value.currentValue = numberAdapter.maxValue
                }

                val targetValue =
                    ((numberSet.value.currentValue - numberSet.value.min) / numberSet.value.interval).toInt()
                val currentValuePosition = firstPos + targetValue

                rvNumber.post {
                    val middle = manager.height / 2
                    val childHeight = manager.getChildAt(0)?.measuredHeight ?: 1
                    val offset = middle - childHeight / 2

                    manager.scrollToPositionWithOffset(currentValuePosition, offset)
                    if (smoothScrollDelta > 0) {
                        rvNumber.smoothScrollBy(0, smoothScrollDelta)
                    }
                }
            }

        }

    }

    private inner class NumberAdapter : RecyclerView.Adapter<NumberAdapter.ViewHolder>() {

        private val numbers = ArrayList<Float>()

        val size: Int get() = numbers.size
        val maxValue: Float get() = numbers.lastOrNull() ?: numberSet.value.min

        var numberSet: NumberSet = NumberSet().apply { value.count = 50 }
            set(value) {
                when {
                    field.value.count != value.value.count
                            || field.value.min != value.value.min
                            || field.value.interval != value.value.interval -> {
                        numbers.clear()
                        for (i in 0 until value.value.count) {
                            numbers.add(value.value.min + i * value.value.interval)
                        }
                    }
                }
                field = value
            }

        fun invalidate(position: Int) {
            val oldSize = numbers.size
            numbers.clear()
            for (i in 0 until numberSet.value.count) {
                numbers.add(numberSet.value.min + i * numberSet.value.interval)
            }

            if (numberSet.isEndlessModeEnabled) {
                notifyItemRangeChanged(position - 10, 20, position)
            } else {
                val start = if (position > 10) position - 10 else 0
                val count = if (oldSize - start > 20) 20 else oldSize - start

                notifyItemRangeChanged(
                    start,
                    count,
                    position
                )
            }
        }

        fun getNumber(position: Int): Float {
            return numbers[position % size]
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val llNumber = LinearLayout(context)
            val tvNumber = TextView(context)

            llNumber.addView(tvNumber)
            tvNumber.tag = NUMBER_TAG

            llNumber.layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            llNumber.gravity = Gravity.CENTER

            tvNumber.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            tvNumber.layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            return ViewHolder(llNumber)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.alpha = 0f
            holder.value = numbers[position % size]
            holder.textSize = numberSet.value.appearance.size
            holder.textColor = numberSet.value.appearance.color
            holder.typeface = numberSet.value.appearance.typeface
            holder.rowSpacing = numberSet.value.rowSpacing
        }

        override fun getItemCount(): Int {
            return if (numberSet.isEndlessModeEnabled) Int.MAX_VALUE else size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvValue = itemView.findViewWithTag<TextView?>(NUMBER_TAG)

            var value: Float = 0f
                set(value) {
                    field = value
                    tvValue?.text = numberSet.value.format(value)
                }

            var textSize: Float? = null
                set(value) {
                    if (field != value && value != null) {
                        tvValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
                        field = value
                    }
                }

            var textColor: Int? = null
                set(value) {
                    if (field != value && value != null) {
                        tvValue.setTextColor(value)
                        field = value
                    }
                }

            var typeface: Typeface? = null
                set(value) {
                    if (field != value && value != null) {
                        tvValue.typeface = value
                        field = value
                    }
                }

            var rowSpacing: Int? = null
                set(value) {
                    if (field != value && value != null) {
                        tvValue.setPaddingVertical(value / 2)
                        field = value
                    }
                }

        }

    }

    data class NumberSet(
        var tag: String = "",
        var width: Int = 60.dip,
        var leftSpacing: Int = 0,
        var rightSpacing: Int = 0,
        var isEndlessModeEnabled: Boolean = false,
        var separator: Separator = Separator(),
        var value: Value = Value(),
        var divider: LineDivider = LineDivider()
    )

    data class Separator(
        var text: String = "",
        var appearance: TextAppearance = TextAppearance()
    )

    data class Value(
        var currentValue: Float = 0f,
        var min: Float = 0f,
        var interval: Float = 1f,
        var count: Int = 100,
        var rowSpacing: Int = 24.dip,
        var scale: Float = 1.3f,
        var appearance: TextAppearance = TextAppearance(size = 11f),
    ) {

        internal var setValueImpl: (value: Float) -> Unit = {}
        internal var invalidateImpl: () -> Unit = {}

        private var onChangedImpl: (value: Float, fromUser: Boolean) -> Unit = { _, _ ->}
        private var formatterImpl: (value: Float) -> String = {
            String.format(Locale.US, "%d", it.toInt())
        }

        fun setValue(value: Float) {
            setValueImpl(value)
        }

        fun invalidate() {
            invalidateImpl()
        }

        fun setOnValueChangedListener(listener: (value: Float, fromUser: Boolean) -> Unit) {
            onChangedImpl = listener
        }

        fun setFormatter(formatter: (value: Float) -> String) {
            formatterImpl = formatter
        }

        fun format(value: Float): String {
            return formatterImpl(value)
        }

        internal fun onChanged(value: Float, fromUser: Boolean) {
            onChangedImpl(value, fromUser)
        }

    }

    data class TextAppearance(
        var size: Float = 14f,
        var color: Int = Color.BLACK,
        var colorFocus: Int = Color.BLACK,
        var typeface: Typeface = Typeface.DEFAULT
    )

    data class LineDivider(
        var height: Int = 1.2.dip,
        var width: Int = 40.dip,
        var color: Int = Color.BLACK,
        var topSpacing: Float = 20.dp,
        var bottomSpacing: Float = 20.dp
    )

    companion object {
        private const val NUMBER_TAG = "number"
        private const val SEPARATOR_TAG = "separator"
        private const val RV_NUMBER_TAG = "rv_number"
        private const val DIVIDER_TOP_TAG = "divider_top"
        private const val DIVIDER_BOT_TAG = "divider_bot"
    }

}