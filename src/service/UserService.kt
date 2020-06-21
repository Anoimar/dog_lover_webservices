package com.thernat.service

import com.thernat.WEB_HOST_BASE_URL
import com.thernat.repository.DatabaseFactory.dbQuery
import com.thernat.repository.model.User
import com.thernat.repository.table.Users
import com.thernat.service.exception.UserNotFoundException
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.url
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.util.KtorExperimentalAPI
import io.ktor.utils.io.streams.asInput
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.jetbrains.exposed.sql.select
import repository.insertOrUpdate
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class UserService {

    suspend fun getUserData(firebaseId: String) = dbQuery {
        Users.select { Users.fireBaseId eq firebaseId }.map {
            User(it[Users.fireBaseId], it[Users.picUrl])
        }.firstOrNull() ?: throw UserNotFoundException()
    }

    @KtorExperimentalAPI
    suspend fun uploadUserAvatar(multipart: MultiPartData): Boolean {
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
                    this.append("uploader",uploaderId)
                }
                val result = (client.submitFormWithBinaryData<String>(formData = parts) {
                    url("$WEB_HOST_BASE_URL/upload_image.php")

                }.trim() == "true").also {
                    tempFile?.delete()
                }
                if (result) {
                    dbQuery {

                        Users.insertOrUpdate(Users.fireBaseId) { user ->
                            user[fireBaseId] = uploaderId
                            user[picUrl] = "$WEB_HOST_BASE_URL/images/$uploaderId/$fileName"
                        }
                    }
                    return true
                }
            }
        }
        return false
    }

}

suspend fun InputStream.copyToSuspend(
        out: OutputStream,
        bufferSize: Int = DEFAULT_BUFFER_SIZE,
        yieldSize: Int = 4 * 1024 * 1024,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
): Long {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0L
        var bytesAfterYield = 0L
        while (true) {
            val bytes = read(buffer).takeIf { it >= 0 } ?: break
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes
        }
        return@withContext bytesCopied
    }
}