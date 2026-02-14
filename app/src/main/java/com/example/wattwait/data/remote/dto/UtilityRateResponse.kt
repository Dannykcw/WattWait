package com.example.wattwait.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UtilityRateResponse(
    @SerializedName("items")
    val items: List<RateItem>?
)

data class RateItem(
    @SerializedName("label")
    val label: String?,

    @SerializedName("utility")
    val utility: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("uri")
    val uri: String?,

    @SerializedName("sector")
    val sector: String?,

    @SerializedName("approved")
    val approved: Boolean?,

    @SerializedName("startdate")
    val startDate: Long?,

    @SerializedName("enddate")
    val endDate: Long?,

    @SerializedName("fixedmonthlycharge")
    val fixedMonthlyCharge: Double?,

    @SerializedName("minmonthlycharge")
    val minMonthlyCharge: Double?,

    @SerializedName("energyratestructure")
    val energyRateStructure: List<List<EnergyRateTier>>?,

    @SerializedName("energyweekdayschedule")
    val energyWeekdaySchedule: List<List<Int>>?,

    @SerializedName("energyweekendschedule")
    val energyWeekendSchedule: List<List<Int>>?,

    @SerializedName("demandratestructure")
    val demandRateStructure: List<List<DemandRateTier>>?,

    @SerializedName("demandweekdayschedule")
    val demandWeekdaySchedule: List<List<Int>>?,

    @SerializedName("demandweekendschedule")
    val demandWeekendSchedule: List<List<Int>>?
)

data class EnergyRateTier(
    @SerializedName("max")
    val max: Double?,

    @SerializedName("rate")
    val rate: Double?,

    @SerializedName("adj")
    val adjustment: Double?,

    @SerializedName("unit")
    val unit: String?,

    @SerializedName("sell")
    val sell: Double?
)

data class DemandRateTier(
    @SerializedName("max")
    val max: Double?,

    @SerializedName("rate")
    val rate: Double?,

    @SerializedName("adj")
    val adjustment: Double?,

    @SerializedName("unit")
    val unit: String?
)
