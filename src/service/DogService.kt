package com.thernat.service

import com.thernat.repository.DatabaseFactory.dbQuery
import com.thernat.repository.model.Dog
import com.thernat.repository.table.Dogs
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.io.File


class DogService {

    suspend fun getMyDogs(firebaseId: String) = dbQuery {
        Dogs.select { Dogs.owner eq firebaseId }.map {
           it.toDog()
        }
    }

    suspend fun deleteDog(firebaseId: String, dogId: Int) = dbQuery {
        Dogs.deleteWhere { Dogs.id eq dogId } > 0
    }

    private fun ResultRow.toDog() = Dog(
            name = this[Dogs.picName],
            dogId = this[Dogs.id].value,
            sex = this[Dogs.sex],
            breed = this[Dogs.breed],
            owner = this[Dogs.owner],
            picUrl = this[Dogs.picUrl],
            verified = this[Dogs.verified],
            description = this[Dogs.description]
    )


    @KtorExperimentalAPI
    suspend fun uploadDogPics(multipart: MultiPartData): Boolean {
        var uploader: String? = null
        var name: String? = null
        var dogDescription: String? = null
        var dogBreed: String? = null
        var tempFile: File? = null
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "uploader" -> uploader = part.value
                        "name" -> name = part.value
                        "description" -> dogDescription = part.value
                        "breed" -> dogBreed = part.value
                    }
                }
                is PartData.FileItem -> {
                    val ext = File(part.originalFileName).extension
                    tempFile = File("av.$ext")
                    part.streamProvider().use { input ->
                        tempFile?.outputStream()?.buffered().use { output ->
                            if (output != null) {
                                input.copyToSuspend(output)
                            }
                        }
                    }
                }
            }
            part.dispose()
        }
        val uploaderId: String = uploader ?: return false
        val dogName: String = name ?: return false
        dbQuery {
            Dogs.insert { dog ->
                dog[owner] = uploaderId
                dog[sex] = ""
                dog[description] = dogDescription
                dog[picName] = dogName
                dog[breed] = dogBreed
                dog[picUrl] = "https://images.pexels.com/photos/4681107/pexels-photo-4681107.jpeg?auto=compress&cs=tinysrgb&dpr=3&h=750&w=1260"
            }
        }
        return true
    }
}

