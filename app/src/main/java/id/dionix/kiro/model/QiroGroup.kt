package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value

data class QiroGroup(
    var fajr: Qiro = Qiro(name = Prayer.Name.Fajr),
    var dhuhr: Qiro = Qiro(name = Prayer.Name.Dhuhr),
    var asr: Qiro = Qiro(name = Prayer.Name.Asr),
    var maghrib: Qiro = Qiro(name = Prayer.Name.Maghrib),
    var isha: Qiro = Qiro(name = Prayer.Name.Isha)
) : RObject() {

    fun getQiro(name: Prayer.Name): Qiro {
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

        fajr = list[0].toObject { Qiro() }
        dhuhr = list[1].toObject { Qiro() }
        asr = list[2].toObject { Qiro() }
        maghrib = list[3].toObject { Qiro() }
        isha = list[4].toObject { Qiro() }
    }

}
