package com.example.router.capsule.route

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.common.file.FileHandler
import com.example.common.json.JsonHandler
import com.example.common.validation.getAPI
import com.example.common.validation.postAPI
import com.example.router.capsule.service.CapsuleService
import com.example.router.capsule.types.CapsuleDetailRequest
import com.example.router.capsule.types.CreateCapsuleRequest
import com.example.router.capsule.types.OpenCapsuleRequest
import com.example.security.Intercept
import com.example.security.PasetoProvider
import com.example.types.GlobalResponseProvider
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.get

fun Route.CapsuleRoute() {
    val service = get<CapsuleService>()

    route("/capsule") {
        intercept(ApplicationCallPipeline.Call, Intercept.tokenHeaderVerify())

        post ("/create/with-file") {
            val multiPart = call.receiveMultipart()
            val (file, jsonData) = FileHandler.exportFileData(multiPart)

            val req = JsonHandler.decodeFromJson(jsonData, CreateCapsuleRequest::class.java)
            val (fileName, fileData) = FileHandler.handlingIncomingFile(file)

            val token = call.request.header(io.ktor.http.HttpHeaders.Authorization).toString()
            val userID = PasetoProvider.getUserId(token)

            val response  = service.fileContent(
                userID, req.contentType, req.recipients,
                req.title, req.description, req.content,
                req.scheduledOpenDate, fileData, fileName
            )

            when (response.code) {
                0 -> {
                    call.respond(HttpStatusCode.OK, response)
                }
                else -> {
                    call.respond(HttpStatusCode.InternalServerError, response)
                }
            }
        }

        postAPI<CreateCapsuleRequest>("/create") { req->
            try {
                val exist = service.verifyEmailExist(req.recipients)
                if (!exist) {
                    call.respond(HttpStatusCode.BadRequest, GlobalResponseProvider.new(-1, "not exist email : ${req.recipients}", null))
                    return@postAPI
                }
            } catch (e : Exception) {
                throw CustomException(ErrorCode.LOGIC_EXCEPTION, e.message.toString())
            }

            if (req.content.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, GlobalResponseProvider.new(-1, "empty content", null))
                return@postAPI
            }

            val token = call.request.header(io.ktor.http.HttpHeaders.Authorization).toString()
            val userID = PasetoProvider.getUserId(token)

            // TOOD -> 현재 시간보다 커야 한다는 조건이 들어가면 좋다 (req.scheduledOpenDate)

            val response = service.textContent(
                userID, req.recipients, req.title,
                req.description, req.content, req.scheduledOpenDate,
            )

            when (response.code) {
                0 -> {
                    call.respond(HttpStatusCode.OK, response)
                }
                else -> {
                    call.respond(HttpStatusCode.InternalServerError, response)
                }
            }
        }

        getAPI<CapsuleDetailRequest>("/capsule/{capsuleId}") { req->
            val response = service.capsuleContentById(req.capsuleId)
            when (response.code) {
                0 -> {
                    call.respond(HttpStatusCode.OK, response)
                }
                else -> {
                    call.respond(HttpStatusCode.InternalServerError, response)
                }
            }
        }

        postAPI<OpenCapsuleRequest>("/open-capsule") { req->
            val response = service.openCapsuleContent(req.capsuleId)
            when (response.code) {
                0 -> {
                    call.respond(HttpStatusCode.OK, response)
                }
                else -> {
                    call.respond(HttpStatusCode.InternalServerError, response)
                }
            }
        }
    }
}