package org.example.data.model

data class Transaction(
    val pan: String,
    val amount: Int,
    val txId: String,
    val merchantId: String,
    val timestamp: Long,
    val suspicious: Boolean = false
)
