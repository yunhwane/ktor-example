package com.example.security

import dev.paseto.jpaseto.Paseto
import dev.paseto.jpaseto.PasetoParser
import dev.paseto.jpaseto.Pasetos
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.Base64
import java.util.UUID

object PasetoProvider {
    private lateinit var publicKey: PublicKey
    private lateinit var privateKey: PrivateKey
    private lateinit var issuer: String


    fun initialize(issuer: String, privateKey: String, publicKey: String) {
        this.issuer = issuer

        val publicKeyBytes = Base64.getDecoder().decode(publicKey)
        val privateKeyBytes = Base64.getDecoder().decode(privateKey)

        this.publicKey = KeyFactory.getInstance("Ed25519").generatePublic(X509EncodedKeySpec(publicKeyBytes))
        this.privateKey = KeyFactory.getInstance("Ed25519").generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))
    }

    fun createToken(userId: String, email : String, roles : List<String> = emptyList()): String {
        val now = Instant.now()

        return Pasetos.V2.PUBLIC.builder()
            .setPrivateKey(this.privateKey)
            .setIssuedAt(now)
            .setIssuer(this.issuer)
            .setSubject(userId)
            .claim("email" , email)
            .claim("roles", roles)
            .setKeyId(UUID.randomUUID().toString())
            .compact()
    }

    fun verifyToken(token: String) : Paseto {
        val processedToken = if(token.startsWith("Bearer ", ignoreCase = true)) {
            token.substring(7)
        } else {
            token
        }

        val parser : PasetoParser = Pasetos.parserBuilder().
        setPublicKey(this.publicKey).
        requireIssuer(this.issuer).
        build()

        return parser.parse(processedToken)
    }

    fun getUserId(token : String) : String {
        return this.verifyToken(token).claims["sub"].toString()
    }
}