package com.thernat

import com.thernat.service.DogService
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get

val dogService = DogService()

fun Routing.getDogs(){
    get ("/dogs") {
        call.respond(dogService.getAllDogs())
    }
}