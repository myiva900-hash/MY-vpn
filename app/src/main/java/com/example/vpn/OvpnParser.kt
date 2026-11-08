package com.example.vpn

data class ParsedOvpnConfig(
    val remoteHost: String,
    val remotePort: Int,
    val protocol: String, // "UDP" or "TCP"
    val dev: String = "tun",
    val cipher: String = "AES-256-GCM",
    val caCert: String? = null,
    val clientCert: String? = null,
    val clientKey: String? = null,
    val tlsAuth: String? = null,
    val dnsServers: List<String> = listOf("1.1.1.1", "8.8.8.8"),
    val routes: List<String> = listOf("0.0.0.0/0"),
    val rawConfig: String
)

object OvpnParser {

    /**
     * Parses and validates a standard .ovpn file.
     * Throws IllegalArgumentException if configuration is malformed or missing critical remote directive.
     */
    fun parse(content: String): ParsedOvpnConfig {
        val lines = content.lines()
        var host = ""
        var port = 1194
        var proto = "UDP"
        var dev = "tun"
        var cipher = "AES-256-GCM"
        val dnsList = mutableListOf<String>()
        val routeList = mutableListOf<String>()

        var inCa = false
        var inCert = false
        var inKey = false
        var inTlsAuth = false

        val caBuilder = StringBuilder()
        val certBuilder = StringBuilder()
        val keyBuilder = StringBuilder()
        val tlsAuthBuilder = StringBuilder()

        for (rawLine in lines) {
            val line = rawLine.trim()

            // Tag parsing
            when {
                line.startsWith("<ca>", ignoreCase = true) -> { inCa = true; continue }
                line.startsWith("</ca>", ignoreCase = true) -> { inCa = false; continue }
                line.startsWith("<cert>", ignoreCase = true) -> { inCert = true; continue }
                line.startsWith("</cert>", ignoreCase = true) -> { inCert = false; continue }
                line.startsWith("<key>", ignoreCase = true) -> { inKey = true; continue }
                line.startsWith("</key>", ignoreCase = true) -> { inKey = false; continue }
                line.startsWith("<tls-auth>", ignoreCase = true) -> { inTlsAuth = true; continue }
                line.startsWith("</tls-auth>", ignoreCase = true) -> { inTlsAuth = false; continue }
            }

            if (inCa) { caBuilder.appendLine(rawLine); continue }
            if (inCert) { certBuilder.appendLine(rawLine); continue }
            if (inKey) { keyBuilder.appendLine(rawLine); continue }
            if (inTlsAuth) { tlsAuthBuilder.appendLine(rawLine); continue }

            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) continue

            val tokens = line.split("\\s+".toRegex())
            if (tokens.isEmpty()) continue

            when (tokens[0].lowercase()) {
                "remote" -> {
                    if (tokens.size >= 2) host = tokens[1]
                    if (tokens.size >= 3) port = tokens[2].toIntOrNull() ?: port
                    if (tokens.size >= 4) proto = tokens[3].uppercase()
                }
                "port" -> {
                    if (tokens.size >= 2) port = tokens[1].toIntOrNull() ?: port
                }
                "proto" -> {
                    if (tokens.size >= 2) {
                        proto = if (tokens[1].lowercase().contains("tcp")) "TCP" else "UDP"
                    }
                }
                "dev" -> {
                    if (tokens.size >= 2) dev = tokens[1]
                }
                "cipher" -> {
                    if (tokens.size >= 2) cipher = tokens[1]
                }
                "dhcp-option" -> {
                    if (tokens.size >= 3 && tokens[1].equals("DNS", ignoreCase = true)) {
                        dnsList.add(tokens[2])
                    }
                }
                "route" -> {
                    if (tokens.size >= 3) {
                        routeList.add("${tokens[1]}/${tokens[2]}")
                    }
                }
            }
        }

        if (host.isBlank()) {
            throw IllegalArgumentException("Invalid .ovpn file: missing required 'remote <host> [port]' directive.")
        }

        return ParsedOvpnConfig(
            remoteHost = host,
            remotePort = port,
            protocol = proto,
            dev = dev,
            cipher = cipher,
            caCert = if (caBuilder.isNotEmpty()) caBuilder.toString().trim() else null,
            clientCert = if (certBuilder.isNotEmpty()) certBuilder.toString().trim() else null,
            clientKey = if (keyBuilder.isNotEmpty()) keyBuilder.toString().trim() else null,
            tlsAuth = if (tlsAuthBuilder.isNotEmpty()) tlsAuthBuilder.toString().trim() else null,
            dnsServers = if (dnsList.isNotEmpty()) dnsList else listOf("1.1.1.1", "8.8.8.8"),
            routes = if (routeList.isNotEmpty()) routeList else listOf("0.0.0.0/0"),
            rawConfig = content
        )
    }

    /**
     * Generates a standard, valid .ovpn file from parameters.
     */
    fun generateStandardOvpn(
        remoteHost: String,
        remotePort: Int,
        proto: String = "UDP",
        cipher: String = "AES-256-GCM",
        caCert: String? = null
    ): String {
        val sb = StringBuilder()
        sb.appendLine("client")
        sb.appendLine("dev tun")
        sb.appendLine("proto ${proto.lowercase()}")
        sb.appendLine("remote $remoteHost $remotePort")
        sb.appendLine("resolv-retry infinite")
        sb.appendLine("nobind")
        sb.appendLine("persist-key")
        sb.appendLine("persist-tun")
        sb.appendLine("remote-cert-tls server")
        sb.appendLine("cipher $cipher")
        sb.appendLine("verb 3")

        if (!caCert.isNullOrBlank()) {
            sb.appendLine("<ca>")
            sb.appendLine(caCert.trim())
            sb.appendLine("</ca>")
        }
        return sb.toString()
    }
}
