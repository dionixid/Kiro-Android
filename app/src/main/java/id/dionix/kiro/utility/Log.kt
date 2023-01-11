package id.dionix.kiro.utility

import android.util.Log as NativeLog

object Log {

    enum class Priority(val value: Int) {
        VERBOSE(NativeLog.VERBOSE),
        DEBUG(NativeLog.DEBUG),
        INFO(NativeLog.INFO),
        WARN(NativeLog.WARN),
        ERROR(NativeLog.ERROR),
        ASSERT(NativeLog.ASSERT)
    }

    private var mIsEnabled = false
    private var mPriority = Priority.DEBUG

    fun enable(priority: Priority) {
        mIsEnabled = true
        mPriority = priority
    }

    fun debug(tag: String, message: String) {
        if (mIsEnabled) {
            NativeLog.println(mPriority.value, tag, message)
        }
    }

}