package com.example.router.auth.route

import com.example.common.validation.postAPI
import com.example.router.auth.service.AuthService
import com.example.router.auth.types.CreateNewAccountRequest
import com.example.router.auth.types.LoginRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import org.koin.ktor.ext.get


fun Route.AuthRouter() {

    val authService = get<AuthService>()

    route("/auth") {
        postAPI<CreateNewAccountRequest>("/create") { req ->
            val res = authService.createAccount(req.email, req.password)
            call.respond(HttpStatusCode.OK, res)
        }

        postAPI<LoginRequest>("/login") { req ->
            val res =  authService.login(req.email, req.password)
            call.respond(HttpStatusCode.OK, res)
        }
    }

}