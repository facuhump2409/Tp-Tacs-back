package com.grupox.wololo.controllers

import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.getOrThrow
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

abstract class BaseController {
    @ExceptionHandler(CustomException.NotFound::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(exception: CustomException) = exception.dto()

    @ExceptionHandler(CustomException.Forbidden::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleForbiddenException(exception: CustomException) = exception.dto()

    @ExceptionHandler(CustomException.BadRequest::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequestException(exception: CustomException) = exception.dto()

    @ExceptionHandler(CustomException.Unauthorized::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorizedException(exception: CustomException) = exception.dto()

    @ExceptionHandler(CustomException.Service::class)
    @ResponseStatus(HttpStatus.FAILED_DEPENDENCY) // Revisar si es correcto esta http status
    fun handleServiceException(exception: CustomException) = exception.dto()

    fun checkAndGetToken(authCookie: String?): Jws<Claims> = JwtSigner.validateJwt(authCookie.toOption()).getOrThrow()
}