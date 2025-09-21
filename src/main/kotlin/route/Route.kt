package org.example.route

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.data.storage.Config
import org.example.controller.processKeyRefresh
import org.example.controller.processTransaction

fun Application.module() {

    routing {
        post("/pos") {
            val chance = (0..99).random()
            if (chance in 0..5) {
                return@post
            }
            val packet = call.receive<ByteArray>()

            val response = when (val type = packet[1].toInt() and 0xFF) {
                0x01 -> processTransaction(packet, Config.hmacKey)
                0x02 -> processKeyRefresh()
                else -> {
                    call.respondText("Unknown message type: $type", status = io.ktor.http.HttpStatusCode.BadRequest)
                    return@post
                }
            }

            call.respond(response)
        }
    }
}

