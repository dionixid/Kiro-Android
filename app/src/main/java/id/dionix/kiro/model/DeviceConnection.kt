package id.dionix.kiro.model

data class DeviceConnection(
    var name: String = "",
    var version: String = "",
    var mac: String = "",
    var ip: String = "",
    var id: String = "",
    var isWifi: Boolean = false,
    var isLan: Boolean = false,
    var isInternet: Boolean = false
) {
    override fun hashCode(): Int {
        return name.hashCode() + mac.hashCode() + ip.hashCode() + id.hashCode() + version.hashCode() + isWifi.hashCode() + isLan.hashCode() + isInternet.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is DeviceConnection) {
            return name == other.name
                    && mac == other.mac
                    && ip == other.ip
                    && id == other.id
                    && version == other.version
                    && isWifi == other.isWifi
                    && isLan == other.isLan
                    && isInternet == other.isInternet
        }
        return false
    }
}