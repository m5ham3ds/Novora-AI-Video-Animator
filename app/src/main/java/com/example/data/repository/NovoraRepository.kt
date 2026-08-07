package com.example.data.repository

import com.example.data.api.ApiClient
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class NovoraRepository(private val baseUrl: String) {
    private val api = ApiClient.getClient(baseUrl)

    suspend fun generateVideo(imageFile: File, audioFile: File, modelName: String): Result<String> {
        return try {
            val imageReqFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageReqFile)

            val audioReqFile = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, audioReqFile)

            val modelReq = modelName.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.generateVideo(imagePart, audioPart, modelReq)
            
            val videoUrl = extractVideoUrl(response, baseUrl)
            if (videoUrl != null) {
                Result.success(videoUrl)
            } else {
                Result.failure(Exception("Video URL not found in response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun extractVideoUrl(json: JsonElement, baseUrl: String): String? {
        // Find inside array `data`
        try {
            if (json is JsonObject) {
                if (json.containsKey("video")) {
                    return json["video"]?.jsonPrimitive?.content
                }
                
                val data = json["data"]
                if (data is JsonArray) {
                    for (item in data) {
                        if (item is JsonObject) {
                            if (item.containsKey("video")) {
                                return item["video"]?.jsonPrimitive?.content
                            }
                            if (item.containsKey("url")) {
                                return baseUrl + item["url"]?.jsonPrimitive?.content
                            }
                            if (item.containsKey("name")) {
                                return baseUrl + "file=" + item["name"]?.jsonPrimitive?.content
                            }
                        } else if (item is JsonArray) {
                            if (item.size > 0) {
                                val first = item[0]
                                if (first is JsonObject && first.containsKey("name")) {
                                    return baseUrl + "file=" + first["name"]?.jsonPrimitive?.content
                                }
                            }
                        } else if (item is JsonPrimitive) {
                            val str = item.content
                            if (str.endsWith(".mp4") || str.startsWith("data:video")) {
                                return if (str.startsWith("http")) str else baseUrl + "file=" + str
                            }
                        }
                    }
                }
            } else if (json is JsonArray) {
                // simple array response
                for (item in json) {
                    if (item is JsonPrimitive) {
                        val str = item.content
                        if (str.endsWith(".mp4")) return baseUrl + "file=" + str
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
