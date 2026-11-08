package qrflow.domain

enum class QrType(val label: String) {
    TEXT("Text"),
    URL("URL"),
    EMAIL("Email"),
    PHONE("Phone"),
    SMS("SMS"),
    CONTACT("Contact"),
    WIFI("Wi-Fi");

    companion object {
        fun fromName(name: String?): QrType =
            QrType.entries.firstOrNull { it.name == name } ?: TEXT
    }
}
