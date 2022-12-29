package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value

data class PrayerGroup(
    var fajr: Prayer = Prayer(name = Prayer.Name.Fajr),
    var dhuhr: Prayer = Prayer(name = Prayer.Name.Dhuhr),
    var asr: Prayer = Prayer(name = Prayer.Name.Asr),
    var maghrib: Prayer = Prayer(name = Prayer.Name.Maghrib),
    var isha: Prayer = Prayer(name = Prayer.Name.Isha)
): RObject() {

    fun getPrayer(name: Prayer.Name): Prayer {
        return when(name) {
            Prayer.Name.Fajr -> fajr
            Prayer.Name.Dhuhr -> dhuhr
            Prayer.Name.Asr -> asr
            Prayer.Name.Maghrib -> maghrib
            Prayer.Name.Isha -> isha
        }
    }

    override val data: List<Value>
        get() = listOf(Value(fajr), Value(dhuhr), Value(asr), Value(maghrib), Value(isha))

    override fun assign(list: List<Value>) {
        if (list.size != 5) {
            isValid = false
            return
        }

        if (!list[0].isObject() || !list[1].isObject() || !list[2].isObject() || !list[3].isObject() || !list[4].isObject()) {
            isValid = false
            return
        }

        fajr = list[0].toObject { Prayer() }
        dhuhr = list[1].toObject { Prayer() }
        asr = list[2].toObject { Prayer() }
        maghrib = list[3].toObject { Prayer() }
        isha = list[4].toObject { Prayer() }
    }
}
