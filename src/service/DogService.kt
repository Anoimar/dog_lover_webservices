package com.thernat.service

import com.thernat.repository.DatabaseFactory.dbQuery
import com.thernat.repository.dao.DogDao
import com.thernat.repository.model.Dog

class DogService {
    suspend fun getAllDogs(): List<Dog> = dbQuery {
        DogDao.all().map { Dog(it) }
    }
}