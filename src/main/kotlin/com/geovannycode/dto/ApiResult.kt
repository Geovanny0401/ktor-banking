package com.geovannycode.dto

sealed class ApiResult<out T> {
    internal data class Failure(val errorCode: ErrorCode, val errorMessage: String) : ApiResult<Nothing>()
    internal data class Success<T>(val value: T) : ApiResult<T>()
}

enum class ErrorCode {
    DATABASE_ERROR,
    MAPPING_ERROR,
    USER_NOT_FOUND,
    PASSWORD_ERROR,
    ACCOUNT_ALREADY_EXIST
}