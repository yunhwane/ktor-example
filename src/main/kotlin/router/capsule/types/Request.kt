package com.example.router.capsule.types

import com.example.common.validation.Request
import com.example.common.validation.RequestInfo
import com.example.common.validation.RequestSource
import com.example.types.storage.ContentType
import com.fasterxml.jackson.annotation.JsonProperty

@Request
data class CreateCapsuleRequest(
    @RequestInfo(name = "contentType", source = RequestSource.BODY)
    val contentType: ContentType = ContentType.image,

    @RequestInfo(name = "content", source = RequestSource.BODY, required = true)
    val content: String,

    @RequestInfo(name = "title", source = RequestSource.BODY, required = true)
    val title : String,

    @RequestInfo(name = "description", source = RequestSource.BODY, required = true)
    val description : String,

    @RequestInfo(name = "recipients", source = RequestSource.BODY, required = true)
    val recipients : String,

    @JsonProperty("openDate")
    @RequestInfo(name = "openDate", source = RequestSource.BODY, required = true)
    val scheduledOpenDate : Int
)

@Request
data class CapsuleDetailRequest(
    @RequestInfo(name = "capsuleId", source = RequestSource.PATH, required = true)
    val capsuleId : String,
)

@Request
data class OpenCapsuleRequest(
    @RequestInfo(name = "capsuleId", source = RequestSource.BODY, required = true)
    val capsuleId : String,
)