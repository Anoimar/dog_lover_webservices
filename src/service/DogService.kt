package com.thernat.service

import com.thernat.copyToSuspend
import com.thernat.repository.DatabaseFactory.dbQuery
import com.thernat.repository.dao.DogDao
import com.thernat.repository.model.Dog
import com.thernat.repository.table.Dogs
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.forms.*
import io.ktor.client.request.url
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.utils.io.streams.asInput
import org.jetbrains.exposed.sql.update
import java.io.File


class DogService {
    suspend fun getAllDogs(): List<Dog> = dbQuery {
        DogDao.all().map { Dog(it) }
    }

    suspend fun uploadDogPicture(multipart: MultiPartData): Boolean {
        var uploader: String? = null
        var tempFile: File? = null
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    if (part.name == "uploader") {
                        uploader = part.value
                    }
                }
                is PartData.FileItem -> {
                    val ext = File(part.originalFileName).extension
                    val uploadTime = System.currentTimeMillis()
                    tempFile = File("${uploadTime}.$ext")
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
        val uploaderId: Int = uploader?.toInt() ?: return false

        tempFile?.let {
            val fileName = "${uploaderId}_${it.name}"
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
                }

                val result = (client.submitFormWithBinaryData<String>(formData = parts) {
                    url("$WEB_HOST_BASE_URL/upload_image.php")
                }.trim() == "true").also {
                    tempFile?.delete()
                }
                if (result) {
                    dbQuery {
                        Dogs.update({ Dogs.id eq uploaderId }) { updateStatement ->
                            updateStatement[picUrl] = "$WEB_HOST_BASE_URL/images/$fileName"
                        }
                    }
                    return true
                }
            }
        }
        return false
    }

    companion object {
        const val WEB_HOST_BASE_URL = "https://doggielover.000webhostapp.com"
    }
}