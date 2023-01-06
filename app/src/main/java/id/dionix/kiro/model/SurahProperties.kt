package id.dionix.kiro.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value

@Entity(tableName = "surah")
data class SurahProperties(
    @PrimaryKey var id: Int = 0,
    var name: String = "",
    var volume: Int = 20,
    @ColumnInfo(name = "duration_seconds") var durationSeconds: Int = 0
) : RObject() {

    fun toSurah() : Surah {
        return Surah(id, volume)
    }

    @Ignore
    override var isValid: Boolean = true

    override val data: List<Value>
        get() = listOf(Value(id), Value(name), Value(volume), Value(durationSeconds))

    override fun assign(list: List<Value>) {
        if (list.size != 4) {
            isValid = false
            return
        }

        if (!list[0].isNumber() || !list[1].isString() || !list[2].isNumber() || !list[3].isNumber()) {
            isValid = false
            return
        }

        id = list[0].toInt()
        name = list[1].toString()
        volume = list[2].toInt()
        durationSeconds = list[3].toInt()
    }
}