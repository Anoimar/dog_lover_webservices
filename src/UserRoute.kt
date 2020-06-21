package com.thernat

import com.thernat.service.UserService
import com.thernat.service.exception.UserNotFoundException
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import java.lang.RuntimeException
import java.rmi.RemoteException

val userService = UserService()


fun Routing.uploadAvatar() {
    post("/upload_avatar") {
        call.respond(userService.uploadUserAvatar(call.receiveMultipart()))
    }
}

fun Routing.getUserData() {
    get("/user"){
        try {
            call.request.queryParameters["userId"]?.let {
                print(userService.getUserData(it))
                call.respond(userService.getUserData(it))
            } ?: call.respond(HttpStatusCode.BadRequest,"Missing param")
        } catch (e: UserNotFoundException) {
            call.respond(HttpStatusCode.NotFound,"User doesn't exist")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, e)
        }
    }
}