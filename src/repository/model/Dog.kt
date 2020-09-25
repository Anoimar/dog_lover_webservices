package com.thernat.repository.model


data class Dog(
    val dogId: Int,
    val name: String?,
    val owner: String,
    val breed: String?,
    val sex: String?,
    val picUrl: String,
    val verified: Boolean = false,
    val description: String?
)