package id.dionix.kiro.utility

import android.text.method.PasswordTransformationMethod
import android.view.View

object Text {

    val passwordTransformationMethod = object : PasswordTransformationMethod() {
        override fun getTransformation(
            source: CharSequence?,
            view: View?
        ): CharSequence {
            return PasswordCharSequence(super.getTransformation(source, view))
        }

        inner class PasswordCharSequence(private val source: CharSequence) : CharSequence {
            override val length: Int get() = source.length

            override fun get(index: Int): Char {
                return if(source[index] == '\u2022') '●' else source[index]
            }

            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
                return source.subSequence(startIndex, endIndex)
            }
        }
    }

}