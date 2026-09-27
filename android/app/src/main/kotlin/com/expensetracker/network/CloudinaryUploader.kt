package com.expensetracker.network

import android.content.Context
import android.net.Uri
import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CloudinaryResponse(
    val secure_url: String
)

class CloudinaryUploader {
    private val cloudName = com.expensetracker.BuildConfig.CLOUDINARY_CLOUD
    private val uploadPreset = com.expensetracker.BuildConfig.CLOUDINARY_PRESET

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun upload(context: Context, imageUri: Uri): String? {
        val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
        val bytes = inputStream.readBytes()
        inputStream.close()
        return upload(bytes)
    }

    suspend fun upload(bytes: ByteArray): String? {
        return try {
            Log.d("CloudinaryUploader", "Uploading to cloud=$cloudName preset=$uploadPreset bytes=${bytes.size}")

            val response = client.submitFormWithBinaryData(
                url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload",
                formData = formData {
                    append("upload_preset", uploadPreset, Headers.build {
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"upload_preset\"")
                    })
                    append("file", bytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"receipt.jpg\"")
                    })
                }
            )

            if (response.status.isSuccess()) {
                val url = response.body<CloudinaryResponse>().secure_url
                Log.d("CloudinaryUploader", "Upload success: $url")
                url
            } else {
                val errorBody = response.body<String>()
                Log.e("CloudinaryUploader", "Upload failed: ${response.status} - $errorBody")
                null
            }
        } catch (e: Exception) {
            Log.e("CloudinaryUploader", "Upload exception: ${e.message}", e)
            null
        }
    }
}
