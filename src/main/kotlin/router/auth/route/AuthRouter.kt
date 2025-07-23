package com.example.router.auth.route

import com.example.common.validation.postAPI
import com.example.router.auth.types.CreateNewAccountRequest
import io.ktor.server.routing.Route
import io.ktor.server.routing.route


fun Route.AuthRouter() {

    route("/auth") {
        postAPI<CreateNewAccountRequest>("/create") {
            req -> println("Creating new account with email: ${req.email}, password: ${req.password}")
        }
    }

}