package org.example.controller

import org.example.data.storage.Config
import org.example.data.model.Transaction
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.sql.DriverManager
import java.util.*
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

fun splitPacket(packet: ByteArray):  ByteArray {
    require(packet.size >= 4) { "Packet too short" }
    val body = packet.copyOfRange(4, packet.size)
    return body
}

fun splitBody(body: ByteArray): BodyParts {
    val encryptedAesKey = body.copyOfRange(0, 256)
    val iv = body.copyOfRange(256, 268)
    val hmac = body.copyOfRange(268, 300)
    val encryptedTLV = body.copyOfRange(300, body.size)
    return BodyParts(encryptedAesKey, iv, hmac, encryptedTLV)
}

private fun getPrivateKeyFromBase64(): PrivateKey {
    val keyBytes = Base64.getDecoder().decode(Config.privateKey.replace("\\s".toRegex(), ""))
    val keySpec = PKCS8EncodedKeySpec(keyBytes)
    return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
}

fun decryptAesKeyWithRsa(encryptedKey: ByteArray): SecretKey {
    val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
    val oaepParams = OAEPParameterSpec(
        "SHA-256",
        "MGF1",
        MGF1ParameterSpec.SHA256,
        PSource.PSpecified.DEFAULT
    )
    cipher.init(Cipher.DECRYPT_MODE, getPrivateKeyFromBase64(),oaepParams)
    val keyBytes = cipher.doFinal(encryptedKey)
    return SecretKeySpec(keyBytes, "AES")
}

fun decryptAesGcm(key: SecretKey, iv: ByteArray, encryptedData: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val spec = GCMParameterSpec(128, iv)
    cipher.init(Cipher.DECRYPT_MODE, key, spec)
    return cipher.doFinal(encryptedData)
}

fun verifyHmac(hmacKey: ByteArray, data: ByteArray, receivedHmac: ByteArray): Boolean {
    val mac = Mac.getInstance("HmacSHA256")
    val keySpec = SecretKeySpec(hmacKey, "HmacSHA256")
    mac.init(keySpec)
    val expectedHmac = mac.doFinal(data)
    return expectedHmac.contentEquals(receivedHmac)
}

fun generateAuthCode(): String {
    return (100_000..999_999).random().toString()
}

fun parseTLV(data: ByteArray): Map<Int, ByteArray> {
    val result = mutableMapOf<Int, ByteArray>()
    var index = 0

    while (index + 3 <= data.size) {
        val tag = data[index].toInt() and 0xFF
        index += 1

        val length = ByteBuffer.wrap(data, index, 2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .short
            .toInt()
        index += 2

        if (index + length > data.size) break

        val value = data.copyOfRange(index, index + length)
        index += length

        result[tag] = value
    }

    return result
}




fun saveTransactionH2(tx: Transaction) {
    val connection = DriverManager.getConnection("jdbc:h2:./posdb", "sa", "123")
    connection.createStatement().use { stmt ->
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS transactions (
                pan VARCHAR(20),
                amount INT,
                txId VARCHAR(50),
                merchantId VARCHAR(50),
                timestamp BIGINT,
                suspicious BOOLEAN DEFAULT FALSE
            )
        """.trimIndent())

        val prep = connection.prepareStatement("""
            INSERT INTO transactions(pan, amount, txId, merchantId, timestamp,suspicious)
            VALUES (?, ?, ?, ?, ?,?)
        """.trimIndent())

        prep.setString(1, tx.pan)
        prep.setInt(2, tx.amount)
        prep.setString(3, tx.txId)
        prep.setString(4, tx.merchantId)
        prep.setLong(5, tx.timestamp)
        prep.setBoolean(6,tx.suspicious)
        prep.executeUpdate()
    }
    connection.close()
}