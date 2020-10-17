package com.thernat

import com.thernat.repository.model.Dog
import com.thernat.repository.model.UpdateDogRequest
import com.thernat.service.DogService
import com.thernat.service.exception.UserNotFoundException
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.*
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post

val dogService = DogService()


fun Routing.uploadDog() {
    post("/upload-dog") {
        call.respond(dogService.uploadDogPics(call.receiveMultipart()))
    }
}

fun Routing.getMyDogs() {
    get("/my-dogs") {
        try {
            call.request.queryParameters["userId"]?.let {
                call.respond(dogService.getMyDogs(it))
            } ?: call.respond(HttpStatusCode.BadRequest, "Missing param")
        } catch (e: UserNotFoundException) {
            call.respond(HttpStatusCode.NotFound, "User doesn't exist")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, e)
        }
    }
}

fun Routing.getOtherDogs() {
    get("/other-dogs") {
        try {
            call.request.queryParameters["userId"]?.let {
                call.respond(dogService.getOtherDogs(it))
            } ?: call.respond(HttpStatusCode.BadRequest, "Missing param")
        } catch (e: UserNotFoundException) {
            call.respond(HttpStatusCode.NotFound, "User doesn't exist")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, e)
        }
    }
}


fun Routing.deleteMyDog() {
    get("/my-dogs-delete") {
        try {
            val userId = call.request.queryParameters["userId"]
            val dogId = call.request.queryParameters["dogId"]
            userId?.let { owner ->
                dogId?.toIntOrNull()?.let {
                    call.respond(dogService.deleteDog(owner, it))
                }
            } ?: call.respond(HttpStatusCode.BadRequest, "Missing param")
        } catch (e: UserNotFoundException) {
            call.respond(HttpStatusCode.NotFound, "User doesn't exist")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, e)
        }
    }
}

fun Routing.updateDog() {
    post("/my-dogs-update") {
        try {
            val request = call.receive<UpdateDogRequest>()
            if (dogService.updateDog(request.userId, request.dog)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, e)
        }
    }
}