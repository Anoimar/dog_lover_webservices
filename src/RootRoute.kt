package com.thernat

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.root(){
    get ("/") {
        call.respondText {"Home" }
    }
}
