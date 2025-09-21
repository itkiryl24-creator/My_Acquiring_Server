package org.example.route.response


data class PosResponse(
    val status: String,
    val authCode: String?,
    val timestamp: Long
) : ServerResponse