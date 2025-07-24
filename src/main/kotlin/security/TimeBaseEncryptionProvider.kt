package com.example.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class TimeLockedData(
    val encryptionContent : String, // 데이터 암호화가 진행되는 값
    val encryptionKey : String, // 마스터 키 + 시간 기반의 키 조합
    val releaseTime : Int, // 가능시간
    val timeSalt: String // salt
)

object TimeBaseEncryptionProvider {
    private const val GCM_IV_LENGTH = 12 // AES-GCM에 사용되는 표준 길이
    private const val GCM_TAG_LENGTH = 16 // GCM 인증 태그의 길이
    private val secureRandom = SecureRandom()

    private lateinit var masterKey: ByteArray

    fun initialize(masterKey: ByteArray) {
        this.masterKey = masterKey
    }

    fun encrypt(content: String, releaseTime: Int): TimeLockedData {
        val dataKey = ByteArray(32)
        secureRandom.nextBytes(dataKey)

        val timeSalt = ByteArray(16)
        secureRandom.nextBytes(timeSalt)

        val timeKey = generateTimeKey(releaseTime, timeSalt)

        val combinedKey = combineKeys(masterKey, timeKey)

        val encryptedDataKey = encryptAES(dataKey, combinedKey)

        val encryptedData = encryptAES(content.toByteArray(Charsets.UTF_8), dataKey)
        val now = (System.currentTimeMillis() / 1000).toInt()

        return TimeLockedData(
            encryptionContent = bytesToBase64(encryptedData),
            encryptionKey = bytesToBase64(encryptedDataKey),
            releaseTime = now,
            timeSalt = bytesToBase64(timeSalt)
        )
    }

    fun decrypt(data: TimeLockedData): String {
        try {
            val timeKey = generateTimeKey(data.releaseTime, base64ToBytes(data.timeSalt))

            val combinedKey = combineKeys(masterKey, timeKey)

            val encryptedKey = base64ToBytes(data.encryptionKey)
            val dataKey = decryptAES(encryptedKey, combinedKey)

            val contentBytes = base64ToBytes(data.encryptionContent)
            val content = decryptAES(contentBytes, dataKey)

            return String(content, Charsets.UTF_8)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to decrypt encrypted data ${e.message}")
        }
    }

    private fun encryptAES(data: ByteArray, key: ByteArray): ByteArray {
        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)

        val ciper = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(key, "AES")
        val paramSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        ciper.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec)

        val encryptionData = ciper.doFinal(data)

        val result = ByteArray(iv.size + encryptionData.size)
        System.arraycopy(iv, 0, result, 0, iv.size)
        System.arraycopy(encryptionData, 0, result, iv.size, encryptionData.size)

        return result
    }

    private fun decryptAES(encryptedData: ByteArray, key: ByteArray): ByteArray {
        val iv = encryptedData.copyOfRange(0, GCM_IV_LENGTH)
        val cipherText = encryptedData.copyOfRange(GCM_IV_LENGTH, encryptedData.size)

        val ciper = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(key, "AES")
        val paramSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        ciper.init(Cipher.DECRYPT_MODE, secretKey, paramSpec)

        return ciper.doFinal(cipherText)
    }

    private fun bytesToBase64(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun base64ToBytes(base64: String): ByteArray {
        return Base64.getDecoder().decode(base64)
    }

    private fun generateTimeKey(releaseTime: Int, salt: ByteArray): ByteArray {
        val timeBytes = releaseTime.toString().toByteArray(Charsets.UTF_8)

        val md = MessageDigest.getInstance("SHA-256")
        md.update(timeBytes)
        md.update(salt)

        return md.digest()
    }

    private fun combineKeys(ke1: ByteArray, ke2: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")

        md.update(ke1)
        md.update(ke2)

        return md.digest()
    }
}