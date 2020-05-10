package com.thernat.repository.table

import org.jetbrains.exposed.dao.id.IntIdTable

object Dogs: IntIdTable() {
    val name = varchar("name", length = 20)
    val owner = varchar("owner", length = 20)
    val breed = varchar("breed", length = 20)
    val sex = varchar("sex", length = 1)
    val picUrl = varchar("pic_url", length = 250)

    override val primaryKey = PrimaryKey(id, name = "PK_Dog_ID")
}