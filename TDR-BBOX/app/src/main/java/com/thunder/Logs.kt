package com.thunder

import android.util.Log

object LOGS {

    val TAG = "ALEX"

    fun warn(info : String ){
        if (BuildConfig.DEBUG)
        {
            Log.w(TAG, info)
        }
    }
    fun error(info : String ){
        if (BuildConfig.DEBUG)
        {
            Log.e(TAG, info)
        }
    }
    fun info(info : String ){
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, info)
        }
    }

}