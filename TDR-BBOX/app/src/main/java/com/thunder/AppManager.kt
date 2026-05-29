package com.thunder

import android.content.Context
import android.content.SharedPreferences
import com.thunder.view.main.BlackBoxLoader


object AppManager {
    @JvmStatic
    val mBlackBoxLoader by lazy {
        BlackBoxLoader()
    }

    @JvmStatic
    val mRemarkSharedPreferences: SharedPreferences by lazy {
        app.getContext().getSharedPreferences("UserRemark", Context.MODE_PRIVATE)
    }

    fun doAttachBaseContext(context: Context) {
        try {
            mBlackBoxLoader.attachBaseContext(context)
            mBlackBoxLoader.addLifecycleCallback()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun doOnCreate() {
        mBlackBoxLoader.doOnCreate()
        initThirdService()
    }

    private fun initThirdService() { }
}
