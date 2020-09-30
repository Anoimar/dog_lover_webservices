package com.thernat.service

import com.thernat.WEB_HOST_BASE_URL
import com.thernat.repository.DatabaseFactory.dbQuery
import com.thernat.repository.model.Dog
import com.thernat.repository.table.Dogs
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.util.KtorExperimentalAPI
import io.ktor.utils.io.streams.*
import org.jetbrains.exposed.sql.*
import java.io.File


class DogService {

    suspend fun getMyDogs(firebaseId: String) = dbQuery {
        Dogs.select { Dogs.owner eq firebaseId }.map {
            it.toDog()
        }
    }

    suspend fun getOtherDogs(firebaseId: String) = dbQuery {
        Dogs.select { Dogs.owner neq firebaseId }
                .andWhere { Dogs.verified eq true }.map {
                    it.toDog()
                }
    }


    suspend fun deleteDog(firebaseId: String, dogId: Int): Boolean {
        val dogPicUrl = dbQuery {
            Dogs.select { Dogs.id eq dogId}
                    .map {
                        it[Dogs.picUrl]
                    }.first().substringAfterLast("/")
        }
        val client = HttpClient()
        client.use {
            val response: String = it.get("$WEB_HOST_BASE_URL/delete_dog_pic.php") {
                parameter("uploader_id",firebaseId)
                parameter("dog_pic_url",dogPicUrl)
            }
            print(response)
        }
        client.close()
        return dbQuery {
            Dogs.deleteWhere { Dogs.id eq dogId } > 0
        }
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
        tempFile?.let {
            val fileName = "${uploaderId}_dog_${System.currentTimeMillis()}${it.name}"
            HttpClient(Apache).use { client ->
                val parts: List<PartData> = formData {
                    val headersBuilder = HeadersBuilder()
                    headersBuilder[HttpHeaders.ContentType] = "image"
                    headersBuilder[HttpHeaders.ContentDisposition] = "filename=$fileName"
                    this.append(
                            "uploaded_file",
                            InputProvider
                            { it.inputStream().asInput() },
                            headersBuilder.build()
                    )
                    this.append("uploader", uploaderId)
                }
                val result = (client.submitFormWithBinaryData<String>(formData = parts) {
                    url("$WEB_HOST_BASE_URL/upload_dog_pic.php")

                }.trim() == "true").also {
                    tempFile?.delete()
                }
                if (result) {
                    dbQuery {
                        Dogs.insert { dog ->
                            dog[owner] = uploaderId
                            dog[sex] = ""
                            dog[description] = dogDescription
                            dog[picName] = dogName
                            dog[breed] = dogBreed
                            dog[picUrl] = "$WEB_HOST_BASE_URL/images/$uploaderId/dogs/$fileName"
                        }
                    }
                    return true
                }
            }
        }
        return false
    }
}

