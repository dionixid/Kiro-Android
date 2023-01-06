package id.dionix.kiro.model

data class DeviceConfig(
    var id: String = "",
    var ip: String = "",
    var name: String = "",
    var isLocal: Boolean = false,
    var key: String = ""
)