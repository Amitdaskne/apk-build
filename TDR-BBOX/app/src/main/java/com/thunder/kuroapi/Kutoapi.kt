package com.thunder.kuroapi

import android.os.Build
import android.provider.Settings
import com.thunder.LOGS
import com.thunder.kuroapi.database.Database

import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest


class KuroApi(
    override val userkey: String?,
    override val baseurl: String?,
    override val Lisence: String?
) : LoginInfo {

    suspend fun postFormRequest(
        formData: Map<String, String>,
        headers: Map<String, String>
    ): String? {
        runCatching {
            val url = URL(this.baseurl!!)
            val connection = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            for ((key, value) in headers) {
                connection.setRequestProperty(key, value)
            }
            val formBody = formData.map { (key, value) -> "${key}=${value}" }.joinToString("&")

            connection.outputStream.use { os: OutputStream ->
                os.write(formBody.toByteArray(Charsets.UTF_8))
                os.flush()
            }
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                return connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                throw Exception("HTTP POST request failed with response code $responseCode")
            }
        }.onFailure {
            it.printStackTrace()
            return null
        }
        return null
    }

    override suspend fun getDatabase(): Database? {
        return try {
            val strings = Sapi.getheaders()

            val headers = mapOf(
                strings[0] to strings[1],
                strings[2] to strings[3],
                strings[4] to strings[5],
                strings[6] to strings[7]
            )
            val userid = getUUid() ?: "default_ID"
            val theuserid = calculateMD5(userid)


            val formData = mapOf(
                "game" to strings[8], // GlAIM
                strings[9] to userkey.toString(),
                "serial" to theuserid
            )
            val response = withContext(Dispatchers.IO) {
                postFormRequest(formData, headers)
            }
            if (response == null)
                return null

            LOGS.warn(response)
            val moshi = Moshi.Builder().build()
            val jsonAdapter = moshi.adapter(Database::class.java)
            val apiResponse : Database? = jsonAdapter.fromJson(response)
            apiResponse!!
        } catch (err: Exception) {
            err.printStackTrace()
            null
        }
    }

    override suspend fun IsUSerValaid(): Boolean {
        return try {
            val strings = Sapi.getheaders()
            val userid = getUUid() ?: "default_ID"
            val theuserid = calculateMD5(userid)
            val database = getDatabase()
            val usertoken = calculateMD5("${strings[8]}-$userkey-$theuserid-$Lisence")

            LOGS.info("usertoken : $usertoken")
            LOGS.info("servertoken : ${database?.data?.token}")
            if (database == null)
                return false
            if (database.data == null)
                return false
            if (!database.status!!)
                return false
            usertoken == database.data.token
        } catch (err: Exception) {
            err.printStackTrace()
            false
        }
    }

    override suspend fun getUUid(): String? {
        return try {
            val devicebarand = Build.BRAND
            val model = Build.MODEL
            val androi_id = Settings.Secure.ANDROID_ID
            val uuid = androi_id.let {
                "$userkey$it$model$devicebarand"
            }
            uuid
        } catch (err: Exception) {
            err.printStackTrace()
            null
        }
    }

    fun calculateMD5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return runBlocking {
            val digest = md.digest(input.toByteArray())
            digest.fold(StringBuilder()) { acc, byte ->
                acc.append(String.format("%02x", byte))
            }.toString()
        }
    }
}