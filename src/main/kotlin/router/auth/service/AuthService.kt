package com.example.router.auth.service

import com.example.repository.UserRepository
import com.example.repository.UserTokenMapperRepository

class AuthService(
    private val userRepository: UserRepository,
    private val userTokenMapperRepository: UserTokenMapperRepository
) {
}