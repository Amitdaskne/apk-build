package com.thunder.kuroapi

import com.thunder.kuroapi.database.Database


interface LoginInfo {
    suspend fun getDatabase() : Database?
    suspend fun getUUid() : String ?
    suspend fun IsUSerValaid() : Boolean
    val userkey : String?
    val baseurl : String?
    val Lisence : String?
}
