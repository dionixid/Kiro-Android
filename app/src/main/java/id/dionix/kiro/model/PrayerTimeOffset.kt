package id.dionix.kiro.model

data class PrayerTimeOffset(
    var fajr: Int,
    var dhuhr: Int,
    var asr: Int,
    var maghrib: Int,
    var isha: Int
)