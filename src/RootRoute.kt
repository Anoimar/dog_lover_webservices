package com.thernat

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post

fun Routing.root(){
    get ("/") {
        call.respondText {"Home" }
    }
}
