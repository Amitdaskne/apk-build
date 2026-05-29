package com.thunder

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import com.vbox.VBoxCore
import com.vbox.app.configuration.ClientConfiguration
import com.vbox.app.configuration.AppLifecycleCallback
import net_62v.external.MetaActivationManager
import java.io.File
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
class app : Application() {
     
     companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var mContext: Context

        @JvmStatic
        fun getContext(): Context {
            return mContext
        }
     }
    
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        mContext = base!!
        AppManager.doAttachBaseContext(base)
    }
    
    override fun onCreate() {
        super.onCreate()
        AppManager.doOnCreate()
        kotlin.runCatching {
            MetaActivationManager.activateSdk("30day>Captainsrc")
        }
    }

    // -----------------------------
    // AES DECRYPT
    // -----------------------------
    @SuppressLint("GetInstance")
    fun decryptString(keystring: String, pass: String): String {
        return try {
            val decodedKey: ByteArray = Base64.getDecoder().decode(pass)
            val secretKey: SecretKey = SecretKeySpec(decodedKey, "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val encryptedBytes: ByteArray = Base64.getDecoder().decode(keystring)
            val decryptedBytes: ByteArray = cipher.doFinal(encryptedBytes)
            String(decryptedBytes)
        } catch (err: Exception) {
            err.printStackTrace()
            "nothing"
        }
    }
}