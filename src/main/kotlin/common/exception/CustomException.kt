package com.example.common.exception

import org.slf4j.LoggerFactory

interface CodeInterface {
    val code: Int
    var message : String
}

class CustomException(
    private val codeInterface: CodeInterface,
    private val additionMessage: String? = null
) : RuntimeException(
    if (additionMessage == null) {
        codeInterface.message
    } else {
        "${codeInterface.message} - $additionMessage"
    }
) {
    private val logger = LoggerFactory.getLogger(CustomException::class.java)

    init {
        logger.error("Exception Created With Code: ${codeInterface.code} Message: ${codeInterface.message}")
    }

    fun getCodeInterface(): CodeInterface {
        val codeInterface = codeInterface

        if (additionMessage!=null) {
            codeInterface.message += additionMessage.toString()
        }

        return codeInterface
    }
}