package id.dionix.marginslider

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.cardview.widget.CardView
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import kotlin.math.max
import kotlin.math.min

class MarginSlider(
    context: Context,
    attrs: AttributeSet?
) : CardView(context, attrs) {

    private var overlay: View? = null

    var anchorMargin: Int = 0
    var maxMargin: Int = 0

    private var onOpen: () -> Unit = {}
    private var onClose: () -> Unit = {}

    private var animator = ValueAnimator()

    private var _isOpen: Boolean = false
    val isOpen: Boolean get() = _isOpen

    private var onSizeChanged: (newHeight: Int) -> Unit = {}
    private var onSlideChanged: (value: Int) -> Unit = {}
    private var onInterceptEvent: (event: MotionEvent) -> Boolean = { false }

    fun open() {
        animator.cancel()
        overlay?.visibility = View.VISIBLE
        animator =
            ValueAnimator.ofInt((layoutParams as MarginLayoutParams).topMargin, maxMargin).apply {
                duration = DEFAULT_ANIMATION_DURATION
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    val progress = it.animatedValue as Int

                    val params = layoutParams as MarginLayoutParams
                    params.topMargin = progress
                    layoutParams = params

                    onSlideChanged(anchorMargin - progress)
                    onSizeChanged(measuredHeight)

                    overlay?.alpha =
                        max(
                            min((progress - anchorMargin).toFloat() / (maxMargin - anchorMargin), 1f),
                            0f
                        )
                }

                addEndListener {
                    _isOpen = true
                }
            }
        animator.start()
        onOpen()
    }

    fun close() {
        animator.cancel()
        animator =
            ValueAnimator.ofInt((layoutParams as MarginLayoutParams).topMargin, anchorMargin).apply {
                duration = DEFAULT_ANIMATION_DURATION
                interpolator = DecelerateInterpolator()

                addUpdateListener {
                    val progress = it.animatedValue as Int

                    val params = layoutParams as MarginLayoutParams
                    params.topMargin = progress
                    layoutParams = params

                    onSlideChanged(anchorMargin - progress)
                    onSizeChanged(measuredHeight)

                    overlay?.alpha =
                        max(
                            min((progress - anchorMargin).toFloat() / (maxMargin - anchorMargin), 1f),
                            0f
                        )
                }

                addEndListener {
                    overlay?.visibility = View.GONE
                    _isOpen = false
                }
            }
        animator.start()
        onClose()
    }

    private var lastTouchY = 0
    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var posY = 0
    private var lastMillis = 0L
    private var velocityY = 0f

    init {
        setOnTouchListener { v, event ->
            event.offsetLocation(event.rawX - event.x, event.rawY - event.y)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchY = event.getY(event.actionIndex).toInt()
                    activePointerId = event.getPointerId(0)
                    posY = (layoutParams as MarginLayoutParams).topMargin
                    lastMillis = System.currentTimeMillis()
                    overlay?.visibility = View.VISIBLE
                }

                MotionEvent.ACTION_MOVE -> {
                    val y = event.getY(event.findPointerIndex(activePointerId))
                    val deltaMillis = System.currentTimeMillis() - lastMillis
                    if (deltaMillis != 0L) {
                        velocityY = (y - lastTouchY) / deltaMillis
                    }

                    posY += y.toInt() - lastTouchY
                    lastTouchY = y.toInt()

                    val params = layoutParams as MarginLayoutParams
                    params.topMargin = posY
                    validateParams(params)
                    layoutParams = params

                    onSlideChanged(anchorMargin - posY)
                    onSizeChanged(measuredHeight)

                    overlay?.alpha =
                        max(min((posY - anchorMargin).toFloat() / (maxMargin - anchorMargin), 1f), 0f)
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    when {
                        velocityY > 0.03f -> {
                            close()
                        }
                        velocityY < -0.03f -> {
                            open()
                        }
                        posY <= (maxMargin + anchorMargin) / 2 -> {
                            open()
                        }
                        else -> {
                            close()
                        }
                    }

                    posY = 0
                    velocityY = 0f
                    activePointerId = MotionEvent.INVALID_POINTER_ID
                    v.performClick()
                    isIntercepted = false
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    event.getPointerId(event.actionIndex).takeIf { it == activePointerId }?.run {
                        val newPointerIndex = if (this == 0) 1 else 0
                        lastTouchY = event.getY(newPointerIndex).toInt()
                        activePointerId = event.getPointerId(newPointerIndex)
                    }
                }
            }

            return@setOnTouchListener true
        }

        context.obtainStyledAttributes(attrs, R.styleable.MarginSlider).apply {
            try {
                anchorMargin = getDimension(R.styleable.MarginSlider_anchor, 0f).toInt()
                maxMargin = getDimension(R.styleable.MarginSlider_max, 0f).toInt()
            } finally {
                recycle()
            }
        }
    }

    private fun validateParams(params: MarginLayoutParams) {
        when {
            params.topMargin < maxMargin -> params.topMargin = maxMargin
            params.topMargin > anchorMargin -> params.topMargin = anchorMargin
        }
    }

    private var initialY = 0f
    private var isIntercepted = false

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                initialY = ev.y
            } else if (ev.actionMasked == MotionEvent.ACTION_MOVE) {
                if (onInterceptEvent(it)
                    && !isIntercepted
                    && ((!isOpen && (ev.y - initialY) < 0) || (isOpen && (ev.y - initialY) > 0))
                ) {
                    isIntercepted = true
                    lastTouchY = ev.rawY.toInt()
                    activePointerId = ev.getPointerId(0)
                    lastMillis = System.currentTimeMillis()
                    posY = (layoutParams as MarginLayoutParams).topMargin
                    overlay?.visibility = View.VISIBLE
                }
            }
            return isIntercepted
        }
        return super.onInterceptTouchEvent(ev)
    }

    fun setOverlay(overlay: View) {
        this.overlay = overlay
    }

    fun setOnSizeChangedListener(listener: (newSize: Int) -> Unit) {
        onSizeChanged = listener
    }

    fun setOnSlideChangedListener(listener: (value: Int) -> Unit) {
        onSlideChanged = listener
    }

    fun setOnOpenListener(listener: () -> Unit) {
        onOpen = listener
    }

    fun setOnCloseListener(listener: () -> Unit) {
        onClose = listener
    }

    fun setOnInterceptEvent(listener: (event: MotionEvent) -> Boolean) {
        onInterceptEvent = listener
    }

    private fun ValueAnimator.addEndListener(listener: () -> Unit) {
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                listener()
            }
        })
    }


    companion object {
        private const val DEFAULT_ANIMATION_DURATION = 250L
    }

}