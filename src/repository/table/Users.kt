package com.thernat.repository.table

import org.jetbrains.exposed.sql.Table

object Users: Table() {
    val fireBaseId = varchar("firebase_id", length = 40)
    val picUrl = varchar("pic_url", length = 250)

    override val primaryKey = PrimaryKey(fireBaseId, name = "PK_ACCOUNT_ID")
}