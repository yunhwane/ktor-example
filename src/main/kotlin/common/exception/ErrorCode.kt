package com.example.common.exception

enum class ErrorCode(
    override val code : Int,
    override var message : String
) : CodeInterface {
    FAILED_TO_FIND_REQUEST_SOURCE(-1, "failed to find request source"),
    FAILED_TO_READ_BODY_REQUEST(-2, "failed to read body"),
    FAILED_TO_ENV(-3, "failed to env"),
    FAILED_TO_QUERY(-4, "failed to query"),
    FAILED_TO_HANDLING_FILE(-5, "failed to handle file"),
    FILE_NOT_FOUND(-6, "file not found"),
    INVALID_REQUEST_FORMAT(-7, "invalid request format"),
    LOGIC_EXCEPTION(-8, "error occurred"),
}