package com.rsschool.catsapp.api

import com.rsschool.catsapp.model.Cat
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface CatsApi {

    companion object {
        const val BASE_URL = "https://api.thecatapi.com/v1/"
        const val API_KEY = "9fe16ceb-a3f6-4b00-a951-fb8e6d85e09b"
        const val ORDER_BY = "ASC"
    }

    @Headers("x-api-key: $API_KEY")
    @GET("images/search")
    suspend fun loadImages(
        @Query("limit") limit: Int,
        @Query("page") page: Int,
        @Query("order") order: String = ORDER_BY
    ): List<Cat>
}
