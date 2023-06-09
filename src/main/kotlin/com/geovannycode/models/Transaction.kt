package com.geovannycode.models

import java.time.LocalDateTime
import java.util.UUID

data class Transaction(
    val transactionId: UUID = UUID.randomUUID(),
    val origin: Account,
    val target: Account,
    val amount: Double,
    val created: LocalDateTime = LocalDateTime.now(),
)
