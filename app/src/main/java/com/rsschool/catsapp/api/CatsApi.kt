package com.rsschool.catsapp.api

import com.rsschool.catsapp.model.Cat
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface CatsApi {

    companion object {
        const val BASE_URL = "https://api.thecatapi.com/v1/"
    }

    @Headers("x-api-key: 9fe16ceb-a3f6-4b00-a951-fb8e6d85e09b")
    @GET("images/search")
    suspend fun loadImages(
        @Query("limit") limit: Int,
        @Query("page") page: Int
    ): List<Cat>
}
