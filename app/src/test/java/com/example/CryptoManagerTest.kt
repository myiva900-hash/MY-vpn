package com.example

import com.example.crypto.CryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoManagerTest {

    @Test
    fun encryptAndDecryptWithCorrectPassword() {
        val originalText = "client\ndev tun\nremote us1.mutigandavpn.com 1194"
        val password = "StrongPassword@2026"

        val encryptedBytes = CryptoManager.encryptMyVpnProfile(originalText, password)
        assertTrue(CryptoManager.isMyVpnEncrypted(encryptedBytes))

        val decryptedText = CryptoManager.decryptMyVpnProfile(encryptedBytes, password)
        assertEquals(originalText, decryptedText)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectDecryptionWithWrongPassword() {
        val originalText = "client\ndev tun\nremote de1.mutigandavpn.com 1194"
        val password = "CorrectPassword123"
        val wrongPassword = "WrongPassword456"

        val encryptedBytes = CryptoManager.encryptMyVpnProfile(originalText, password)
        CryptoManager.decryptMyVpnProfile(encryptedBytes, wrongPassword)
    }

    @Test
    fun isMyVpnEncryptedDetectsFormat() {
        val validHeader = "MYVPN1".toByteArray() + ByteArray(20)
        assertTrue(CryptoManager.isMyVpnEncrypted(validHeader))

        val plainOvpn = "client\ndev tun".toByteArray()
        assertFalse(CryptoManager.isMyVpnEncrypted(plainOvpn))
    }
}
