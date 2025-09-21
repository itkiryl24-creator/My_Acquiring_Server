package org.example.controller

data class BodyParts(
    val encryptedAesKey: ByteArray,
    val iv: ByteArray,
    val hmac: ByteArray,
    val encryptedTLV: ByteArray
)
