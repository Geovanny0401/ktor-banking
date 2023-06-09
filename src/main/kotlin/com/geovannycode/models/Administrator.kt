package com.geovannycode.models

import java.util.UUID

data class Administrator(
    val adminId: UUID = UUID.randomUUID(),
    val name: String,
    val password: String,
)
