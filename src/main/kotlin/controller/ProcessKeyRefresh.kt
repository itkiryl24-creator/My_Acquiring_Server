package org.example.controller

import org.example.route.response.KeyRefreshResponse
import org.example.data.storage.Config
import java.io.File
import java.security.SecureRandom
import java.util.*

fun processKeyRefresh(): KeyRefreshResponse {

    val newKey = ByteArray(32)
    SecureRandom().nextBytes(newKey)
    val hmacKeyBase64 = Base64.getEncoder().encodeToString(newKey)

    val file = File("src/main/kotlin/data/storage/keys.properties")
    val props = Properties()

    if (file.exists()) {
        file.inputStream().use { props.load(it) }
    }

    props.setProperty("HMAC_KEY", hmacKeyBase64)
    Config.updateProperty(props)

    file.outputStream().use { props.store(it, "Server keys updated") }

    return KeyRefreshResponse(
        hmacKey = hmacKeyBase64,
    )
}