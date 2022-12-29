package id.dionix.kiro.utility

import android.content.res.Resources
import android.graphics.Rect
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val Int.sp: Float get() = this * Resources.getSystem().displayMetrics.scaledDensity
val Int.dp: Float get() = this * Resources.getSystem().displayMetrics.density
val Int.dip: Int get() = this.dp.toInt()

val Float.sp: Float get() = this * Resources.getSystem().displayMetrics.scaledDensity
val Float.dp: Float get() = this * Resources.getSystem().displayMetrics.density
val Float.dip: Int get() = this.dp.toInt()

val Double.sp: Float get() = this.toFloat() * Resources.getSystem().displayMetrics.scaledDensity
val Double.dp: Float get() = this.toFloat() * Resources.getSystem().displayMetrics.density
val Double.dip: Int get() = this.dp.toInt()

fun runMain(task: () -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        task()
    }
}

fun makeTimer(interval: Long, isContinuous: Boolean = false, callback: () -> Unit): CountDownTimer {
    return object : CountDownTimer(interval, interval) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            callback()
            if (isContinuous) {
                this.start()
            }
        }
    }
}

fun View.scaleOnClick(
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
