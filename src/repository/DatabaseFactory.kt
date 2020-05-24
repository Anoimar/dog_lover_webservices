package com.thernat.repository

import com.thernat.repository.table.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {

    init {
        Database.connect("jdbc:mysql://sql7.freemysqlhosting.net:3306/sql7342973", driver = "com.mysql.jdbc.Driver",
                user = "sql7342973", password = File("secrets/db_pass.txt").readText())
        transaction {
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T =
            withContext(Dispatchers.IO) {
                transaction { block() }
            }
}