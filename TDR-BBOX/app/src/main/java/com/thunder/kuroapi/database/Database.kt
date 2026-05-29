package com.thunder.kuroapi.database

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class Database(
    val status: Boolean?,
    val data: Data?,
    val reason : String?
)

@Keep
@JsonClass(generateAdapter = true)
data class Data(
    val real: String?,
    val token: String?,
    val modname: String?,
    val mod_status: String?,
    val credit: String?,
    val ESP: String?,
    val Item: String?,
    val AIM: String?,
    val SilentAim: String?,
    val BulletTrack: String?,
    val Floating: String?,
    val Memory: String?,
    val Setting: String?,
    val expired_date: String?,
    val EXP: String?,
    val Enc: String?,
    val exdate: String?,
    val device: Int?,
    val rng: Long?,



    val Announcement: String?,
    val SLOT: String?,
    val appName: String?,
    val key: String?,
    val statusText: String?,
    val updateversion: String?,
    val updateapklink: String?,
    val updateinfo: String?,
    val updatetitle: String?,
    val updated: String?,
    val version: String?,
    val bypass: String?
)