package id.dionix.kiro.utility

import android.content.Context
import android.widget.Toast

object SingleToast {
    private lateinit var mToast: Toast

    fun initialize(context: Context) {
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT)
    }

    fun show(message: String, isLengthLong: Boolean = false) {
        mToast.cancel()
        mToast.setText(message)
        mToast.duration = if(isLengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        mToast.show()
    }

}