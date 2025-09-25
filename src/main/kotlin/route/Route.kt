package org.example.route

import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.data.storage.Config
import org.example.controller.processKeyRefresh
import org.example.controller.processTransaction
import org.example.route.response.KeyRefreshResponse
import org.example.route.response.PosResponse
import java.util.concurrent.atomic.AtomicInteger

val  activeConnections = AtomicInteger(0)
fun Application.module() {

    routing {
        post("/pos") {
            val currentConnections = activeConnections.incrementAndGet()
            println("New connection! Active connections: $currentConnections")

            val chance = (0..99).random()
            if (chance in 0..5) {
                return@post
            }
            val packet = call.receive<ByteArray>()

            println("Incoming /pos request from ${call.request.origin.remoteHost}:${call.request.origin.remotePort}")
            println("Packet size: ${packet.size} bytes")
            println(hexdump(packet))

            val response = when (val type = packet[1].toInt() and 0xFF) {
                0x01 -> processTransaction(packet, Config.hmacKey)
                0x02 -> processKeyRefresh()
                else -> {
                    call.respondText("Unknown message type: $type", status = io.ktor.http.HttpStatusCode.BadRequest)
                    return@post
                }
            }

            when (response){
                is KeyRefreshResponse -> {
                    println("Response:")
                    println(response.toString())
                }
                is PosResponse -> {
                    println("Response:")
                    println(response.toString())
                }
            }

            call.respond(response)
            val remaining = activeConnections.decrementAndGet()
            println("Connection closed. Active connections: $remaining")
        }
    }

}

fun hexdump(bytes: ByteArray): String {
    val sb = StringBuilder()
    for (i in bytes.indices step 16) {
        val slice = bytes.slice(i until minOf(i + 16, bytes.size))
        sb.append(String.format("%04X: ", i))
        sb.append(slice.joinToString(" ") { String.format("%02X", it) })
        sb.append(" ")
        sb.append(slice.map { if (it in 32..126) it.toInt().toChar() else '.' }.joinToString(""))
        sb.append("\n")
    }
    return sb.toString()
}

