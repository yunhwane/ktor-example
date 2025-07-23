package com.example.common.validation

import com.example.types.GlobalResponseProvider
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import java.lang.Exception


suspend inline fun <reified T : Any> PipelineContext<Unit, ApplicationCall>.handlingRequest(
    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit,
    source : RequestSource
) {
    val result = if (source == RequestSource.BODY) {
        RequestBinder.post<T>(call)
    } else {
        RequestBinder.get<T>(call)
    }

    when(result) {
        is RequestBinder.BindResult.Success -> {
            try {
                handler(result.data)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    GlobalResponseProvider.new<Any>(-1, e.message ?: "Unknown error", null)
                )
            }
        }
        is RequestBinder.BindResult.Error -> {
            call.respond(HttpStatusCode.BadRequest, GlobalResponseProvider.new<Any>(-1, result.error, null))
        }
    }
}

inline fun <reified T : Any> Route.postAPI(
    path : String,
    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
) {
    post(path) {
        handlingRequest(handler, RequestSource.BODY)
    }
}

inline fun <reified T : Any> Route.getAPI(
    path : String,
    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
) {
    get(path) {
        handlingRequest(handler, RequestSource.QUERY)
    }
}