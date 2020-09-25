package com.thernat.repository

import com.thernat.repository.table.Dogs
import com.thernat.repository.table.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    val databaseUrl = "jdbc:mysql://127.0.0.1:3306/testdb"
    val  user = "testuser"
    val password = "testpassword"

    init {
        try {
            Database.connect(databaseUrl, driver = "com.mysql.jdbc.Driver",
                user = user, password = password)
            transaction {
                SchemaUtils.create(Users)
                SchemaUtils.create(Dogs)
            }
        } catch (e: Exception){
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T =
            withContext(Dispatchers.IO) {
                transaction { block() }
            }
}