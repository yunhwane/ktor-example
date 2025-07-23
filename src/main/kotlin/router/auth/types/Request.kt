package com.example.router.auth.types

import com.example.common.validation.Request
import com.example.common.validation.RequestInfo
import com.example.common.validation.RequestSource


@Request
data class CreateNewAccountRequest(

    @RequestInfo(name = "email", source = RequestSource.BODY, required = true)
    val email: String,

    @RequestInfo(name = "password", source = RequestSource.BODY)
    val password: String
)