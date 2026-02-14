package com.example.wattwait.data.remote.api

import com.example.wattwait.data.remote.dto.UtilityRateResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenEiApi {

    @GET("utility_rates")
    suspend fun getUtilityRatesByAddress(
        @Query("version") version: String = "latest",
        @Query("format") format: String = "json",
        @Query("api_key") apiKey: String,
        @Query("address") address: String,
        @Query("sector") sector: String = "Residential",
        @Query("approved") approved: Boolean = true,
        @Query("detail") detail: String = "full",
        @Query("limit") limit: Int = 50
    ): Response<UtilityRateResponse>

    @GET("utility_rates")
    suspend fun getUtilityRatesByCoordinates(
        @Query("version") version: String = "latest",
        @Query("format") format: String = "json",
        @Query("api_key") apiKey: String,
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("sector") sector: String = "Residential",
        @Query("approved") approved: Boolean = true,
        @Query("detail") detail: String = "full",
        @Query("limit") limit: Int = 50
    ): Response<UtilityRateResponse>

    @GET("utility_rates")
    suspend fun getRateByLabel(
        @Query("version") version: String = "latest",
        @Query("format") format: String = "json",
        @Query("api_key") apiKey: String,
        @Query("getpage") label: String,
        @Query("detail") detail: String = "full"
    ): Response<UtilityRateResponse>
}
