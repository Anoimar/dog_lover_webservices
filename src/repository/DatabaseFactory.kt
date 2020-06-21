package com.thernat.repository

import com.thernat.repository.table.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    init {
        try {
            Database.connect("jdbc:mysql://sql7.freemysqlhosting.net:3306/sql7349985", driver = "com.mysql.jdbc.Driver",
                user = "sql7349985", password = "")
            transaction {
                SchemaUtils.create(Users)
            }
        } catch (e: Exception){
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T =
            withContext(Dispatchers.IO) {
                transaction { block() }
            }
}