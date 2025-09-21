package org.example.controller

import org.example.route.response.PosResponse
import org.example.data.model.Transaction
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun processTransaction(packet: ByteArray, hmacKey: ByteArray): PosResponse {
    val chance = (0..99).random()
    if (chance in 0..3) {
        return PosResponse("DECLINED", null, System.currentTimeMillis())
    }
    val suspicious = packet.contains(0xFF.toByte())

    val body = splitPacket(packet)

    val parts = splitBody(body)

    val aesKey = decryptAesKeyWithRsa(parts.encryptedAesKey)

    val tlvData = decryptAesGcm(aesKey, parts.iv, parts.encryptedTLV)

    if (!verifyHmac(hmacKey, tlvData, parts.hmac)) {
        return PosResponse(
            status = "DECLINED",
            authCode = null,
            timestamp = System.currentTimeMillis()
        )
    }

    val tlvMap = parseTLV(tlvData)

    val fullTxId = tlvMap[0x30]?.toString(Charsets.UTF_8) ?: "N/A"

    val lastDashIndex = fullTxId.lastIndexOf('-')
    val (cleanTxId, txTimestamp) = if (lastDashIndex != -1) {
        val uuidPart = fullTxId.substring(0, lastDashIndex)
        val tsPart = fullTxId.substring(lastDashIndex + 1)
        val ts = tsPart.toLongOrNull() ?: System.currentTimeMillis()
        uuidPart to ts
    } else {
        fullTxId to System.currentTimeMillis()
    }


    val tx = Transaction(
        pan = tlvMap[0x10]?.toString(Charsets.UTF_8) ?: "N/A",
        amount = ByteBuffer.wrap(tlvMap[0x20]).order(ByteOrder.LITTLE_ENDIAN).int,
        txId = cleanTxId,
        merchantId = tlvMap[0x40]?.toString(Charsets.UTF_8) ?: "N/A",
        timestamp = txTimestamp,
        suspicious = suspicious
    )
    saveTransactionH2(tx)

    return PosResponse(
        status = "APPROVED",
        authCode = generateAuthCode(),
        timestamp = System.currentTimeMillis()
    )
}
