package qrflow.domain

enum class WifiSecurity(val label: String, val qrValue: String) {
    WPA("WPA/WPA2/WPA3", "WPA"),
    WEP("WEP", "WEP"),
    OPEN("Open (no password)", "nopass")
}

data class WifiConfig(
    val ssid: String,
    val password: String = "",
    val security: WifiSecurity = WifiSecurity.WPA,
    val hidden: Boolean = false
)
