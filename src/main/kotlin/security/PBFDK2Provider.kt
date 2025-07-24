package com.example.security

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PBFDK2Provider {
    private lateinit var algorithm: String
    private var iterations = 0
    private var keyLength = 0
    private var saltLength = 0

    fun initialize(
        algorithm: String,
        iterations: Int,
        keyLength: Int,
        saltLength: Int,
    ) {
        this.algorithm = algorithm
        this.keyLength = keyLength
        this.saltLength = saltLength
        this.iterations = iterations
    }

    fun encrypt(password: String): String {
        val salt = ByteArray(saltLength).apply {
            SecureRandom().nextBytes(this)
        }

        val KeySpec = PBEKeySpec(
            password.toCharArray(),
            salt,
            iterations,
            keyLength
        )

        val factory = SecretKeyFactory.getInstance(algorithm)
        val hash = factory.generateSecret(KeySpec).encoded

        return buildString {
            append(algorithm)
            append(":")
            append(iterations)
            append(":")
            append(Base64.getEncoder().encodeToString(salt))
            append(":")
            append(Base64.getEncoder().encodeToString(hash))
        }
    }

    fun verify(storedHash: String, inputPassword: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 4) return false

        val algorithm = parts[0]
        val iterations = parts[1].toInt()
        val salt = Base64.getDecoder().decode(parts[2])
        val originalHash = Base64.getDecoder().decode(parts[3])

        val KeySpec = PBEKeySpec(
            inputPassword.toCharArray(),
            salt,
            iterations,
            originalHash.size * 8
        )

        val factory = SecretKeyFactory.getInstance(algorithm)
        val hash = factory.generateSecret(KeySpec).encoded

        return originalHash.contentEquals(hash)
    }
}