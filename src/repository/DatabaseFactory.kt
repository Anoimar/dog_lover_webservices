package com.thernat.repository

import com.thernat.repository.table.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    init {
        Database.connect("jdbc:mysql://127.0.0.1:3306/testdb", driver = "com.mysql.jdbc.Driver",
                user = "testuser", password = "testpassword")
        transaction {
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T =
            withContext(Dispatchers.IO) {
                transaction { block() }
            }
}