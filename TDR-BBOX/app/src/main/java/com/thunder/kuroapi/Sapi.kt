package com.thunder.kuroapi


object Sapi {
    init {
        System.loadLibrary("newloader")
    }
    external fun getbaseurl(): String
    external fun libdownloadlink(): String
    external fun getheaders(): List<String>
}