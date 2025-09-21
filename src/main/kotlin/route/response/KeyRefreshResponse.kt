package org.example.route.response

data class KeyRefreshResponse(
    val hmacKey: String,
) : ServerResponse
