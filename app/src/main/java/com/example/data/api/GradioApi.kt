package com.example.data.api

import kotlinx.serialization.json.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface GradioApi {
    @Multipart
    @POST("api/predict/")
    suspend fun generateVideo(
        @Part image: MultipartBody.Part,
        @Part audio: MultipartBody.Part,
        @Part("model") modelName: RequestBody
    ): JsonElement
}
