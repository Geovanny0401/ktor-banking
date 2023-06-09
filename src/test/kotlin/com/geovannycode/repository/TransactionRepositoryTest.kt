package com.geovannycode.repository

import com.geovannycode.TestDatabaseFactory
import com.geovannycode.di.bankingModule
import com.geovannycode.entities.transaction.TransactionEntity
import com.geovannycode.entities.transaction.TransactionTable
import com.geovannycode.models.Account
import com.geovannycode.models.Transaction
import com.geovannycode.models.User
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import java.time.LocalDate
import java.util.UUID

class TransactionRepositoryTest : KoinTest {
    private lateinit var databaseFactory: TestDatabaseFactory
    private val accountRepository by inject<AccountRepository>()
    private val userRepository by inject<UserRepository>()
    private val transactionRepository by inject<TransactionRepository>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            bankingModule
        )
    }

    @BeforeEach
    fun setupDatasource() {
        databaseFactory = TestDatabaseFactory()
        databaseFactory.connect()
    }

    @AfterEach
    fun tearDownDatasource() {
        databaseFactory.close()
    }

    @Test
    fun `save persists new transaction to database`() {
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "Geovanny",
            lastName = "Mendoza",
            birthdate = LocalDate.of(2002, 1, 1),
            password = "Ta1&tudol3lal54e",
            accounts = listOf()
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedAccount = accountRepository.saveForUser(persistedUser, account)
        val otherAccount = Account(
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedAccount,
            target = persistedOtherAccount,
            amount = 60.0
        )

        val current = transactionRepository.save(transaction)

        assertThat(current).isNotNull
        assertThat(transaction {
            TransactionEntity.find { TransactionTable.transactionId eq current.transactionId }.single()
        }).isNotNull
    }

    @Test
    fun `save throws exception if origin account not exists in database`() {
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "Geovanny",
            lastName = "Mendoza",
            birthdate = LocalDate.of(2002, 1, 1),
            password = "Ta1&tudol3lal54e",
            accounts = listOf()
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val otherAccount = Account(
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = account,
            target = persistedOtherAccount,
            amount = 60.0
        )

        assertThatThrownBy { transactionRepository.save(transaction) }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `save throws exception if target account not exists in database`() {
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "Geovanny",
            lastName = "Mendoza",
            birthdate = LocalDate.of(2002, 1, 1),
            password = "Ta1&tudol3lal54e",
            accounts = listOf()
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val otherAccount = Account(
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedOtherAccount,
            target = account,
            amount = 60.0
        )

        assertThatThrownBy { transactionRepository.save(transaction) }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `save throws exception if transaction already exists in database`() {
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "Geovanny",
            lastName = "Mendoza",
            birthdate = LocalDate.of(2002, 1, 1),
            password = "Ta1&tudol3lal54e",
            accounts = listOf()
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedAccount = accountRepository.saveForUser(persistedUser, account)
        val otherAccount = Account(
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedOtherAccount,
            target = persistedAccount,
            amount = 60.0
        )
        transactionRepository.save(transaction)

        assertThatThrownBy { transactionRepository.save(transaction) }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `findAllByAccount returns matching transaction for origin account`() {
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "Geovanny",
            lastName = "Mendoza",
            birthdate = LocalDate.of(2002, 1, 1),
            password = "Ta1&tudol3lal54e",
            accounts = listOf()
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedAccount = accountRepository.saveForUser(persistedUser, account)
        val otherAccount = Account(
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedAccount,
            target = persistedOtherAccount,
            amount = 60.0
        )
        val persistedTransaction = transactionRepository.save(transaction)

        val actual = transactionRepository.findAllByAccount(persistedAccount)

        assertThat(actual.map { it.transactionId }).containsExactly(persistedTransaction.transactionId)
    }

    @Test
    fun `findAllByAccount returns matching transaction for target account`() {
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "Geovanny",
            lastName = "Mendoza",
            birthdate = LocalDate.of(2002, 1, 1),
            password = "Ta1&tudol3lal54e",
            accounts = listOf()
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedAccount = accountRepository.saveForUser(persistedUser, account)
        val otherAccount = Account(
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedAccount,
            target = persistedOtherAccount,
            amount = 60.0
        )
        val persistedTransaction = transactionRepository.save(transaction)

        val actual = transactionRepository.findAllByAccount(persistedOtherAccount)

        assertThat(actual.map { it.transactionId }).containsExactly(persistedTransaction.transactionId)
    }

    @Test
    fun `findAllByAccount returns multiple matching transactions for account`() {
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "Geovanny",
            lastName = "Mendoza",
            birthdate = LocalDate.of(2002, 1, 1),
            password = "Ta1&tudol3lal54e",
            accounts = listOf()
        )
        val persistedUser = userRepository.save(user)

        val otherUser = User(
            userId = UUID.randomUUID(),
            firstName = "Manuel",
            lastName = "Gonzalez",
            birthdate = LocalDate.of(2002, 1, 1),
            password = "Ta1&tudol3lal54e",
            accounts = listOf()
        )
        val persistedOtherUser = userRepository.save(otherUser)

        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedAccount = accountRepository.saveForUser(persistedUser, account)
        val otherAccount = Account(
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedOtherUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedAccount,
            target = persistedOtherAccount,
            amount = 60.0
        )
        val persistedTransaction = transactionRepository.save(transaction)

        val otherTransaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedOtherAccount,
            target = persistedAccount,
            amount = 60.0
        )
        val persistedOtherTransaction = transactionRepository.save(otherTransaction)

        val actual = transactionRepository.findAllByAccount(persistedAccount)

        assertThat(actual.map { it.transactionId }).containsExactly(
            persistedTransaction.transactionId,
            persistedOtherTransaction.transactionId
        )
    }
}