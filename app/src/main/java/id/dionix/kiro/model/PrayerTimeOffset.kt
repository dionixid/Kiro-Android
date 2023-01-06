package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value

data class PrayerTimeOffset(
    var fajr: Int = 0,
    var dhuhr: Int = 0,
    var asr: Int = 0,
    var maghrib: Int = 0,
    var isha: Int = 0
): RObject() {

    override val data: List<Value>
        get() = listOf(Value(fajr), Value(dhuhr), Value(asr), Value(maghrib), Value(isha))

    override fun assign(list: List<Value>) {
        if (list.size != 5) {
            isValid = false
            return
        }

        if (!list[0].isNumber() || !list[1].isNumber() || !list[2].isNumber() || !list[3].isNumber() || !list[4].isNumber()) {
            isValid = false
            return
        }

        fajr = list[0].toInt()
        dhuhr = list[1].toInt()
        asr = list[2].toInt()
        maghrib = list[3].toInt()
        isha = list[4].toInt()
    }
}
