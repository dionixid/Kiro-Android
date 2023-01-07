package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value

data class SurahCollection(
    var name: String = "",
    var totalSize: Int = 0,
    var progress: Int = 0
) : RObject() {

    val isFinished: Boolean get() = totalSize == progress
    val percentInt: Int get() = if(totalSize == 0) 0 else ((100 * progress) / totalSize).coerceIn(0, 100)

    override fun hashCode(): Int {
        return name.hashCode() + totalSize.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is SurahCollection && name == other.name && totalSize == other.totalSize
    }

    override val data: List<Value>
        get() = listOf(Value(name), Value(totalSize), Value(progress))

    override fun assign(list: List<Value>) {
        if (list.size != 3) {
            isValid = false
            return
        }

        if (!list[0].isString() || !list[1].isNumber() || !list[2].isNumber()) {
            isValid = false
            return
        }

        name = list[0].toString()
        totalSize = list[1].toInt()
        progress = list[2].toInt()
    }

}