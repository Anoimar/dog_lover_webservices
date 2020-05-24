package com.thernat

import com.thernat.service.UserService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import org.apache.http.HttpException

val userService = UserService()


fun Routing.uploadAvatar() {
    post("/upload_avatar") {
        call.respond(userService.uploadUserAvatar(call.receiveMultipart()))
    }
}

fun Routing.getUserData() {
    get("/user"){
        call.parameters["userId"]?.let {
            call.respond(userService.getUserData(it))
        } ?: call.respond(HttpStatusCode.BadRequest,"Missing param")

    }
}