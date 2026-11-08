package com.example.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * MUTIGANDA YVAN VPN — Encrypted Configuration Engine
 *
 * Implements:
 * 1. AES-256-GCM authenticated encryption for .myvpn configuration backups.
 * 2. PBKDF2WithHmacSHA256 key derivation with 65,536 iterations and 16-byte cryptographically secure random salt.
 * 3. Android Keystore integration for securing local device-stored certificates and VPS configurations.
 * 4. Configuration format validation and tamper detection.
 */
object CryptoManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEYSTORE_ALIAS = "mutiganda_vpn_keystore_alias"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val PBKDF2_SALT_LENGTH_BYTES = 16
    private const val PBKDF2_ITERATIONS = 65536
    private const val AES_KEY_SIZE_BITS = 256

    // Magic header for MUTIGANDA YVAN VPN Encrypted Profile
    val MYVPN_MAGIC_HEADER = "MYVPN1".toByteArray(StandardCharsets.US_ASCII)

    private val secureRandom = SecureRandom()

    // ==========================================
    // 1. ANDROID KEYSTORE (Device-Side Security)
    // ==========================================

    private fun getOrCreateKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val entry = keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(AES_KEY_SIZE_BITS)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypts sensitive text (e.g. certificates, credentials) using the Android Keystore hardware-backed key.
     */
    fun encryptWithKeystore(plainText: String): String {
        val secretKey = getOrCreateKeystoreKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

        // Prepend IV (12 bytes)
        val byteBuffer = ByteBuffer.allocate(iv.size + cipherBytes.size)
        byteBuffer.put(iv)
        byteBuffer.put(cipherBytes)
        return android.util.Base64.encodeToString(byteBuffer.array(), android.util.Base64.NO_WRAP)
    }

    /**
     * Decrypts text using the Android Keystore hardware-backed key.
     */
    fun decryptWithKeystore(cipherBase64: String): String {
        val combined = android.util.Base64.decode(cipherBase64, android.util.Base64.NO_WRAP)
        val byteBuffer = ByteBuffer.wrap(combined)
        val iv = ByteArray(GCM_IV_LENGTH_BYTES)
        byteBuffer.get(iv)
        val cipherBytes = ByteArray(byteBuffer.remaining())
        byteBuffer.get(cipherBytes)

        val secretKey = getOrCreateKeystoreKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, StandardCharsets.UTF_8)
    }

    // =============================================================
    // 2. PASSWORD-PROTECTED .myvpn BACKUPS (AES-256-GCM + PBKDF2)
    // =============================================================

    /**
     * Encrypts an OpenVPN configuration into a password-protected .myvpn package.
     * File Structure:
     * [6 bytes: "MYVPN1"] [16 bytes: Salt] [12 bytes: IV] [Ciphertext + GCM Tag]
     */
    fun encryptMyVpnProfile(plainContent: String, password: String): ByteArray {
        require(password.isNotBlank()) { "Encryption password cannot be blank" }

        val salt = ByteArray(PBKDF2_SALT_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(GCM_IV_LENGTH_BYTES).also { secureRandom.nextBytes(it) }

        val derivedKey = deriveKeyFromPassword(password, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, derivedKey, gcmSpec)

        val plainBytes = plainContent.toByteArray(StandardCharsets.UTF_8)
        val cipherBytes = cipher.doFinal(plainBytes)

        val outputStream = ByteArrayOutputStream()
        outputStream.write(MYVPN_MAGIC_HEADER)
        outputStream.write(salt)
        outputStream.write(iv)
        outputStream.write(cipherBytes)
        return outputStream.toByteArray()
    }

    /**
     * Decrypts a password-protected .myvpn configuration.
     * Validates magic header, salt, IV, and verifies GCM authentication tag.
     */
    fun decryptMyVpnProfile(encryptedBytes: ByteArray, password: String): String {
        if (!isMyVpnEncrypted(encryptedBytes)) {
            throw IllegalArgumentException("Invalid file format. Not a recognized MUTIGANDA YVAN VPN (.myvpn) encrypted file.")
        }

        val minLength = MYVPN_MAGIC_HEADER.size + PBKDF2_SALT_LENGTH_BYTES + GCM_IV_LENGTH_BYTES + 16
        if (encryptedBytes.size < minLength) {
            throw IllegalArgumentException("Corrupted configuration file: payload too short.")
        }

        val buffer = ByteBuffer.wrap(encryptedBytes)
        // Skip header
        val header = ByteArray(MYVPN_MAGIC_HEADER.size)
        buffer.get(header)

        val salt = ByteArray(PBKDF2_SALT_LENGTH_BYTES)
        buffer.get(salt)

        val iv = ByteArray(GCM_IV_LENGTH_BYTES)
        buffer.get(iv)

        val cipherBytes = ByteArray(buffer.remaining())
        buffer.get(cipherBytes)

        val derivedKey = deriveKeyFromPassword(password, salt)

        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, derivedKey, gcmSpec)
            val decryptedBytes = cipher.doFinal(cipherBytes)
            return String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to decrypt configuration. Incorrect password or file has been tampered with.")
        }
    }

    /**
     * Checks if a byte stream starts with the .myvpn magic header.
     */
    fun isMyVpnEncrypted(bytes: ByteArray): Boolean {
        if (bytes.size < MYVPN_MAGIC_HEADER.size) return false
        for (i in MYVPN_MAGIC_HEADER.indices) {
            if (bytes[i] != MYVPN_MAGIC_HEADER[i]) return false
        }
        return true
    }

    private fun deriveKeyFromPassword(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_SIZE_BITS)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
}
