package com.example

import com.example.vpn.OvpnParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OvpnParserTest {

    @Test
    fun parseValidOvpnConfig() {
        val ovpnContent = """
            client
            dev tun
            proto udp
            remote fr1.mutigandavpn.com 1194
            resolv-retry infinite
            nobind
            persist-key
            persist-tun
            cipher AES-256-GCM
            dhcp-option DNS 1.1.1.1
            dhcp-option DNS 8.8.8.8
            verb 3
            <ca>
            -----BEGIN CERTIFICATE-----
            MIIB...SAMPLE...CA
            -----END CERTIFICATE-----
            </ca>
        """.trimIndent()

        val parsed = OvpnParser.parse(ovpnContent)
        assertEquals("fr1.mutigandavpn.com", parsed.remoteHost)
        assertEquals(1194, parsed.remotePort)
        assertEquals("UDP", parsed.protocol)
        assertEquals("AES-256-GCM", parsed.cipher)
        assertTrue(parsed.dnsServers.contains("1.1.1.1"))
        assertNotNull(parsed.caCert)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectInvalidOvpnWithoutRemote() {
        val invalidContent = """
            client
            dev tun
            proto udp
            cipher AES-256-GCM
        """.trimIndent()

        OvpnParser.parse(invalidContent)
    }
}
