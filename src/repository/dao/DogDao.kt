package com.thernat.repository.dao

import com.thernat.repository.table.Dogs
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class DogDao(id: EntityID<Int>) : IntEntity(id) {
 companion object : IntEntityClass<DogDao>(Dogs)

 var dogId by Dogs.id
 var name by Dogs.name
 var owner by Dogs.owner
 var breed by Dogs.breed
 var sex by Dogs.sex
 var picUrl by Dogs.picUrl
}