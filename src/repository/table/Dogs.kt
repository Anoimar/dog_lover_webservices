package com.thernat.repository.table

import org.jetbrains.exposed.dao.id.IntIdTable

object Dogs: IntIdTable() {
    val picName = varchar("name", length = 50)
    val owner = varchar("owner", length = 40)
    val breed = varchar("breed", length = 20).nullable()
    val sex = varchar("sex", length = 1).default("?")
    val picUrl = varchar("pic_url", length = 250)
    val description = varchar("description", length = 1000).nullable()
    val verified = bool("verified").default(false)

    override val primaryKey = PrimaryKey(id, name = "PK_Dog_ID")
}