package com.geovannycode.models

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

private const val MINIMUM_PASSWORD_LENGTH = 16
private const val MINIMUM_AGE_YEARS = 18L
private const val PASSWORD_REGEX_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$"

data class User(
    val userId: UUID = UUID.randomUUID(),
    val firstName: String,
    val lastName: String,
    val birthdate: LocalDate,
    val password: String,
    val created: LocalDateTime = LocalDateTime.now(),
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    val accounts: List<Account> = emptyList(),
) {
    init {
        val requiredDate = LocalDate.now().minusYears(MINIMUM_AGE_YEARS)
        require(birthdate.isBefore(requiredDate)) {
            "Birthdate must be before '$requiredDate'."
        }
        require(password.length >= MINIMUM_PASSWORD_LENGTH) {
            "Password must be at minimum '$MINIMUM_PASSWORD_LENGTH' characters."
        }
        require(password.matches(Regex(PASSWORD_REGEX_PATTERN))) {
            "Password must contain at minimum one lowercase, one uppercase, one special character and one digit."
        }
    }
}
