package id.dionix.kiro

import com.codedillo.rttp.model.Value
import id.dionix.kiro.model.*
import org.junit.Assert
import org.junit.Test
import java.time.LocalTime

class DataModelUnitTest {

    @Test
    fun device_serialization_isCorrect() {
        Assert.assertEquals("{\"id\",\"name\",\"version\"}", Device("id", "name", "version").serialize())
    }

    @Test
    fun prayer_serialization_isCorrect() {
        Assert.assertEquals("{1,300,3}", Prayer(Prayer.Name.Dhuhr, LocalTime.ofSecondOfDay(300), 3).serialize())
    }

    @Test
    fun prayer_group_serialization_isCorrect() {
        Assert.assertEquals(
            "{{0,100,1},{1,200,2},{2,300,3},{3,400,4},{4,500,5}}",
            PrayerGroup(
                Prayer(Prayer.Name.Fajr, LocalTime.ofSecondOfDay(100), 1),
                Prayer(Prayer.Name.Dhuhr, LocalTime.ofSecondOfDay(200), 2),
                Prayer(Prayer.Name.Asr, LocalTime.ofSecondOfDay(300), 3),
                Prayer(Prayer.Name.Maghrib, LocalTime.ofSecondOfDay(400), 4),
                Prayer(Prayer.Name.Isha, LocalTime.ofSecondOfDay(500), 5),
            ).serialize()
        )
    }

    @Test
    fun prayer_time_offset_serialization_isCorrect() {
        Assert.assertEquals("{1,2,3,4,5}", PrayerTimeOffset(1,2,3,4,5).serialize())
    }

    @Test
    fun qiro_serialization_isCorrect() {
        Assert.assertEquals("{1,10,[{1,20},{2,20},{3,20}],20}", Qiro(Prayer.Name.Dhuhr, 10, listOf(Surah(1, 20), Surah(2, 20), Surah(3, 20)), 20).serialize())
    }

    @Test
    fun qiro_group_serialization_isCorrect() {
        Assert.assertEquals(
            "{6,{0,10,[{1,20}],10},{1,20,[{1,20},{2,20}],20},{2,30,[{1,20},{2,20},{3,20}],30},{3,40,[{1,20},{2,20},{3,20},{4,20}],40},{4,50,[{1,20},{2,20},{3,20},{4,20},{5,20}],50}}",
            QiroGroup(
                6,
                Qiro(Prayer.Name.Fajr, 10, listOf(Surah(1, 20)), 10),
                Qiro(Prayer.Name.Dhuhr, 20, listOf(Surah(1, 20), Surah(2, 20)), 20),
                Qiro(Prayer.Name.Asr, 30, listOf(Surah(1, 20), Surah(2, 20), Surah(3, 20)), 30),
                Qiro(Prayer.Name.Maghrib, 40, listOf(Surah(1, 20), Surah(2, 20), Surah(3, 20), Surah(4, 20)), 40),
                Qiro(Prayer.Name.Isha, 50, listOf(Surah(1, 20), Surah(2, 20), Surah(3, 20), Surah(4, 20), Surah(5, 20)), 50)
            ).serialize()
        )
    }

    @Test
    fun setting_serialization_isCorrect() {
        Assert.assertEquals(
            "{\"S0\",6,\"Password\",\"MyPassword\",true}",
            Setting("S0", Setting.Type.WiFi, "Password", Value("MyPassword"), true).serialize()
        )
    }

    @Test
    fun setting_group_serialization_isCorrect() {
        Assert.assertEquals(
            "{\"WiFi\",[{\"S0\",6,\"SSID\",\"MySSID\",false},{\"S1\",6,\"Password\",\"MyPassword\",true}]}",
            SettingGroup(
                "WiFi",
                listOf(
                    Setting("S0", Setting.Type.WiFi, "SSID", Value("MySSID"), false),
                    Setting("S1", Setting.Type.WiFi, "Password", Value("MyPassword"), true)
                )
            ).serialize()
        )
    }

}