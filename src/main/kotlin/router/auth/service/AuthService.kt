package com.example.router.auth.service

import com.example.common.transactional.TransactionProvider
import com.example.repository.UserRepository
import com.example.repository.UserTokenMapperRepository
import com.example.security.PBFDK2Provider
import com.example.security.PasetoProvider
import com.example.types.GlobalResponse
import com.example.types.GlobalResponseProvider
import com.example.types.storage.UserStorage

class AuthService(
    private val userRepository: UserRepository,
    private val userTokenMapperRepository: UserTokenMapperRepository
) {

    suspend fun createAccount(email: String, password : String) : GlobalResponse<String> {
        val hashedPassword = PBFDK2Provider.encrypt(password)

        return TransactionProvider.transaction {
            val userInfo = userRepository.findByEmail(email)

            if (userInfo != null) {
                return@transaction GlobalResponseProvider.new(1, "failed", "already exists")
            } else {
                val userId = userRepository.create(email, hashedPassword)
                val token = PasetoProvider.createToken(userId, email)

                TransactionProvider.transaction {
                    userTokenMapperRepository.createPasetoToken(userId, token)
                }

                return@transaction GlobalResponseProvider.new(1, "SUCESS", token)
            }
        }
    }

    suspend fun login(email: String, password: String) : GlobalResponse<String> {
        var userInfo : UserStorage? = null

        TransactionProvider.transaction {
            userInfo = userRepository.findByEmail(email)
        }

        if (userInfo == null) {
            return GlobalResponseProvider.new(1, "not exists", null)
        } else {

            val verify = PBFDK2Provider.verify(userInfo!!.passwordHash, password)

            if (!verify) {
                return GlobalResponseProvider.new(1, "password in correct", null)
            }

            val token = PasetoProvider.createToken(email, password)

            TransactionProvider.transaction {
                userTokenMapperRepository.createPasetoToken(userInfo!!.id, token)
            }

            return GlobalResponseProvider.new(1, "SUCESS", token)
        }
    }

}