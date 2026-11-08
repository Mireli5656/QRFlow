package qrflow.domain

import java.net.URLEncoder

data class ContactData(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val organization: String = "",
    val jobTitle: String = "",
    val website: String = "",
    val address: String = "",
    val note: String = ""
)

object QrPayloads {

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val webLinkRegex = Regex("^https?://\\S+$", RegexOption.IGNORE_CASE)
    private val vCardNameRegex = Regex("^FN:(.*)$", RegexOption.MULTILINE)

    // ---------- Builders ----------

    fun text(input: String): String = input

    fun url(input: String): String {
        val trimmed = input.trim()
        return if (trimmed.contains("://")) trimmed else "https://$trimmed"
    }

    fun email(to: String, subject: String = "", body: String = ""): String {
        val params = buildList {
            if (subject.isNotBlank()) add("subject=${urlEncode(subject)}")
            if (body.isNotBlank()) add("body=${urlEncode(body)}")
        }
        val query = if (params.isEmpty()) "" else "?" + params.joinToString("&")
        return "mailto:${to.trim()}$query"
    }

    fun phone(number: String): String = "tel:${cleanNumber(number)}"

    fun sms(number: String, message: String = ""): String =
        "SMSTO:${cleanNumber(number)}:$message"

    fun vCard(contact: ContactData): String {
        val lines = mutableListOf("BEGIN:VCARD", "VERSION:3.0")
        lines.add("N:${vEscape(contact.lastName)};${vEscape(contact.firstName)};;;")
        lines.add("FN:${vEscape(fullName(contact))}")
        if (contact.organization.isNotBlank()) lines.add("ORG:${vEscape(contact.organization)}")
        if (contact.jobTitle.isNotBlank()) lines.add("TITLE:${vEscape(contact.jobTitle)}")
        if (contact.phone.isNotBlank()) lines.add("TEL;TYPE=CELL:${vEscape(contact.phone)}")
        if (contact.email.isNotBlank()) lines.add("EMAIL:${vEscape(contact.email)}")
        if (contact.website.isNotBlank()) lines.add("URL:${contact.website.trim()}")
        if (contact.address.isNotBlank()) lines.add("ADR:;;${vEscape(contact.address)};;;;")
        if (contact.note.isNotBlank()) lines.add("NOTE:${vEscape(contact.note)}")
        lines.add("END:VCARD")
        return lines.joinToString("\n")
    }

    fun wifi(config: WifiConfig): String {
        val sb = StringBuilder("WIFI:")
        sb.append("T:").append(config.security.qrValue).append(';')
        sb.append("S:").append(wifiEscape(config.ssid)).append(';')
        if (config.security != WifiSecurity.OPEN) {
            sb.append("P:").append(wifiEscape(config.password)).append(';')
        }
        if (config.hidden) sb.append("H:true;")
        sb.append(';')
        return sb.toString()
    }

    /** Escapes \ ; , " : with a backslash, as required by the Wi-Fi QR format. */
    fun wifiEscape(value: String): String {
        val sb = StringBuilder(value.length + 4)
        for (ch in value) {
            if (ch == '\\' || ch == ';' || ch == ',' || ch == '"' || ch == ':') sb.append('\\')
            sb.append(ch)
        }
        return sb.toString()
    }

    // ---------- Parsing / detection ----------

    fun parseWifi(content: String): WifiConfig? {
        if (!content.startsWith("WIFI:", ignoreCase = true)) return null
        val body = content.substring(5)
        val fields = mutableMapOf<String, String>()
        val current = StringBuilder()
        var key: String? = null
        var i = 0
        while (i < body.length) {
            val c = body[i]
            when {
                c == '\\' && i + 1 < body.length -> {
                    current.append(body[i + 1])
                    i++
                }
                c == ':' && key == null -> {
                    key = current.toString()
                    current.clear()
                }
                c == ';' -> {
                    key?.let { fields[it] = current.toString() }
                    key = null
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        key?.let { fields[it] = current.toString() }

        val ssid = fields["S"] ?: return null
        val security = when (fields["T"]?.uppercase()) {
            null, "", "NOPASS" -> WifiSecurity.OPEN
            "WEP" -> WifiSecurity.WEP
            else -> WifiSecurity.WPA
        }
        return WifiConfig(
            ssid = ssid,
            password = fields["P"].orEmpty(),
            security = security,
            hidden = fields["H"].equals("true", ignoreCase = true)
        )
    }

    fun detectType(content: String): QrType {
        val c = content.trimStart()
        return when {
            c.startsWith("WIFI:", true) -> QrType.WIFI
            c.startsWith("BEGIN:VCARD", true) -> QrType.CONTACT
            c.startsWith("mailto:", true) || c.startsWith("MATMSG:", true) -> QrType.EMAIL
            c.startsWith("tel:", true) -> QrType.PHONE
            c.startsWith("smsto:", true) || c.startsWith("sms:", true) -> QrType.SMS
            webLinkRegex.matches(c.trim()) -> QrType.URL
            else -> QrType.TEXT
        }
    }

    /** Returns the content if it is an http(s) link, otherwise null. */
    fun webLink(content: String): String? =
        content.trim().takeIf { webLinkRegex.matches(it) }

    fun title(type: QrType, content: String): String {
        val result: String? = when (type) {
            QrType.WIFI -> parseWifi(content)?.let { "Wi-Fi: ${it.ssid}" }
            QrType.CONTACT -> vCardNameRegex.find(content)?.groupValues?.get(1)?.let { vUnescape(it) }
            QrType.EMAIL -> content.substringAfter(':').substringBefore('?')
            QrType.PHONE -> content.substringAfter(':')
            QrType.SMS -> content.substringAfter(':').substringBefore(':')
            else -> content.lineSequence().firstOrNull { it.isNotBlank() }
        }
        return result?.trim()?.takeIf { it.isNotEmpty() }?.take(80) ?: type.label
    }

    // ---------- Validation ----------

    fun isValidEmail(value: String): Boolean = emailRegex.matches(value.trim())

    fun isValidPhone(value: String): Boolean =
        cleanNumber(value).count { it.isDigit() } >= 3

    // ---------- Helpers ----------

    private fun cleanNumber(number: String): String =
        number.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }

    private fun fullName(contact: ContactData): String =
        listOf(contact.firstName, contact.lastName)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(" ")

    private fun urlEncode(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    private fun vEscape(value: String): String = value.trim()
        .replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\r\n", "\\n")
        .replace("\n", "\\n")

    private fun vUnescape(value: String): String = value
        .replace("\\,", ",")
        .replace("\\;", ";")
        .replace("\\n", " ")
        .replace("\\\\", "\\")
}
