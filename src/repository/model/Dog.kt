package com.thernat.repository.model

import com.thernat.repository.dao.DogDao

data class Dog(
    val dogId: Int,
    val name: String,
    val owner: String,
    val breed: String,
    val sex: String,
    val picUrl: String
) {
    constructor(dog: DogDao) : this(dog.dogId.value, dog.name, dog.owner, dog.breed, dog.sex, dog.picUrl)
}