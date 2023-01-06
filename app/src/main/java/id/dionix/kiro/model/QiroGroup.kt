package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value
import java.time.DayOfWeek

data class QiroGroup(
    var dayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
    var fajr: Qiro = Qiro(name = Prayer.Name.Fajr),
    var dhuhr: Qiro = Qiro(name = Prayer.Name.Dhuhr),
    var asr: Qiro = Qiro(name = Prayer.Name.Asr),
    var maghrib: Qiro = Qiro(name = Prayer.Name.Maghrib),
    var isha: Qiro = Qiro(name = Prayer.Name.Isha)
) : RObject() {

    fun getQiro(name: Prayer.Name): Qiro {
        return when (name) {
            Prayer.Name.Fajr -> fajr
            Prayer.Name.Dhuhr -> dhuhr
            Prayer.Name.Asr -> asr
            Prayer.Name.Maghrib -> maghrib
            Prayer.Name.Isha -> isha
        }
    }

    fun deepCopy(
        dayOfWeek: DayOfWeek = this.dayOfWeek,
        fajr: Qiro = this.fajr.deepCopy(),
        dhuhr: Qiro = this.dhuhr.deepCopy(),
        asr: Qiro = this.asr.deepCopy(),
        maghrib: Qiro = this.maghrib.deepCopy(),
        isha: Qiro = this.isha.deepCopy()
    ): QiroGroup {
        return QiroGroup(dayOfWeek, fajr, dhuhr, asr, maghrib, isha)
    }

    override val data: List<Value>
        get() = listOf(
            Value(dayOfWeek.value),
            Value(fajr),
            Value(dhuhr),
            Value(asr),
            Value(maghrib),
            Value(isha)
        )

    override fun assign(list: List<Value>) {
        if (list.size != 6) {
            isValid = false
            return
        }

        if (!list[0].isNumber() || !list[1].isObject() || !list[2].isObject() || !list[3].isObject() || !list[4].isObject() || !list[5].isObject()) {
            isValid = false
            return
        }

        if (list[0].toInt() !in 1..7) {
            isValid = false
        }

        dayOfWeek = DayOfWeek.values()[list[0].toInt() - 1]
        fajr = list[1].toObject { Qiro() }
        dhuhr = list[2].toObject { Qiro() }
        asr = list[3].toObject { Qiro() }
        maghrib = list[4].toObject { Qiro() }
        isha = list[5].toObject { Qiro() }
    }

}
