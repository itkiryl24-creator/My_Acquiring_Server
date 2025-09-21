package org.example.data.storage

import java.io.File
import java.util.*

object Config {

     private var props = Properties()

    init {
        // путь к файлу с ключами
        val file = File("src/main/kotlin/data/storage/keys.properties")
        props.load(file.inputStream())
    }

    val privateKey: String
        get() {
            return props.getProperty("PRIVATE_KEY").replace("\\s".toRegex(), "")
        }

    val hmacKey: ByteArray
        get() {
            val base64 = props.getProperty("HMAC_KEY")
                ?.replace("\\s".toRegex(), "")
                ?: throw IllegalStateException("HMAC_KEY not found")
            return Base64.getDecoder().decode(base64)
        }

    fun updateProperty(newPros: Properties){
        props = newPros
    }
}