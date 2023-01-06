package id.dionix.kiro.model

data class SurahCollection(
    var name: String = "",
    var total: Int = 0,
    var progress: Int = 0
) {

    val isFinished: Boolean get() = total == progress
    val percentInt: Int get() = if(total == 0) 0 else ((100 * progress) / total).coerceIn(0, 100)

    override fun hashCode(): Int {
        return name.hashCode() + total.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is SurahCollection && name == other.name && total == other.total
    }
}