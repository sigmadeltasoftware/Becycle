package be.sigmadelta.common.util

import io.ktor.client.features.*
import io.ktor.client.statement.*


class AuthorizationKeyExpiredException(response: HttpResponse): ResponseException(response)

class InvalidAddressException(response: HttpResponse): ResponseException(response)