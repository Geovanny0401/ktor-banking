package com.geovannycode.service

import com.geovannycode.dto.AccountDto
import com.geovannycode.dto.ApiResult
import com.geovannycode.dto.ErrorCode
import com.geovannycode.exception.InvalidInputException
import com.geovannycode.models.Account
import com.geovannycode.repository.AccountRepository
import com.geovannycode.repository.UserRepository
import org.slf4j.LoggerFactory
import java.util.UUID
import org.slf4j.Logger

class DefaultAccountService(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
) : AccountService{
    private val logger: Logger = LoggerFactory.getLogger(AccountService::class.java)
    override fun createAccount(userId: UUID, accountDto: AccountDto): ApiResult<UUID> {
        logger.info("Start creation of account '$accountDto' for user with userId '$userId'.")
        val user = userRepository.findByUserId(userId)
        if (user == null) {
            logger.error("User with userId '$userId' not found.")
            return ApiResult.Failure(ErrorCode.USER_NOT_FOUND, "User with userId '$userId' not found.")
        }
        val account = try {
            accountDto.toAccount()
        } catch (e: InvalidInputException) {
            logger.error("Unable to map given dto '$accountDto' to domain object.", e)
            return ApiResult.Failure(
                ErrorCode.MAPPING_ERROR,
                e.getErrorMessage()
            )
        }
        return try {
            val persistedAccount = accountRepository.saveForUser(user = user, account = account)
            logger.info("Successfully created account '$persistedAccount' for user with userId '$userId'.")
            ApiResult.Success(persistedAccount.accountId)

        } catch (e: Exception) {
            logger.error("Unable to create account '$accountDto' in database.", e)
            ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.getErrorMessage())
        }
    }
    private fun AccountDto.toAccount() = try {
        val account = Account(
            name = this.name,
            dispo = this.dispo,
            limit = this.limit
        )
        if (this.accountId != null) {
            account.copy(
                accountId = this.accountId
            )
        } else {
            account
        }
    } catch (e: IllegalArgumentException) {
        throw InvalidInputException("Given AccountDto '$this' is not valid.", e)
    }
    fun Exception.getErrorMessage(): String {
        return message ?: return "Unexpected error occurred."
    }
}